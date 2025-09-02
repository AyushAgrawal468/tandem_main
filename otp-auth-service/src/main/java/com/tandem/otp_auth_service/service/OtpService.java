package com.tandem.otp_auth_service.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {
    private final Map<String, String> otpStorage = new ConcurrentHashMap<>();

    public String generateOtp(String phone) {
        // Trim phone parameter to handle whitespace
        phone = phone != null ? phone.trim() : phone;

        String otp = String.valueOf((int)(Math.random() * 900000) + 100000);
        otpStorage.put(phone, otp);
        System.out.println("=== OTP GENERATED ===");
        return otp;
    }

    public boolean validateOtp(String phone, String otp) {
        // Show raw input first
        System.out.println("=== RAW INPUT DEBUG ===");
        System.out.println("Raw phone: '" + phone + "' (length: " + (phone != null ? phone.length() : "null") + ")");
        System.out.println("Raw OTP: '" + otp + "' (length: " + (otp != null ? otp.length() : "null") + ")");

        // Trim both parameters to handle whitespace
        phone = phone != null ? phone.trim() : phone;
        otp = otp != null ? otp.trim() : otp;

        String storedOtp = otpStorage.get(phone);
        boolean isValid = otp != null && otp.equals(storedOtp);
        return isValid;
    }

    public void clearOtp(String phone) {
        otpStorage.remove(phone);
    }

}
