package com.tandem.otp_auth_service.controller;

import com.tandem.otp_auth_service.entity.User;
import com.tandem.otp_auth_service.repository.UserRepository;
import com.tandem.otp_auth_service.service.OtpService;
import com.tandem.otp_auth_service.service.SmsService;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final OtpService otpService;
    private final SmsService smsService;
    private final UserRepository userRepository;

    public AuthController(OtpService otpService, SmsService smsService, UserRepository userRepository) {
        this.otpService = otpService;
        this.smsService = smsService;
        this.userRepository = userRepository;
    }

    @PostMapping("/send-otp")
    public ResponseEntity<?> sendOtp(@RequestParam String phone) {
        // Trim whitespace from phone parameter
        phone = phone != null ? phone.trim() : null;

        try {
            String otp = otpService.generateOtp(phone);
            smsService.sendOtp(phone, otp);
            return ResponseEntity.ok().body(Map.of(
                "status", "success",
                "message", "OTP sent successfully",
                "phone", phone
            ));
        } catch (Exception e) {
            // Log the actual error for debugging
            System.err.println("Error sending OTP to " + phone + ": " + e.getMessage());
            e.printStackTrace();

            // Return appropriate error response based on the type of error
            if (e.getMessage().contains("Twilio Authentication Failed")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "status", "error",
                    "message", "SMS service authentication failed",
                    "error_type", "AUTHENTICATION_ERROR",
                    "details", e.getMessage()
                ));
            } else if (e.getMessage().contains("Twilio API Error")) {
                return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(Map.of(
                    "status", "error",
                    "message", "SMS service unavailable",
                    "error_type", "SMS_SERVICE_ERROR",
                    "details", e.getMessage()
                ));
            } else if (e.getMessage().contains("not properly configured")) {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of(
                    "status", "error",
                    "message", "SMS service not configured",
                    "error_type", "CONFIGURATION_ERROR",
                    "details", "SMS service is temporarily unavailable"
                ));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "status", "error",
                    "message", "Failed to send OTP",
                    "error_type", "UNKNOWN_ERROR",
                    "details", e.getMessage()
                ));
            }
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestParam String phone, @RequestParam String otp) {
        // Trim whitespace from parameters
        phone = phone != null ? phone.trim() : null;
        otp = otp != null ? otp.trim() : null;

        // Create final variables for lambda usage
        final String finalPhone = phone;
        final String finalOtp = otp;


        try {
            if (otpService.validateOtp(finalPhone, finalOtp)) {
                User user = userRepository.findById(finalPhone).orElseGet(() -> {
                    User newUser = new User();
                    String normalizedPhone = finalPhone != null ? finalPhone.replaceFirst("^\\+", "") : null;
                    newUser.setPhoneNo(normalizedPhone.trim());
                    return newUser;
                });
                user.setLastLogin(LocalDateTime.now());
                userRepository.save(user);
                otpService.clearOtp(finalPhone);

                return ResponseEntity.ok().body(Map.of(
                    "status", "success",
                    "message", "OTP verified successfully",
                    "phone", finalPhone,
                    "user_id", user.getUserId() != null ? user.getUserId() : "generated",
                    "login_time", LocalDateTime.now().toString()
                ));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "status", "error",
                    "message", "Invalid or expired OTP",
                    "error_type", "INVALID_OTP",
                    "phone", finalPhone
                ));
            }
        } catch (Exception e) {
            // Log the actual error for debugging
            System.err.println("Error verifying OTP for " + finalPhone + ": " + e.getMessage());
            e.printStackTrace();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "status", "error",
                "message", "Failed to verify OTP",
                "error_type", "VERIFICATION_ERROR",
                "details", e.getMessage(),
                "phone", finalPhone
            ));
        }
    }

    private String generateBase62Id() {
        String base62 = RandomStringUtils.random(6, true, true); // or use Base62 library
        return base62.toUpperCase();
    }
}
