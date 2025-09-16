package com.tandem.otp_auth_service.controller;

import com.tandem.otp_auth_service.dto.ContactDto;
import com.tandem.otp_auth_service.entity.UserContacts;
import com.tandem.otp_auth_service.repository.UserContactsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/contact")
public class ContactController {

    @Autowired
    private UserContactsRepository userContactsRepository;

    @PostMapping("/bulk-save")
    public ResponseEntity<?> saveUserContacts(@RequestParam String userId, @RequestBody List<ContactDto> contacts) {
        List<UserContacts> entities = contacts.stream().map(dto -> {
            UserContacts uc = new UserContacts();
            uc.setUserId(userId);
            uc.setName(dto.getName());
            uc.setPhoneNum(dto.getPhoneNum());
            uc.setEmail(dto.getEmail());
            uc.setAddress(dto.getAddress());
            uc.setOrganization(dto.getOrganization());
            uc.setWebsites(dto.getWebsites());
            uc.setSocialMedia(dto.getSocialMedia());
            uc.setEvents(dto.getEvents());
            uc.setRelations(dto.getRelations());
            uc.setNotes(dto.getNotes());
            uc.setPhotos(dto.getPhotos());

            return uc;
        }).collect(Collectors.toList());
        if (entities.isEmpty()) {
            return ResponseEntity.ok(
                java.util.Map.of(
                    "statusId", "2",
                    "status", "Failed to save contacts. Please provide valid contact data."
                )
            );
        }
        userContactsRepository.saveAll(entities);
        int totalCount = entities.size();
        return ResponseEntity.ok(
            java.util.Map.of(
                "statusId", "1",
                "status", totalCount + " saved successfully"
            )
        );
    }

    @GetMapping("/list")
    public ResponseEntity<?> getContactsByUserId(@RequestParam String userId) {
        List<UserContacts> contacts = userContactsRepository.findByUserId(userId);
        List<java.util.Map<String, Object>> result = contacts.stream()
            .map(c -> java.util.Map.of(
                "name", c.getName(),
                "phoneNum", c.getPhoneNum(),
                "photo", c.getPhotos()
            ))
            .toList();
        int totalCount = result.size();
        String statusMsg = totalCount > 0 ? totalCount + " contacts found" : "No contacts found";
        String statusId = totalCount > 0 ? "1" : "2";
        return ResponseEntity.ok(
            java.util.Map.of(
                "statusId", statusId,
                "status", statusMsg,
                "contacts", result
            )
        );
    }

}
