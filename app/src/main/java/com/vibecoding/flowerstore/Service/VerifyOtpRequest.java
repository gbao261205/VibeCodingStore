package com.vibecoding.flowerstore.Service;

public class VerifyOtpRequest {
    private String otp;
    private String email;

    public VerifyOtpRequest(String email, String otp) {
        this.otp = otp;
        this.email = email;
    }
}
