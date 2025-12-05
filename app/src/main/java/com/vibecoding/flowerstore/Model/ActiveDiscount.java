package com.vibecoding.flowerstore.Model;


import com.google.gson.annotations.SerializedName;

public class ActiveDiscount {

    @SerializedName("discountPercentage")
    private int discountPercentage;

    public int getDiscountPercentage() {
        return discountPercentage;
    }

    public void setDiscountPercentage(int discountPercentage) {
        this.discountPercentage = discountPercentage;
    }
}
