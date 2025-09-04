package com.tandem.otp_auth_service.entity;

import com.tandem.otp_auth_service.utility.IdGenerator;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "blocked_users")
public class BlockedUser {

    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            this.id = IdGenerator.generateRandomId(12);
        }
    }

    @Id
    private String id;

    private String phoneNo;

    private Integer resendOtpCount;

    private LocalDateTime blockedAt;

    public BlockedUser() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }

    public Integer getResendOtpCount() {
        return resendOtpCount;
    }

    public void setResendOtpCount(Integer resendOtpCount) {
        this.resendOtpCount = resendOtpCount;
    }

    public LocalDateTime getBlockedAt() {
        return blockedAt;
    }
    public void setBlockedAt(LocalDateTime blockedAt) {
        this.blockedAt = blockedAt;
    }
}
