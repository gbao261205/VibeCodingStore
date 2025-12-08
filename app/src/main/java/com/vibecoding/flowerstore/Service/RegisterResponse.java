package com.vibecoding.flowerstore.Service;

public class RegisterResponse {
    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public RegisterResponse(String message, Boolean success) {
        this.message = message;
    }
}
