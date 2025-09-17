package com.tandem.otp_auth_service.entity;

import com.tandem.otp_auth_service.utility.IdGenerator;
import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "user_contacts")
public class UserContacts {

    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            this.id = IdGenerator.generateRandomId(12);
        }
    }
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;
    private String userId;
    private String name;
    private String phoneNum;
    private String email;
    private String address;
    private String organization;

    @ElementCollection
    private List<String> websites;

    @ElementCollection
    private List<String> socialMedia;

    @ElementCollection
    private List<String> events;

    @ElementCollection
    private List<String> relations;

    @Column(length = 2000)
    private String notes;

    @ElementCollection
    private List<String> photos;

    public UserContacts() {
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhoneNum() { return phoneNum; }
    public void setPhoneNum(String phoneNum) { this.phoneNum = phoneNum; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getOrganization() { return organization; }
    public void setOrganization(String organization) { this.organization = organization; }

    public List<String> getWebsites() { return websites; }
    public void setWebsites(List<String> websites) { this.websites = websites; }

    public List<String> getSocialMedia() { return socialMedia; }
    public void setSocialMedia(List<String> socialMedia) { this.socialMedia = socialMedia; }

    public List<String> getEvents() { return events; }
    public void setEvents(List<String> events) { this.events = events; }

    public List<String> getRelations() { return relations; }
    public void setRelations(List<String> relations) { this.relations = relations; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public List<String> getPhotos() { return photos; }
    public void setPhotos(List<String> photos) { this.photos = photos; }

    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }
}

