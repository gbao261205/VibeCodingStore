package com.vibecoding.flowerstore.Model;

import java.io.Serializable;
import com.vibecoding.flowerstore.Model.RoleDTO; // Import tường minh để fix lỗi cannot find symbol

public class UserDTO implements Serializable {
    private Integer id;
    private String username;
    private String email;
    private String fullName;
    private String phoneNumber;
    private String avatar;
    private boolean active;
    private RoleDTO role;
    private String createdAt;

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public RoleDTO getRole() { return role; }
    public void setRole(RoleDTO role) { this.role = role; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
