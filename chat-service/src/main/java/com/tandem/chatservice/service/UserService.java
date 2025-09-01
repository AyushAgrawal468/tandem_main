package com.tandem.chatservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;
import java.util.Map;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final RestTemplate restTemplate;

    // This will use service discovery to find otp-auth-service
    private static final String USER_SERVICE_URL = "http://otp-auth-service/user";

    public UserService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String getUserName(String phoneNumber) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> user = restTemplate.getForObject(
                USER_SERVICE_URL + "/" + phoneNumber,
                Map.class
            );

            if (user != null && user.get("name") != null) {
                return (String) user.get("name");
            }

            // Fallback to showing last 4 digits if no name
            return "User " + phoneNumber.substring(Math.max(0, phoneNumber.length() - 4));

        } catch (HttpClientErrorException.NotFound e) {
            log.warn("User not found for phone: {}", phoneNumber);
            return "User " + phoneNumber.substring(Math.max(0, phoneNumber.length() - 4));
        } catch (Exception e) {
            log.error("Error fetching user details for phone: {}", phoneNumber, e);
            return "Unknown User";
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getUserDetails(String phoneNumber) {
        try {
            return restTemplate.getForObject(
                USER_SERVICE_URL + "/" + phoneNumber,
                Map.class
            );
        } catch (Exception e) {
            log.error("Error fetching user details for phone: {}", phoneNumber, e);
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getUsersBatch(List<String> phoneNumbers) {
        try {
            String phoneParams = String.join(",", phoneNumbers);
            return restTemplate.getForObject(
                USER_SERVICE_URL + "/batch?phoneNumbers=" + phoneParams,
                List.class
            );
        } catch (Exception e) {
            log.error("Error fetching batch user details", e);
            return List.of();
        }
    }
}
