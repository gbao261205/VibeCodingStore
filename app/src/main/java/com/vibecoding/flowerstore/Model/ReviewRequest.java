package com.vibecoding.flowerstore.Model;

import com.google.gson.annotations.SerializedName;

public class ReviewRequest {
    // Server yêu cầu key là "productId" -> Phải dùng SerializedName để đảm bảo chính xác
    @SerializedName("productId")
    private int productId;

    @SerializedName("rating")
    private int rating;

    @SerializedName("comment")
    private String comment;

    public ReviewRequest(int productId, int rating, String comment) {
        this.productId = productId;
        this.rating = rating;
        this.comment = comment;
    }

    // Getter & Setter (nếu cần)
}