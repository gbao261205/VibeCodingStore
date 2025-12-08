package com.vibecoding.flowerstore.Model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class CartDTO {

    @SerializedName("cartId")
    private int cartId;

    @SerializedName("items")
    private List<CartItem> items;

    @SerializedName("totalAmount")
    private double totalAmount;

    // --- Getters ---

    public int getCartId() {
        return cartId;
    }

    public List<CartItem> getItems() {
        return items;
    }

    public double getTotalAmount() {
        return totalAmount;
    }
}
