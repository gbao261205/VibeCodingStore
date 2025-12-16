package com.vibecoding.flowerstore.Service;

import com.google.gson.annotations.SerializedName;

public class EditProfileRequest {
    @SerializedName("fullName")
    private String fullName;

    @SerializedName("phoneNumber")
    private String phoneNumber;

    public EditProfileRequest(String fullName, String phoneNumber) {
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
    }
}
