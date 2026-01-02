package com.vibecoding.flowerstore.Activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.vibecoding.flowerstore.Model.ProductReviewDTO; // Import Model mới
import com.vibecoding.flowerstore.Model.ReviewRequest;
import com.vibecoding.flowerstore.Model.ReviewResponse;
import com.vibecoding.flowerstore.R;
import com.vibecoding.flowerstore.Service.ApiService;
import com.vibecoding.flowerstore.Service.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WriteReviewActivity extends AppCompatActivity {

    private static final String TAG = "WriteReviewActivity"; // Tag để lọc Logcat

    private ImageView btnBack, imgProduct;
    private TextView tvProductName, tvShopName;
    private RatingBar ratingBar;
    private EditText edtComment;
    private Button btnSubmit;

    private int productId;
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_review);

        // 1. Lấy Token và ID sản phẩm từ Intent
        SharedPreferences prefs = getSharedPreferences("MY_APP_PREFS", Context.MODE_PRIVATE);
        token = prefs.getString("ACCESS_TOKEN", null);
        productId = getIntent().getIntExtra("product_id", -1);

        // LOG kiểm tra đầu vào
        Log.d(TAG, "onCreate: ProductID = " + productId);
        Log.d(TAG, "onCreate: Token = " + token);

        if (token == null || productId == -1) {
            Toast.makeText(this, "Lỗi xác thực hoặc không tìm thấy sản phẩm", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();

        // 2. Gọi API lấy thông tin sản phẩm (Dùng ProductReviewDTO)
        loadProductInfo();

        // 3. Xử lý sự kiện gửi
        btnSubmit.setOnClickListener(v -> submitReview());

        btnBack.setOnClickListener(v -> finish());
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back_review);
        imgProduct = findViewById(R.id.img_review_product);
        tvProductName = findViewById(R.id.tv_review_product_name);
        tvShopName = findViewById(R.id.tv_review_shop_name);
        ratingBar = findViewById(R.id.rb_review_star);
        edtComment = findViewById(R.id.edt_review_comment);
        btnSubmit = findViewById(R.id.btn_submit_review);
    }

    private void loadProductInfo() {
        Log.d(TAG, "Đang tải thông tin form đánh giá...");

        ApiService apiService = RetrofitClient.getClient(this).create(ApiService.class);

        // Gọi API form-details hứng về ProductReviewDTO
        apiService.getReviewFormDetails("Bearer " + token, productId).enqueue(new Callback<ProductReviewDTO>() {
            @Override
            public void onResponse(Call<ProductReviewDTO> call, Response<ProductReviewDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ProductReviewDTO product = response.body();

                    // Log kiểm tra dữ liệu
                    Log.d(TAG, "Load Form Thành công: " + product.getName());
                    Log.d(TAG, "Link ảnh: " + product.getImageUrl());

                    // Hiển thị dữ liệu lên giao diện
                    tvProductName.setText(product.getName());

                    if (product.getShop() != null) {
                        tvShopName.setText("Cung cấp bởi: " + product.getShop().getName());
                    } else {
                        tvShopName.setText("Cung cấp bởi: Hệ thống");
                    }

                    // Load ảnh dùng Glide
                    Glide.with(WriteReviewActivity.this)
                            .load(product.getImageUrl()) // Model mới dùng getImageUrl()
                            .placeholder(R.drawable.placeholder_product)
                            .error(R.drawable.placeholder_product)
                            .into(imgProduct);
                } else {
                    Log.e(TAG, "Lỗi tải form: Code " + response.code());
                    Toast.makeText(WriteReviewActivity.this, "Không tải được thông tin sản phẩm", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ProductReviewDTO> call, Throwable t) {
                Log.e(TAG, "Lỗi kết nối tải form: " + t.getMessage());
                Toast.makeText(WriteReviewActivity.this, "Lỗi kết nối mạng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void submitReview() {
        int rating = (int) ratingBar.getRating();
        String comment = edtComment.getText().toString().trim();

        if (rating == 0) {
            Toast.makeText(this, "Vui lòng chọn số sao đánh giá", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tạo Request Body (Model ReviewRequest phải có @SerializedName chuẩn)
        ReviewRequest request = new ReviewRequest(productId, rating, comment);

        // Log dữ liệu chuẩn bị gửi đi
        Log.d(TAG, "Đang gửi đánh giá...");
        Log.d(TAG, "Data gửi đi: ProductID=" + productId + ", Rating=" + rating + ", Comment=" + comment);

        // Khóa nút để tránh bấm nhiều lần
        btnSubmit.setEnabled(false);
        btnSubmit.setText("Đang gửi...");

        ApiService apiService = RetrofitClient.getClient(this).create(ApiService.class);

        apiService.saveReview("Bearer " + token, request).enqueue(new Callback<ReviewResponse>() {
            @Override
            public void onResponse(Call<ReviewResponse> call, Response<ReviewResponse> response) {
                btnSubmit.setEnabled(true);
                btnSubmit.setText("Gửi đánh giá");

                if (response.isSuccessful()) {
                    Log.d(TAG, "Gửi đánh giá thành công!");
                    Toast.makeText(WriteReviewActivity.this, "Đánh giá thành công!", Toast.LENGTH_SHORT).show();
                    finish(); // Đóng màn hình
                } else {
                    // Lấy lỗi chi tiết từ Server để debug lỗi 400
                    try {
                        String errorBody = response.errorBody().string();
                        Log.e(TAG, "Lỗi gửi đánh giá (Code " + response.code() + "): " + errorBody);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(WriteReviewActivity.this, "Lỗi gửi: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ReviewResponse> call, Throwable t) {
                btnSubmit.setEnabled(true);
                btnSubmit.setText("Gửi đánh giá");
                Log.e(TAG, "Lỗi mạng khi gửi: " + t.getMessage());
                Toast.makeText(WriteReviewActivity.this, "Lỗi kết nối mạng", Toast.LENGTH_SHORT).show();
            }
        });
    }
}