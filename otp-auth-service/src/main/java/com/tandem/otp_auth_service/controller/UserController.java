package com.tandem.otp_auth_service.controller;

import com.tandem.otp_auth_service.dto.OnboardDto;
import com.tandem.otp_auth_service.dto.OtpRequest;
import com.tandem.otp_auth_service.entity.User;
import com.tandem.otp_auth_service.entity.UserPackages;
import com.tandem.otp_auth_service.repository.UserPackageRepository;
import com.tandem.otp_auth_service.repository.UserRepository;
import com.tandemit.security.AESUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/user")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserRepository userRepository;

    @Autowired
    private UserPackageRepository userPackageRepository;

    @Value("${security.key}")
    private String secretKey;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/{phoneNumber}")
    public ResponseEntity<User> getUserByPhone(@PathVariable String phoneNumber) throws Exception {

        phoneNumber = AESUtil.decrypt(phoneNumber, secretKey, String.class);

        String normalizedPhone = phoneNumber != null ? phoneNumber.replaceFirst("^\\+", "") : null;
        Optional<User> user = userRepository.findById(normalizedPhone);
        return user.map(ResponseEntity::ok)
                  .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/batch")
    public ResponseEntity<List<User>> getUsersByPhones(@RequestParam List<String> phoneNumbers) {
        List<User> users = userRepository.findAllById(phoneNumbers);
        return ResponseEntity.ok(users);
    }

    @PutMapping("/{userId}")
    public ResponseEntity<?> updateUser(@PathVariable String userId, @RequestBody User userUpdate) {
        Optional<User> existingUser = userRepository.findById(userId);
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            if (userUpdate.getName() != null) {
                user.setName(userUpdate.getName());
            }
            if (userUpdate.getLocation() != null) {
                user.setLocation(userUpdate.getLocation());
            }
            if (userUpdate.getGender() != null) {
                user.setGender(userUpdate.getGender());
            }
            if (userUpdate.getTags() != null) {
                user.setTags(userUpdate.getTags());
            }
            User savedUser = userRepository.save(user);
            return ResponseEntity.ok(savedUser);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("User with phone number " + userId + " not found");
    }

    @GetMapping("/onboarding-status")
    public ResponseEntity<?> getUserOnboardingStatus(@RequestBody String userId) {
        try {
            userId = AESUtil.decrypt(userId, secretKey, String.class);
        }catch(Exception ex){
            logger.error("Error while decrypting userId: ", ex);
        }
        Optional<UserPackages> userPackage= userPackageRepository.findByUserId(userId);
        return userPackage.<ResponseEntity<?>>map(userPackages -> {

            Map<?,?> response =    Map.of(
                    "status", "Onboarding status fetched successfully",
                    "statusId", "1",
                    "user_id", userPackages.getUserId() != null ? userPackages.getUserId() : "generated",
                    "onboarding_status", userPackages.getOnBoardingStatus()
            );
            String encryptedResponse =null;
            try {
                encryptedResponse = AESUtil.encrypt(response, secretKey);
            }catch(Exception ex){
                logger .error("Error while encrypting response: ", ex);
            }
            return ResponseEntity.ok().body(encryptedResponse);

        }
        ).orElseGet(() ->{
          Map<?,?> response =    Map.of(
                    "status", "Failed to fetch onboarding status",
                    "statusId", "2",
                    "user_id", "",
                    "onboarding_status", ""
            );
            String encryptedResponse = null;
            try {
                encryptedResponse = AESUtil.encrypt(response, secretKey);
            } catch (Exception e) {
                logger.error("Error while encrypting response: ", e);
            }
            return   ResponseEntity.ok().body(encryptedResponse);
        });
    }

    @PutMapping("/onboard")
    public ResponseEntity<?> onboardUser(@RequestBody String onboardDto) {
        OnboardDto onboardDtoDecrypted = null;
        try {
            onboardDtoDecrypted = AESUtil.decrypt(onboardDto, secretKey, OnboardDto.class);
        } catch (Exception e) {
            logger.error("Error while decrypting onboardDto: ", e);
        }

        if (onboardDtoDecrypted == null || onboardDtoDecrypted.getUserId() == null) {
            logger.warn("[onboardUser] UserId is mandatory but missing.");

            Map<?, ?> response = Map.of(
                    "status", "UserId is mandatory",
                    "statusId", "3"
            );
            String encryptedResponse = encryptResponse(response);
            return ResponseEntity.status(HttpStatus.OK).body(encryptedResponse);
        }

        String userId = onboardDtoDecrypted.getUserId();
        logger.info("[onboardUser] Called for userId: {}", userId);

        User existingUser = userRepository.findByUserId(userId).orElse(null);
        UserPackages userPackage = userPackageRepository.findByUserId(userId).orElse(null);

        if (existingUser == null || userPackage == null) {
            logger.warn("[onboardUser] User or UserPackage not found for userId: {}", userId);

            Map<?, ?> response = Map.of(
                    "status", "UserId is not valid",
                    "statusId", "2"
            );
            String encryptedResponse = encryptResponse(response);
            return ResponseEntity.status(HttpStatus.OK).body(encryptedResponse);
        }

        int currentStatus = Integer.parseInt(userPackage.getOnBoardingStatus());
        boolean isUpdated = updateUserDetails(existingUser, onboardDtoDecrypted);

        if (isUpdated && currentStatus == 2) {
            userRepository.save(existingUser);
            userPackage.setOnBoardingStatus(String.valueOf(currentStatus + 1));
            logger.info("[onboardUser] Incremented onboarding status.");
        }

        updateUserPackage(userPackage, onboardDtoDecrypted, currentStatus);
        userPackageRepository.save(userPackage);
        logger.info("[onboardUser] Saved UserPackage for userId: {}", userId);

        Map<?, ?> response = Map.of(
                "status", "User onboarded successfully",
                "statusId", "1"
        );
        String encryptedResponse = encryptResponse(response);
        return ResponseEntity.ok().body(encryptedResponse);
    }

    /**
     * Update User entity details if available in DTO.
     */
    private boolean updateUserDetails(User existingUser, OnboardDto dto) {
        boolean updated = false;

        if (dto.getUserName() != null) {
            existingUser.setName(dto.getUserName());
            updated = true;
            logger.info("[onboardUser] Updated userName: {}", dto.getUserName());
        }
        if (dto.getGender() != null) {
            existingUser.setGender(dto.getGender());
            updated = true;
            logger.info("[onboardUser] Updated gender: {}", dto.getGender());
        }
        return updated;
    }

    /**
     * Update UserPackage details depending on current status.
     */
    private void updateUserPackage(UserPackages userPackage, OnboardDto dto, int currentStatus) {
        if (dto.getLocPermission() != null && currentStatus == 1) {
            userPackage.setLocPermission(dto.getLocPermission());
            if (dto.getLatitude() != null && dto.getLongitude() != null) {
                userPackage.setLatitude(dto.getLatitude());
                userPackage.setLongitude(dto.getLongitude());
                logger.info("[onboardUser] Updated latitude: {} and longitude: {}", dto.getLatitude(), dto.getLongitude());
            }
            userPackage.setOnBoardingStatus(String.valueOf(currentStatus + 1));
            logger.info("[onboardUser] Updated locPermission: {}", dto.getLocPermission());
        }

        if (dto.getReferralSource() != null && currentStatus == 3) {
            userPackage.setReferralSource(dto.getReferralSource());
            userPackage.setOnBoardingStatus(String.valueOf(currentStatus + 1));
            logger.info("[onboardUser] Updated referralSource: {}", dto.getReferralSource());
        }

        if (dto.getContactPermission() != null && currentStatus == 4) {
            userPackage.setContactPermission(dto.getContactPermission());
            userPackage.setOnBoardingStatus(String.valueOf(currentStatus + 1));
            logger.info("[onboardUser] Updated contactPermission: {}", dto.getContactPermission());
        }
    }

    /**
     * Encrypts the response and logs in case of error.
     */
    private String encryptResponse(Map<?, ?> response) {
        String encryptedResponse = null;
        try {
            encryptedResponse = AESUtil.encrypt(response, secretKey);
        } catch (Exception e) {
            logger.error("Error while encrypting response: ", e);
        }
        return encryptedResponse;
    }



}
