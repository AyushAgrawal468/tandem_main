package com.tandem.otp_auth_service.entity;

import com.tandem.otp_auth_service.utility.IdGenerator;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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
    @Column(length = 12, nullable = false, unique = true)
    private String id;  // Custom random ID

    private String userId;
    private String name;
    private String phoneNum;
    private String email;
    private String address;
    private String organization;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "websites", columnDefinition = "text[]")
    private List<String> websites;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "social_media", columnDefinition = "text[]")
    private List<String> socialMedia;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "events", columnDefinition = "text[]")
    private List<String> events;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "relations", columnDefinition = "text[]")
    private List<String> relations;

    @Column(length = 2000)
    private String notes;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "photos", columnDefinition = "text[]")
    private List<String> photos;

    public UserContacts() {}

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

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
}
