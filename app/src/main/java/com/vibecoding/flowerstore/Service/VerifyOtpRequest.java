package com.vibecoding.flowerstore.Service;

public class VerifyOtpRequest {
    private String username;
    private String otpCode;

    public VerifyOtpRequest(String username, String otpCode) {
        this.username = username;
        this.otpCode = otpCode;
    }
}
