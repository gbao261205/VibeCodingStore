package com.vibecoding.flowerstore.Model;

import com.google.gson.annotations.SerializedName;

public class Review {
    @SerializedName("id")
    private int id;

    @SerializedName("rating")
    private int rating;

    @SerializedName("comment")
    private String comment;

    @SerializedName("reviewDate")
    private String reviewDate;

    @SerializedName("user")
    private UserReview user;

    public int getRating() { return rating; }
    public String getComment() { return comment; }
    public String getReviewDate() { return reviewDate; }
    public UserReview getUser() { return user; }

    // Class con để hứng object "user" bên trong
    public static class UserReview {
        @SerializedName("fullName")
        private String fullName;

        @SerializedName("avatar")
        private String avatar;

        public String getFullName() { return fullName; }
        public String getAvatar() { return avatar; }
    }
}