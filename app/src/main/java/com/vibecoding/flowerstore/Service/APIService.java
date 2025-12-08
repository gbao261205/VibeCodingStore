package com.vibecoding.flowerstore.Service;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface APIService {
    // ================== 1. AUTHENTICATION ==================
    @POST("auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

//    @POST("auth/logout")
//    Call<MessageResponse> logout();
//
//    @GET("auth/validate")
//    Call<ValidateResponse> validateToken();
//
//    @GET("auth/check-cookie")
//    Call<CheckCookieResponse> checkCookie();
//
//    @POST("auth/refresh")
//    Call<RefreshTokenResponse> refreshToken();
//
    @POST("auth/register")
    Call<RegisterResponse> register(@Body RegisterRequest request);

    @POST("auth/verify-otp")
    Call<VerifyOtpResponse> verifyOtp(@Body VerifyOtpRequest request);

    @POST("auth/resend-otp")
    Call<ResendOtpResponse> resendOtp(@Body ResendOtpRequest request);

    @POST("auth/forgot-password")
    Call<ForgotPasswordResponse> forgotPassword(@Body ForgotPasswordRequest request);
//
//    @POST("auth/reset-password-verify")
//    Call<MessageResponse> verifyResetPasswordOtp(@Body VerifyOtpRequest request);
//
//    @POST("auth/reset-password-new")
//    Call<MessageResponse> resetPassword(@Body ResetPasswordRequest request);
//
//    @POST("auth/resend-reset-otp")
//    Call<MessageResponse> resendResetOtp(@Body ForgotPasswordRequest request);
//
//    // ================== 2. PRODUCTS ==================
//    @GET("home/products")
//    Call<Map<String, List<ProductDTO>>> getHomeProducts();
//
//    @GET("products/category/{categorySlug}")
//    Call<PageResponse<ProductDTO>> getProductsByCategory(
//            @Path("categorySlug") String categorySlug,
//            @Query("page") Integer page,
//            @Query("size") Integer size,
//            @Query("sort") String sort
//    );
//
//    @GET("products")
//    Call<PageResponse<ProductDTO>> getProducts(
//            @Query("query") String query,
//            @Query("page") Integer page,
//            @Query("size") Integer size,
//            @Query("sort") String sort
//    );
//
//    @GET("home/products/{id}")
//    Call<ProductDetailResponse> getProductDetail(@Path("id") int id);
//
//    // ================== 3. CART ==================
//    @GET("cart")
//    Call<CartDTO> getCart();
//
//    @POST("cart/add")
//    Call<CartDTO> addToCart(
//            @Query("productId") int productId,
//            @Query("quantity") int quantity
//    );
//
//    @PUT("cart/update-quantity")
//    Call<CartDTO> updateCartQuantity(
//            @Query("productId") int productId,
//            @Query("quantity") int quantity
//    );
//
//    @POST("cart/remove")
//    Call<CartDTO> removeFromCart(@Query("productId") int productId);
//
//    // ================== 4. ORDERS ==================
//    @GET("orders/checkout-details")
//    Call<CheckoutDetailsResponse> getCheckoutDetails();
//
//    @POST("orders/place-order")
//    Call<PlaceOrderResponse> placeOrder(@Body PlaceOrderRequest request);
//
//    @GET("orders/history")
//    Call<List<OrderDTO>> getOrderHistory(@Query("status") String status);
//
//    // ================== 5. ADDRESSES ==================
//    @GET("addresses")
//    Call<List<AddressDTO>> getAddresses();
//
//    @GET("addresses/{id}")
//    Call<AddressDTO> getAddressById(@Path("id") int id);
//
//    @POST("addresses")
//    Call<MessageResponse> createAddress(@Body AddressRequest request);
//
//    @PUT("addresses/{id}")
//    Call<MessageResponse> updateAddress(@Path("id") int id, @Body AddressRequest request);
//
//    @DELETE("addresses/{id}")
//    Call<MessageResponse> deleteAddress(@Path("id") int id);
//
//    // ================== 6. REVIEWS ==================
//    @GET("reviews/product/{productId}/form-details")
//    Call<ProductDTO> getReviewFormDetails(@Path("productId") int productId);
//
//    @POST("reviews")
//    Call<MessageResponse> submitReview(@Body ReviewRequest request);
//
//    // ================== 7. SHOPS ==================
//    @GET("shop/register-status")
//    Call<MessageResponse> getShopRegisterStatus();
//
//    @POST("shop/register")
//    Call<MessageResponse> registerShop(@Body ShopRegisterRequest request);
//
//    @GET("shop/{shopId}")
//    Call<ShopDetailResponse> getShopDetail(@Path("shopId") int shopId);
//
//    // ================== 8. PAYMENT ==================
//    @GET("payment/vnpay-return")
//    Call<PaymentReturnResponse> vnpayReturn(@QueryMap Map<String, String> vnpParams);
//
//    // ================== 9. SHIPPER ==================
//    @GET("shipper/dashboard")
//    Call<ShipperDashboardResponse> getShipperDashboard();
//
//    @GET("shipper/orders")
//    Call<List<OrderDTO>> getShipperAssignedOrders();
//
//    @GET("shipper/available-orders")
//    Call<List<OrderDTO>> getShipperAvailableOrders();
//
//    @POST("shipper/accept-order/{orderId}")
//    Call<MessageResponse> acceptOrder(@Path("orderId") int orderId);
//
//    @POST("shipper/orders/{orderId}/update-status")
//    Call<MessageResponse> updateOrderStatus(
//            @Path("orderId") int orderId,
//            @Query("status") String status
//    );
//
//    // ================== 10. ADMIN ==================
//    @GET("admin/dashboard")
//    Call<AdminDashboardResponse> getAdminDashboard();
//
//    @GET("admin/orders")
//    Call<List<OrderDTO>> getAllOrders();
//
//    @GET("admin/products")
//    Call<List<ProductDTO>> getAllProducts(@Query("keyword") String keyword);
//
//    @POST("admin/products/toggle-visibility/{id}")
//    Call<MessageResponse> toggleProductVisibility(@Path("id") int id);
}
