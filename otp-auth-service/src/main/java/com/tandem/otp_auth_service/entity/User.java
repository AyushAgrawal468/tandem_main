package com.tandem.otp_auth_service.entity;

import com.tandem.otp_auth_service.utility.IdGenerator;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;


@Entity
@Table(name = "tandem_users")
public class User {

    @PrePersist
    public void prePersist() {
        if (this.userId == null) {
            this.userId = IdGenerator.generateRandomId(12);
        }
        if (this.creationDate == null) {
            this.creationDate = LocalDateTime.now();
        }
    }

    @Id
    @Column(length = 15, nullable = false, unique = true)
    private String phoneNo;

    @Column(length = 12, unique = true, nullable = false, updatable = false)
    private String userId;

    @Column(length = 20)
    private String name;

    @ElementCollection
    private List<String> tags;

    private String location;
    private String gender;
    private LocalDateTime creationDate;
    private LocalDateTime lastLogin;

    public User() {
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }

    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }
}
