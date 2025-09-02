package com.tandem.otp_auth_service.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SmsService {
    @Value("${twilio.accountSid}")
    private String accountSid;
    @Value("${twilio.authToken}")
    private String authToken;
    @Value("${twilio.phoneNumber}")
    private String twilioNumber;

    @PostConstruct
    public void init() {
        try {
            if (accountSid == null || accountSid.isEmpty() ||
                authToken == null || authToken.isEmpty() ||
                twilioNumber == null || twilioNumber.isEmpty()) {
                return;
            }
            Twilio.init(accountSid, authToken);
            System.out.println("Twilio initialized successfully with number: " + twilioNumber);
        } catch (Exception e) {
            System.err.println("Failed to initialize Twilio: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void sendOtp(String toPhone, String otp) {
        try {
            if (accountSid == null || accountSid.isEmpty()) {
                throw new RuntimeException("Twilio not properly configured - missing credentials");
            }

            Message message = Message.creator(
                    new PhoneNumber(toPhone),
                    new PhoneNumber(twilioNumber),
                    "Your OTP is: " + otp
            ).create();

            System.out.println("SMS sent successfully. Message SID: " + message.getSid());
        } catch (com.twilio.exception.ApiException e) {
            // Capture specific Twilio API errors
            String errorMessage = String.format("Twilio API Error - Code: %d, Message: %s, Details: %s",
                e.getCode(), e.getMessage(), e.getMoreInfo());
            System.err.println("Twilio API Error: " + errorMessage);
            throw new RuntimeException("SMS Failed: " + errorMessage, e);
        } catch (com.twilio.exception.AuthenticationException e) {
            // Capture authentication errors specifically
            String errorMessage = "Twilio Authentication Failed: Invalid Account SID or Auth Token";
            System.err.println("Twilio Auth Error: " + errorMessage);
            throw new RuntimeException("SMS Failed: " + errorMessage, e);
        } catch (Exception e) {
            // Capture any other errors
            String errorMessage = "SMS sending failed: " + e.getMessage();
            System.err.println("SMS Error: " + errorMessage);
            throw new RuntimeException(errorMessage, e);
        }
    }
}
