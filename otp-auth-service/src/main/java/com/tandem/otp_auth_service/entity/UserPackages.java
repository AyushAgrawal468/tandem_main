package com.tandem.otp_auth_service.entity;

import com.tandem.otp_auth_service.utility.IdGenerator;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_packages")
public class UserPackages {

    @PrePersist
    public void prePersist() {
        if (this.userPackageId == null) {
            this.userPackageId = IdGenerator.generateRandomId(12);
        }
        if (this.creationDate == null) {
            this.creationDate = LocalDateTime.now();
        }

        this.modifiedDate = LocalDateTime.now();
    }

    @Id
    @Column(name = "user_package_id")
    private String userPackageId;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "loc_permission")
    private Boolean locPermission;  //Y/N

    @Column(name = "notif_permission")
    private Boolean notifPermission; //Y/N

    @Column(name = "package_type")
    private String packageType;   //free, pro, premium

    @Column(name = "package_status")
    private String packageStatus; //active/inactive

    @Column(name = "package_months")
    private String packageMonths; //1, 3, 6, 12

    @Column(name = "on_boarding_status")
    private String onBoardingStatus; //0,1,2,3,4..

    @Column(name = "creation_date")
    private LocalDateTime creationDate;

    @Column(name = "modified_date")
    private LocalDateTime modifiedDate;

    @Column(name = "contact_permission")
    private Boolean contactPermission; //Y/N

    @Column(name = "referral_source")
    private String referralSource;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    public UserPackages(){

    }
    public String getUserPackageId() {
        return userPackageId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Boolean getLocPermission() {
        return locPermission;
    }

    public void setLocPermission(Boolean locPermission) {
        this.locPermission = locPermission;
    }

    public Boolean getNotifPermission() {
        return notifPermission;
    }

    public void setNotifPermission(Boolean notifPermission) {
        this.notifPermission = notifPermission;
    }

    public String getPackageType() {
        return packageType;
    }

    public void setPackageType(String packageType) {
        this.packageType = packageType;
    }

    public String getPackageStatus() {
        return packageStatus;
    }

    public void setPackageStatus(String packageStatus) {
        this.packageStatus = packageStatus;
    }

    public String getPackageMonths() {
        return packageMonths;
    }

    public void setPackageMonths(String packageMonths) {
        this.packageMonths = packageMonths;
    }

    public String getOnBoardingStatus() {
        return onBoardingStatus;
    }

    public void setOnBoardingStatus(String onBoardingStatus) {
        this.onBoardingStatus = onBoardingStatus;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public LocalDateTime getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(LocalDateTime modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public Boolean getContactPermission() {
        return contactPermission;
    }

    public void setContactPermission(Boolean contactPermission) {
        this.contactPermission = contactPermission;
    }

    public String getReferralSource() {
        return referralSource;
    }

    public void setReferralSource(String referralSource) {
        this.referralSource = referralSource;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
}