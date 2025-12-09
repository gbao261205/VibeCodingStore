package com.vibecoding.flowerstore.Service;

public class ResetPasswordVerifyRequest {
    private String email;
    private String otpCode;

    public ResetPasswordVerifyRequest(String email, String otpCode) {
        this.email = email;
        this.otpCode = otpCode;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getOtp() {
        return otpCode;
    }

    public void setOtp(String otpCode) {
        this.otpCode = otpCode;
    }
}
