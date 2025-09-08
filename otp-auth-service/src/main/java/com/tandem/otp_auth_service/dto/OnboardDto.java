package com.tandem.otp_auth_service.dto;

public class OnboardDto {

    private String userId;
    private String userName;
    private String gender;
    private Boolean locPermission;
    private String referralSource;
    private Boolean contactPermission;
    private Double latitude;
    private Double longitude;

    public OnboardDto() {
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Boolean getLocPermission() {
        return locPermission;
    }

    public void setLocPermission(Boolean locPermission) {
        this.locPermission = locPermission;
    }

    public String getReferralSource() {
        return referralSource;
    }

    public void setReferralSource(String referralSource) {
        this.referralSource = referralSource;
    }

    public Boolean getContactPermission() {
        return contactPermission;
    }

    public void setContactPermission(Boolean contactPermission) {
        this.contactPermission = contactPermission;
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
