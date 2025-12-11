package com.vibecoding.flowerstore.Model;

import com.google.gson.annotations.SerializedName;

// This class represents the User object returned by the API
public class User {

    @SerializedName("id")
    private int id;

    @SerializedName("username")
    private String username;

    @SerializedName("email")
    private String email;

    @SerializedName("fullName")
    private String fullName;

    @SerializedName("phoneNumber")
    private String phoneNumber;

    @SerializedName("avatar")
    private String avatar;

    @SerializedName("active")
    private boolean active;

    // --- Getters ---

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    // We will use this to display the user's name
    public String getFullName() {
        return fullName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getAvatar() {
        return avatar;
    }

    public boolean isActive() {
        return active;
    }
}
