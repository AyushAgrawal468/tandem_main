package com.tandem.otp_auth_service.controller;

import com.tandem.otp_auth_service.dto.OtpRequest;
import com.tandem.otp_auth_service.dto.OtpVerifyRequest;
import com.tandem.otp_auth_service.entity.BlockedUser;
import com.tandem.otp_auth_service.entity.User;
import com.tandem.otp_auth_service.entity.UserPackages;
import com.tandem.otp_auth_service.repository.BlockedUserRepository;
import com.tandem.otp_auth_service.repository.UserPackageRepository;
import com.tandem.otp_auth_service.repository.UserRepository;
import com.tandem.otp_auth_service.service.OtpService;
import com.tandem.otp_auth_service.service.SmsService;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.tandemit.security.AESUtil;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final OtpService otpService;
    private final SmsService smsService;
    private final UserRepository userRepository;

    @Autowired
    private UserPackageRepository userPackagesRepository;

    @Autowired
    private BlockedUserRepository blockedUserRepository;

    @Value("${security.key}")
    private String secretKey;

    public AuthController(OtpService otpService, SmsService smsService, UserRepository userRepository) {
        this.otpService = otpService;
        this.smsService = smsService;
        this.userRepository = userRepository;
    }

    @PostMapping("/send-otp")
    public ResponseEntity<?> sendOtp(@RequestBody String request) throws Exception {
        OtpRequest otpRequest = otpRequest = AESUtil.decrypt(request, secretKey, OtpRequest.class);

        String phone = otpRequest.getPhone() != null ? otpRequest.getPhone().trim() : null;
        String countryCode = otpRequest.getCountryCode() != null ? otpRequest.getCountryCode().trim() : null;
        String appSignature = otpRequest.getAppSignature() != null ? otpRequest.getAppSignature().trim() : null;

        //checking if the user is blocked or not for the wrong otp entry more than 3 times

        BlockedUser blockedUser = blockedUserRepository.findByPhoneNo(phone).orElse(null);
        if (blockedUser!=null){
            Duration duration = Duration.between(blockedUser.getBlockedAt(), LocalDateTime.now());
            if (blockedUser.getResendOtpCount()>=3 && duration.toMinutes()<=30){

                Map<String, Object> response = Map.of(
                        "status", "Too many incorrect OTP attempts. Please try again later.",
                        "statusId", "2"
                );
                String encryptedResponse = AESUtil.encrypt(response, secretKey);
                return ResponseEntity.status(HttpStatus.OK).body(encryptedResponse);
            }else if(blockedUser.getResendOtpCount()>=3 && duration.toMinutes()>30){
                blockedUserRepository.delete(blockedUser);
            }
        }
        try {
            String otp = otpService.generateOtp(phone);

            // 🔹 Debug logs
            System.out.println("[DEBUG] OTP generated for phone: " + phone);
            System.out.println("[DEBUG] Generated OTP: " + otp);

            smsService.sendOtp(phone, otp, countryCode, appSignature);
            System.out.println("[DEBUG] OTP sent to: " + phone);
            Map<String, Object> response = Map.of(
                    "status", "OTP successfully sent",
                    "statusId","1"
            );
            String encryptedResponse = AESUtil.encrypt(response, secretKey);
            return ResponseEntity.ok().body(encryptedResponse);
        } catch (Exception e) {
            System.err.println("Error sending OTP to " + phone + ": " + e.getMessage());
            e.printStackTrace();

            if (e.getMessage().contains("Twilio Authentication Failed")) {
                Map<String, Object> response = Map.of(
                        "status", "SMS service authentication failed",
                        "statusId", "5"
                );
                String encryptedResponse = AESUtil.encrypt(response, secretKey);
                return ResponseEntity.status(HttpStatus.OK).body(encryptedResponse);
            } else if (e.getMessage().contains("Twilio API Error")) {
                Map<String, Object> response = Map.of(
                        "status", "SMS service unavailable",
                        "statusId", "5"
                );
                String encryptedResponse = AESUtil.encrypt(response, secretKey);
                return ResponseEntity.status(HttpStatus.OK).body(encryptedResponse);
            } else if (e.getMessage().contains("not properly configured")) {
                Map<String, Object> response = Map.of(
                        "status", "SMS service not configured",
                        "statusId", "5"
                );
                String encryptedResponse = AESUtil.encrypt(response, secretKey);
                return ResponseEntity.status(HttpStatus.OK).body(encryptedResponse);
            } else {
                Map<String, Object> response = Map.of(
                        "status", "Failed to send OTP",
                        "statusId", "5"
                );
                String encryptedResponse = AESUtil.encrypt(response, secretKey);
                return ResponseEntity.status(HttpStatus.OK).body(encryptedResponse);
            }
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody String otpVerifyRequest) throws Exception {

        OtpVerifyRequest otpVerifyRequestDecrypted = AESUtil.decrypt(otpVerifyRequest, secretKey, OtpVerifyRequest.class);

        String finalPhone = otpVerifyRequestDecrypted.getPhone() != null ? otpVerifyRequestDecrypted.getPhone().trim() : null;
        String finalOtp = otpVerifyRequestDecrypted.getOtp() != null ? otpVerifyRequestDecrypted.getOtp().trim() : null;
        String countryCode = otpVerifyRequestDecrypted.getCountryCode() != null ? otpVerifyRequestDecrypted.getCountryCode().trim() : null;

        try {
            // 🔹 Debug logs
            System.out.println("[DEBUG] Verifying OTP for phone: " + finalPhone);
            System.out.println("[DEBUG] Entered OTP: " + finalOtp);

            BlockedUser blockedUser = blockedUserRepository.findByPhoneNo(finalPhone).orElse(null);
            if (blockedUser!=null){
                Duration duration = Duration.between(blockedUser.getBlockedAt(), LocalDateTime.now());
                if (blockedUser.getResendOtpCount()>=3 && duration.toMinutes()<=30){
                    return ResponseEntity.status(HttpStatus.OK).body(Map.of(
                            "status", "Too many incorrect OTP attempts. Please try again later.",
                            "statusId", "2"
                    ));
                }else if(blockedUser.getResendOtpCount()>=3 && duration.toMinutes()>30){
                    blockedUserRepository.delete(blockedUser);
                }
            }

            if (otpService.validateOtp(finalPhone, finalOtp)) {
                System.out.println("[DEBUG] OTP validation successful for: " + finalPhone);

                User user = userRepository.findById(finalPhone).orElseGet(() -> {
                    User newUser = new User();
                    newUser.setPhoneNo(finalPhone.trim());
                    newUser.setCountryCode(countryCode);
                    System.out.println("[DEBUG] New user created with phone: " + finalPhone);
                    return newUser;
                });

                user.setLastLogin(LocalDateTime.now());
                user = userRepository.save(user);
                System.out.println("[DEBUG] User saved/updated in DB: " + user.getPhoneNo());

                otpService.clearOtp(finalPhone);
                System.out.println("[DEBUG] OTP cleared for phone: " + finalPhone);

                final String userId = user.getUserId();

                UserPackages userPackage = userPackagesRepository.findByUserId(user.getUserId()).orElseGet(() -> {
                    UserPackages newUserPackage = new UserPackages();
                    newUserPackage.setUserId(userId);
                    newUserPackage.setPackageType("FREE_PLAN");
                    newUserPackage.setPackageMonths("12");
                    newUserPackage.setPackageStatus("ACTIVE");
                    newUserPackage.setOnBoardingStatus("1");
                    return newUserPackage;
                });

                userPackagesRepository.save(userPackage);
                System.out.println("[DEBUG] User package ensured for userId: " + userId);

                Map<String, Object> response = Map.of(
                        "status", "OTP verified successfully",
                        "statusId", "1",
                        "user_id", user.getUserId() != null ? user.getUserId() : "generated"
                );
                String encryptedResponse = AESUtil.encrypt(response, secretKey);

                return ResponseEntity.ok().body(encryptedResponse);
            } else {
                System.out.println("[DEBUG] OTP validation failed for: " + finalPhone);

                BlockedUser blockedUsr = blockedUserRepository.findByPhoneNo(finalPhone).orElseGet(() -> {
                    BlockedUser newBlockedUser = new BlockedUser();
                    newBlockedUser.setPhoneNo(finalPhone);
                    newBlockedUser.setResendOtpCount(0);
                    return newBlockedUser;
                });
                blockedUsr.setBlockedAt(LocalDateTime.now());
                blockedUsr.setResendOtpCount(blockedUsr.getResendOtpCount()+1);
                blockedUserRepository.save(blockedUsr);
                System.out.println("[DEBUG] Blocked user entry updated for phone: " + finalPhone);

                Map<String, Object> response = Map.of(
                        "status", "Invalid or expired OTP",
                        "statusId", "2",
                        "user_id", ""
                );
                String encryptedResponse = AESUtil.encrypt(response, secretKey);

                return ResponseEntity.status(HttpStatus.OK).body(encryptedResponse);
            }
        } catch (Exception e) {
            System.err.println("Error verifying OTP for " + finalPhone + ": " + e.getMessage());
            e.printStackTrace();

            Map<String, Object> response = Map.of(
                    "status", "Failed to verify OTP",
                    "statusId", "3",
                    "user_id", ""
            );
            String encryptedResponse = AESUtil.encrypt(response, secretKey);

            return ResponseEntity.status(HttpStatus.OK).body(encryptedResponse);
        }
    }

    private String generateBase62Id() {
        String base62 = RandomStringUtils.random(6, true, true);
        return base62.toUpperCase();
    }
}
