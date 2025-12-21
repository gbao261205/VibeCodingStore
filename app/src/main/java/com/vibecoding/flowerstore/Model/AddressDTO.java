package com.vibecoding.flowerstore.Model;

import java.io.Serializable;

public class AddressDTO implements Serializable {
    private Integer id;
    private String recipientName;
    private String phoneNumber;
    private String detailAddress;
    private String city;
    private boolean isDefault;

    // Constructors
    public AddressDTO() {}

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getRecipientName() { return recipientName; }
    public void setRecipientName(String recipientName) { this.recipientName = recipientName; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getDetailAddress() { return detailAddress; }
    public void setDetailAddress(String detailAddress) { this.detailAddress = detailAddress; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public boolean isDefault() { return isDefault; }
    public void setDefault(boolean aDefault) { isDefault = aDefault; }

    // Helper để hiển thị địa chỉ đầy đủ
    public String getFullAddress() {
        return detailAddress + ", " + city;
    }
}