package com.vibecoding.flowerstore.Model;

import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;

public class Product {

    @SerializedName("id")
    private Integer id;

    @SerializedName("name")
    private String name;

    @SerializedName("price")
    private BigDecimal price;

    @SerializedName("imageUrl")
    private String imageUrl;

    // Các trường này có thể null nếu sản phẩm không có khuyến mãi
    @SerializedName("discountedPrice")
    private BigDecimal discountedPrice;

    @SerializedName("activeDiscount")
    private ActiveDiscount activeDiscount;

    // Constructor, Getters và Setters
    public Product() {}

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public BigDecimal getDiscountedPrice() {
        return discountedPrice;
    }

    public void setDiscountedPrice(BigDecimal discountedPrice) {
        this.discountedPrice = discountedPrice;
    }

    public ActiveDiscount getActiveDiscount() {
        return activeDiscount;
    }

    public void setActiveDiscount(ActiveDiscount activeDiscount) {
        this.activeDiscount = activeDiscount;
    }
}
