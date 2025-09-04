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

        String otp = String.valueOf((int)(Math.random() * 9000) + 1000); // 4-digit OTP
        otpStorage.put(phone, otp);

        // 🔹 Debug logs
        System.out.println("=== OTP GENERATED ===");
        System.out.println("Phone: " + phone + " | OTP: " + otp);
        System.out.println("Current OTP Storage: " + otpStorage);

        return otp;
    }

    public boolean validateOtp(String phone, String otp) {
        System.out.println("=== VALIDATE OTP CALLED ===");

        // Show raw input first
        System.out.println("Raw phone: '" + phone + "' (length: " + (phone != null ? phone.length() : "null") + ")");
        System.out.println("Raw OTP:   '" + otp + "' (length: " + (otp != null ? otp.length() : "null") + ")");

        // Trim both parameters to handle whitespace
        phone = phone != null ? phone.trim() : phone;
        otp = otp != null ? otp.trim() : otp;

        String storedOtp = otpStorage.get(phone);

        // 🔹 Debug logs
        System.out.println("Normalized phone: " + phone);
        System.out.println("Stored OTP: " + storedOtp);
        System.out.println("Entered OTP: " + otp);

        boolean isValid = otp != null && otp.equals(storedOtp);
        System.out.println("Validation Result for phone " + phone + ": " + isValid);

        return isValid;
    }

    public void clearOtp(String phone) {
        phone = phone != null ? phone.trim() : phone;
        otpStorage.remove(phone);

        // 🔹 Debug log
        System.out.println("=== OTP CLEARED ===");
        System.out.println("Phone: " + phone);
        System.out.println("Current OTP Storage: " + otpStorage);
    }
}
