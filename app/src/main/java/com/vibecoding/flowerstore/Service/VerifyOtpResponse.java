package com.vibecoding.flowerstore.Service;

import com.google.gson.annotations.SerializedName;

public class VerifyOtpResponse {
    @SerializedName("message")
    private String message;

    // Getter
    public String getMessage() {
        return message;
    }

    // Setter (Nên có)
    public void setMessage(String message) {
        this.message = message;
    }
}