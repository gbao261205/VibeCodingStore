package com.vibecoding.flowerstore.Service;

import com.google.gson.annotations.SerializedName;

public class VerifyOtpRequest {
    @SerializedName("username")
    private String username;

    @SerializedName("otpCode")
    private String otpCode;

    // Constructor này RẤT TỐT, giữ nguyên để dễ tạo object
    public VerifyOtpRequest(String username, String otpCode) {
        this.username = username;
        this.otpCode = otpCode;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getOtpCode() { return otpCode; }
    public void setOtpCode(String otpCode) { this.otpCode = otpCode; }
}
