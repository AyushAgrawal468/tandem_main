package com.tandem.otp_auth_service.dto;

public class OtpRequest {
    private String phone;
    private String countryCode;
    private String appSignature;

    public OtpRequest() {
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }
    public String getAppSignature() {
        return appSignature;
    }
    public void setAppSignature(String appSignature) {
        this.appSignature = appSignature;
    }
}
