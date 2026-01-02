package com.vibecoding.flowerstore.Service;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class ShopRegisterRequest implements Serializable {
    @SerializedName("name")
    private String name;

    @SerializedName("description")
    private String description; // Đã sửa từ address -> description

    public ShopRegisterRequest(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}