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
        String otp = otpService.generateOtp(phone);
        smsService.sendOtp(phone, otp);
        return ResponseEntity.ok("OTP sent");
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestParam String phoneNo, @RequestParam String otp) {
        if (otpService.validateOtp(phoneNo, otp)) {
            User user = userRepository.findById(phoneNo).orElseGet(() -> {
                User newUser = new User();
                String normalizedPhone = phoneNo != null ? phoneNo.replaceFirst("^\\+", "") : null;
                newUser.setPhoneNo(normalizedPhone.trim());
                return newUser;
            });
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);
            otpService.clearOtp(phoneNo);
            return ResponseEntity.ok("Login successful");
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid OTP");
    }

    private String generateBase62Id() {
        String base62 = RandomStringUtils.random(6, true, true); // or use Base62 library
        return base62.toUpperCase();
    }
}
