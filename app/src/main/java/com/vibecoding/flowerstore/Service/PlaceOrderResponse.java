package com.vibecoding.flowerstore.Service;

import com.google.gson.annotations.SerializedName;

public class PlaceOrderResponse {
    @SerializedName("orderId")
    private int orderId;
    
    @SerializedName("status")
    private String status;
    
    @SerializedName("totalAmount")
    private double totalAmount;
    
    @SerializedName("message")
    private String message;
    
    @SerializedName("paymentUrl")
    private String paymentUrl;

    public int getOrderId() { return orderId; }
    public String getStatus() { return status; }
    public double getTotalAmount() { return totalAmount; }
    public String getMessage() { return message; }
    public String getPaymentUrl() { return paymentUrl; }
}
