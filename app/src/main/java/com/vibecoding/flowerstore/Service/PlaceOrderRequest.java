package com.vibecoding.flowerstore.Service;

public class PlaceOrderRequest {
    private int addressId;
    private int shippingCarrierId;
    private String paymentMethod; // "COD", "VNPAY"
    private String notes;
    private String discountCode;
    private double currentCartTotal;

    public PlaceOrderRequest(int addressId, int shippingCarrierId, String paymentMethod, double currentCartTotal) {
        this.addressId = addressId;
        this.shippingCarrierId = shippingCarrierId;
        this.paymentMethod = paymentMethod;
        this.currentCartTotal = currentCartTotal;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getNotes() {
        return notes;
    }

    public void setDiscountCode(String discountCode) {
        this.discountCode = discountCode;
    }
}
