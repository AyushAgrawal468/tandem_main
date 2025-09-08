package com.tandem.otp_auth_service.controller;

import com.tandem.otp_auth_service.dto.OnboardDto;
import com.tandem.otp_auth_service.entity.User;
import com.tandem.otp_auth_service.entity.UserPackages;
import com.tandem.otp_auth_service.repository.UserPackageRepository;
import com.tandem.otp_auth_service.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/{phoneNumber}")
    public ResponseEntity<User> getUserByPhone(@PathVariable String phoneNumber) {
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
        Optional<UserPackages> userPackage= userPackageRepository.findByUserId(userId);
        return userPackage.<ResponseEntity<?>>map(userPackages -> ResponseEntity.ok().body(Map.of(
                        "status", "Onboarding status fetched successfully",
                        "statusId", "1",
                        "user_id", userPackages.getUserId() != null ? userPackages.getUserId() : "generated",
                        "onboarding_status", userPackages.getOnBoardingStatus()
                )
        )).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(
                        Map.of(
                                "status", "Failed to fetch onboarding status",
                                "statusId", "2",
                                "user_id", "",
                                "onboarding_status", ""
                        )
                ));
    }

    @PutMapping("/onboard")
    public ResponseEntity<?> onboardUser(@RequestBody OnboardDto onboardDto) {
        Boolean isUpdated = false;
        if(onboardDto.getUserId()!=null) {
            String userId = onboardDto.getUserId();
            Optional<User> optionalUser = userRepository.findByUserId(userId);
            User existingUser = optionalUser.orElse(null);
            UserPackages userPackage = userPackageRepository.findByUserId(userId).isPresent() ? userPackageRepository.findByUserId(userId).get() : null;
            int currentStatus = 0;

            logger.info("[onboardUser] Called for userId: {}", userId);
            if (existingUser != null && userPackage != null) {
                currentStatus = Integer.parseInt(userPackage.getOnBoardingStatus());
                if(onboardDto.getUserName()!=null){
                    existingUser.setName(onboardDto.getUserName());
                    isUpdated = true;
                    logger.info("[onboardUser] Updated userName: {}", onboardDto.getUserName());
                }
                if(onboardDto.getGender()!=null) {
                    existingUser.setGender(onboardDto.getGender());
                    isUpdated = true;
                    logger.info("[onboardUser] Updated gender: {}", onboardDto.getGender());
                }

                if(isUpdated && currentStatus==2){
                    userRepository.save(existingUser);

                    userPackage.setOnBoardingStatus(String.valueOf(currentStatus + 1));
                    logger.info("[onboardUser] Incremented onboarding status.");
                }

            }else{
                logger.warn("[onboardUser] User or UserPackage not found for userId: {}", userId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(
                                Map.of(
                                        "status","UserId is not valid",
                                        "statusId","2"
                                )
                        );
            }

            if (userPackage != null) {
                if(onboardDto.getLocPermission()!=null && currentStatus==1) {
                    userPackage.setLocPermission(onboardDto.getLocPermission());
                    if(onboardDto.getLatitude()!=null && onboardDto.getLongitude()!=null){
                        userPackage.setLatitude(onboardDto.getLatitude());
                        userPackage.setLongitude(onboardDto.getLongitude());
                        logger.info("[onboardUser] Updated latitude: {} and longitude: {}", onboardDto.getLatitude(), onboardDto.getLongitude());
                    }
                    userPackage.setOnBoardingStatus(String.valueOf(currentStatus + 1));
                    logger.info("[onboardUser] Updated locPermission: {}", onboardDto.getLocPermission());
                }
                if(onboardDto.getReferralSource()!=null && currentStatus==3) {
                    userPackage.setReferralSource(onboardDto.getReferralSource());
                    userPackage.setOnBoardingStatus(String.valueOf(currentStatus + 1));
                    logger.info("[onboardUser] Updated referralSource: {}", onboardDto.getReferralSource());
                }
                if(onboardDto.getContactPermission()!=null && currentStatus==4) {
                    userPackage.setContactPermission(onboardDto.getContactPermission());
                    userPackage.setOnBoardingStatus(String.valueOf(currentStatus + 1));
                    logger.info("[onboardUser] Updated contactPermission: {}", onboardDto.getContactPermission());
                }
            }
            userPackageRepository.save(userPackage);
            logger.info("[onboardUser] Saved UserPackage for userId: {}", userId);
            return ResponseEntity.ok().body(Map.of(
                    "status", "User onboarded successfully",
                    "statusId", "1"
            )
        );
        }
        logger.warn("[onboardUser] UserId is mandatory but missing.");
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(
                        Map.of(
                                "status","UserId is mandatory",
                                "statusId","3"
                        )
                );
    }


}
