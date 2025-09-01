package com.tandem.otp_auth_service.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {
    private final Map<String, String> otpStorage = new ConcurrentHashMap<>();

    public String generateOtp(String phone) {
        String otp = String.valueOf((int)(Math.random() * 900000) + 100000);
        otpStorage.put(phone, otp);
        return otp;
    }

    public boolean validateOtp(String phone, String otp) {
        return otp.equals(otpStorage.get(phone));
    }

    public void clearOtp(String phone) {
        otpStorage.remove(phone);
    }
}
