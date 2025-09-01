package com.tandem.otp_auth_service.controller;

import com.tandem.otp_auth_service.entity.User;
import com.tandem.otp_auth_service.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserRepository userRepository;

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

    @PutMapping("/{phoneNumber}")
    public ResponseEntity<?> updateUser(@PathVariable String phoneNumber, @RequestBody User userUpdate) {
        String normalizedPhone = phoneNumber != null ? phoneNumber.replaceFirst("^\\+", "") : null;
        Optional<User> existingUser = userRepository.findByPhoneNo(normalizedPhone);
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
                .body("User with phone number " + phoneNumber + " not found");
    }
}
