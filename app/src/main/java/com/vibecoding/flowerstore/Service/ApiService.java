package com.vibecoding.flowerstore.Service;

import com.vibecoding.flowerstore.Model.AddressDTO;
import com.vibecoding.flowerstore.Model.ApiResponse;
import com.vibecoding.flowerstore.Model.CartDTO;
import com.vibecoding.flowerstore.Model.Category;
import com.vibecoding.flowerstore.Model.OrderDTO;
import com.vibecoding.flowerstore.Model.Product;
import com.vibecoding.flowerstore.Model.ProductDTO;
import com.vibecoding.flowerstore.Model.ProductReviewDTO;
import com.vibecoding.flowerstore.Model.Review;
import com.vibecoding.flowerstore.Model.ReviewRequest;
import com.vibecoding.flowerstore.Model.ReviewResponse;
import com.vibecoding.flowerstore.Model.ShopDTO;
import com.vibecoding.flowerstore.Model.User;
// Import các class Request/Response từ Service
import com.vibecoding.flowerstore.Service.CheckoutDetailsResponse;
import com.vibecoding.flowerstore.Service.PlaceOrderRequest;
import com.vibecoding.flowerstore.Service.PlaceOrderResponse;
// Import Shipper Response
import com.vibecoding.flowerstore.Service.ShipperDashboardResponse;
import com.vibecoding.flowerstore.Model.UserDTO;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
    /**
     * Lấy danh sách sản phẩm với đầy đủ các tùy chọn lọc và sắp xếp.
     * @param query Từ khóa tìm kiếm (có thể rỗng)
     * @param page Số trang (có thể rỗng)
     * @param size Số lượng trên trang (có thể rỗng)
     * @param sortBy Cách sắp xếp
     * @return Call<ApiResponse>
     */
    @GET("products")
    Call<ApiResponse> getProducts(
            @Query("query") String query,
            @Query("page") int page,  // Dùng String để có thể truyền rỗng
            @Query("size") int size,  // Dùng String để có thể truyền rỗng
            @Query("sort") String sortBy
    );
    @GET("categories")
    Call<List<Category>> getCategories();
    @GET("products")
    Call<ApiResponse> getWishlistedProducts(
            @Query("sort") String sort,  // Truyền "wishlisted" vào đây
            @Query("page") int page,     // Mặc định 0
            @Query("size") int size      // Mặc định 20
    );
    @GET("reviews/product/{productId}")
    Call<List<Review>> getProductReviews(@Path("productId") int productId);
    // 1. Lấy thông tin form đánh giá
    // Response trả về chính là object Product nên ta tái sử dụng class Product
    @GET("reviews/product/{productId}/form-details")
    Call<ProductReviewDTO> getReviewFormDetails(@Header("Authorization") String token, @Path("productId") int productId);

    // 2. Gửi đánh giá
    @POST("reviews")
    Call<ReviewResponse> saveReview(@Header("Authorization") String token, @Body ReviewRequest request);
    @POST("/wishlist/remove/{id}")
    Call<ResponseBody> removeFromWishlist(@Path("id") int id);
    @POST("/wishlist/add/{id}")
    Call<ResponseBody> addToWishlist(@Path("id") int id);
    @GET("products/category/{categorySlug}")
    Call<ApiResponse> getProductsByCategory(@Path("categorySlug") String slug);

    // ================== 1. AUTHENTICATION ==================
    @POST("auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @GET("profile")
    Call<User> getProfile(@Header("Authorization") String authToken);
    @Multipart
    @PUT("profile") // RetrofitClient base url đã là .../api/v1/ nên ở đây chỉ cần "profile"
    Call<ResponseBody> updateProfile(
            // Part 1: JSON Object (tên param là "request")
            @Part("request") RequestBody requestBody,

            // Part 2: File ảnh (tên param là "avatarFile", có thể null)
            @Part MultipartBody.Part avatarFile
    );
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

    @POST("auth/reset-password-verify")
    Call<ResetPasswordVerifyResponse> verifyResetPasswordOtp(@Body ResetPasswordVerifyRequest request);

    @POST("auth/reset-password-new")
    Call<ChangePasswordResponse> changePassword(@Body ChangePasswordRequest request);

    @POST("auth/resend-reset-otp")
    Call<ResendOtpResponse> resendResetOtp(@Body ResendOtpForgotPassRequest request);
//
//    // ================== 2. PRODUCTS ==================
    @GET("home/products")
    Call<Map<String, List<ProductDTO>>> getHomeProducts();
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
    @GET("cart")
    Call<CartDTO> getCart(@Header("Authorization") String authToken);

    @POST("cart/add")
    Call<CartDTO> addToCart(
            @Header("Authorization") String authToken,
            @Query("productId") int productId,
            @Query("quantity") int quantity
    );

    @PUT("cart/update-quantity")
    Call<CartDTO> updateCartQuantity(
            @Header("Authorization") String authToken,
            @Query("productId") int productId,
            @Query("quantity") int quantity
    );

    @POST("cart/remove")
    Call<CartDTO> removeFromCart(
            @Header("Authorization") String authToken,
            @Query("productId") int productId
    );

    // ================== 4. ORDERS ==================
    @GET("orders/checkout-details")
    Call<CheckoutDetailsResponse> getCheckoutDetails(@Header("Authorization") String authToken);

    @POST("orders/place-order")
    Call<PlaceOrderResponse> placeOrder(@Header("Authorization") String authToken, @Body PlaceOrderRequest request);

    @GET("orders/history")
    Call<List<OrderDTO>> getOrderHistory(
            @Header("Authorization") String authToken,
            @Query("status") String status
    );

//    // ================== 5. ADDRESSES ==================
    @GET("addresses")
    Call<List<AddressDTO>> getAddresses(@Header("Authorization") String authToken);

//    @GET("addresses/{id}")
//    Call<AddressDTO> getAddressById(@Path("id") int id);
//
    @POST("addresses")
    Call<MessageResponse> createAddress(@Body AddressDTO request);

    @PUT("addresses/{id}")
    Call<MessageResponse> updateAddress(@Path("id") int id, @Body AddressDTO request);

    @DELETE("addresses/{id}")
    Call<MessageResponse> deleteAddress(@Path("id") int id);
//
//    // ================== 6. REVIEWS ==================
//    @GET("reviews/product/{productId}/form-details")
//    Call<ProductDTO> getReviewFormDetails(@Path("productId") int productId);
//
//    @POST("reviews")
//    Call<MessageResponse> submitReview(@Body ReviewRequest request);
//
//    // ================== 7. SHOPS ==================
    @GET("shop/register-status")
    Call<ResponseBody> getShopRegisterStatus(@Header("Authorization") String authToken);

    @POST("shop/register")
    Call<ResponseBody> registerShop( // Đổi từ MessageResponse sang ResponseBody
            @Header("Authorization") String authToken,
            @Body ShopRegisterRequest request
    );
//
//    @GET("shop/{shopId}")
//    Call<ShopDetailResponse> getShopDetail(@Path("shopId") int shopId);
//
//    // ================== 8. PAYMENT ==================
//    @GET("payment/vnpay-return")
//    Call<PaymentReturnResponse> vnpayReturn(@QueryMap Map<String, String> vnpParams);
//
    // ================== 9. SHIPPER ==================
    @GET("shipper/dashboard")
    Call<ShipperDashboardResponse> getShipperDashboard();

    @GET("shipper/orders")
    Call<List<OrderDTO>> getShipperAssignedOrders();

    @GET("shipper/available-orders")
    Call<List<OrderDTO>> getShipperAvailableOrders();

    @POST("shipper/accept-order/{orderId}")
    Call<MessageResponse> acceptOrder(@Path("orderId") int orderId);

    @POST("shipper/orders/{orderId}/update-status")
    Call<MessageResponse> updateOrderStatus(
            @Path("orderId") int orderId,
            @Query("status") String status
    );
//
//    // ================== 10. ADMIN ==================
    @GET("admin/dashboard")
    Call<AdminDashboardResponse> getAdminDashboard();

    @GET("admin/orders")
    Call<List<OrderDTO>> getAllOrders(@Header("Authorization") String authToken);

    @GET("admin/products")
    Call<List<ProductDTO>> getAllProducts(@Query("keyword") String keyword);

    @GET("admin/shops")
    Call<List<ShopDTO>> getAllShop(@Header("Authorization") String authToken, @Query("keyword") String keyword);

    @GET("admin/users")
    Call<List<UserDTO>> getAllUsers(@Header("Authorization") String authToken);
}
