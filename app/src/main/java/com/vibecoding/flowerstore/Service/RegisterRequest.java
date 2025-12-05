package com.vibecoding.flowerstore.Service;

public class RegisterRequest {
    // username, password, full_name, phone, email
    private String username;
    private String password;
    private String email;
    private String fullName;
    private String phoneNumber;

    public RegisterRequest(String username, String password, String email, String fullName, String phoneNumber) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
    }
}
