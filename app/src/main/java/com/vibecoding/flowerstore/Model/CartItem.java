package com.vibecoding.flowerstore.Model;

import com.google.gson.annotations.SerializedName;

public class CartItem {

    @SerializedName("cartItemId")
    private int cartItemId;

    @SerializedName("product")
    private Product product;

    @SerializedName("quantity")
    private int quantity;

    @SerializedName("subtotal")
    private double subtotal;

    // --- Getters ---

    public int getCartItemId() {
        return cartItemId;
    }

    public Product getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getSubtotal() {
        return subtotal;
    }
}
