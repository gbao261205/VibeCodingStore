package com.vibecoding.flowerstore.Service;

public class VerifyOtpResponse {
    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public VerifyOtpResponse(String message, Boolean success) {
        this.message = message;
    }
}
