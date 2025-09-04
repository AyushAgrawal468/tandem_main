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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    public AuthController(OtpService otpService, SmsService smsService, UserRepository userRepository) {
        this.otpService = otpService;
        this.smsService = smsService;
        this.userRepository = userRepository;
    }

    @PostMapping("/send-otp")
    public ResponseEntity<?> sendOtp(@RequestBody OtpRequest request) {
        String phone = request.getPhone() != null ? request.getPhone().trim() : null;
        String countryCode = request.getCountryCode() != null ? request.getCountryCode().trim() : null;

        //checking if the user is blocked or not for the wrong otp entry more than 3 times

        BlockedUser blockedUser = blockedUserRepository.findByPhoneNo(phone).orElse(null);
        if (blockedUser!=null){
            Duration duration = Duration.between(blockedUser.getBlockedAt(), LocalDateTime.now());
            if (blockedUser.getResendOtpCount()>=3 && duration.toMinutes()<=30){

                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(Map.of(
                        "status", "Too many incorrect OTP attempts. Please try again later.",
                        "statusId", "4"
                ));
            }else if(blockedUser.getResendOtpCount()>=3 && duration.toMinutes()>30){
                blockedUserRepository.delete(blockedUser);
            }
        }
        try {
            String otp = otpService.generateOtp(phone);

            // 🔹 Debug logs
            System.out.println("[DEBUG] OTP generated for phone: " + phone);
            System.out.println("[DEBUG] Generated OTP: " + otp);

            smsService.sendOtp(phone, otp, countryCode);
            System.out.println("[DEBUG] OTP sent to: " + phone);

            return ResponseEntity.ok().body(Map.of(
                    "status", "OTP successfully sent",
                    "statusId","1"
            ));
        } catch (Exception e) {
            System.err.println("Error sending OTP to " + phone + ": " + e.getMessage());
            e.printStackTrace();

            if (e.getMessage().contains("Twilio Authentication Failed")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                        "status", "SMS service authentication failed",
                        "statusId", "5"
                ));
            } else if (e.getMessage().contains("Twilio API Error")) {
                return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(Map.of(
                        "status", "SMS service unavailable",
                        "message", "5"
                ));
            } else if (e.getMessage().contains("not properly configured")) {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of(
                        "status", "SMS service not configured",
                        "message", "5"
                ));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                        "status", "Failed to send OTP",
                        "message", "5"
                ));
            }
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody OtpVerifyRequest otpVerifyRequest) {
        String finalPhone = otpVerifyRequest.getPhone() != null ? otpVerifyRequest.getPhone().trim() : null;
        String finalOtp = otpVerifyRequest.getOtp() != null ? otpVerifyRequest.getOtp().trim() : null;
        String countryCode = otpVerifyRequest.getCountryCode() != null ? otpVerifyRequest.getCountryCode().trim() : null;

        try {
            // 🔹 Debug logs
            System.out.println("[DEBUG] Verifying OTP for phone: " + finalPhone);
            System.out.println("[DEBUG] Entered OTP: " + finalOtp);


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

                return ResponseEntity.ok().body(Map.of(
                        "status", "OTP verified successfully",
                        "statusId", "1",
                        "user_id", user.getUserId() != null ? user.getUserId() : "generated"
                ));
            } else {
                System.out.println("[DEBUG] OTP validation failed for: " + finalPhone);

                BlockedUser blockedUser = blockedUserRepository.findByPhoneNo(finalPhone).orElseGet(() -> {
                    BlockedUser newBlockedUser = new BlockedUser();
                    newBlockedUser.setPhoneNo(finalPhone);
                    newBlockedUser.setResendOtpCount(0);
                    return newBlockedUser;
                });
                blockedUser.setBlockedAt(LocalDateTime.now());
                blockedUser.setResendOtpCount(blockedUser.getResendOtpCount()+1);
                blockedUserRepository.save(blockedUser);
                System.out.println("[DEBUG] Blocked user entry updated for phone: " + finalPhone);

                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                        "status", "Invalid or expired OTP",
                        "statusId", "2",
                        "user_id", null
                ));
            }
        } catch (Exception e) {
            System.err.println("Error verifying OTP for " + finalPhone + ": " + e.getMessage());
            e.printStackTrace();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "status", "Failed to verify OTP",
                    "message", "3",
                    "user_id", null
            ));
        }
    }

    private String generateBase62Id() {
        String base62 = RandomStringUtils.random(6, true, true);
        return base62.toUpperCase();
    }
}
