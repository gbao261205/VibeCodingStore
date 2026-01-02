package com.vibecoding.flowerstore.Model;

import com.google.gson.annotations.SerializedName;

public class ProductReviewDTO {

    @SerializedName("id")
    private int id;

    @SerializedName("name")
    private String name;

    // JSON trả về "imageUrl" -> Map đúng vào biến này
    @SerializedName("imageUrl")
    private String imageUrl;

    // Dùng Double thay vì double để tránh lỗi nếu server trả về null
    @SerializedName("price")
    private Double price;

    @SerializedName("shop")
    private ShopReviewDTO shop;

    // --- Getters ---
    public int getId() { return id; }
    public String getName() { return name; }
    public String getImageUrl() { return imageUrl; }
    public Double getPrice() { return price; }
    public ShopReviewDTO getShop() { return shop; }

    // Class con để hứng thông tin Shop (chỉ cần lấy tên là đủ)
    public static class ShopReviewDTO {
        @SerializedName("id")
        private int id;

        @SerializedName("name")
        private String name;

        public String getName() { return name; }
    }
}