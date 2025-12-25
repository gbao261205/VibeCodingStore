package com.vibecoding.flowerstore.Activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.vibecoding.flowerstore.Adapter.FavoriteProductAdapter;
import com.vibecoding.flowerstore.Model.DataStore; // Import DataStore
import com.vibecoding.flowerstore.R;
import com.vibecoding.flowerstore.Service.ApiService;
import com.vibecoding.flowerstore.Model.ApiResponse;
import com.vibecoding.flowerstore.Model.Product;
import com.vibecoding.flowerstore.Service.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FavoriteActivity extends AppCompatActivity implements View.OnClickListener {

    private RecyclerView recyclerFavorites;
    private FavoriteProductAdapter adapter;
    private List<Product> productList;

    // Bottom Navigation Elements
    private LinearLayout navHome;
    private LinearLayout navCategories;
    private LinearLayout navFavorites;
    private LinearLayout navAccount;
    private TextView tvEmptyNotify;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_favorite);

        initViews();
        setupRecyclerView();
        setupBottomNavigation();

    }


    @Override
    protected void onResume() {
        super.onResume();
        loadWishlistData();
    }

    private void initViews() {
        recyclerFavorites = findViewById(R.id.recycler_favorites);

        // Navigation Elements
        tvEmptyNotify = findViewById(R.id.tv_empty_notify);
        navHome = findViewById(R.id.nav_home);
        navCategories = findViewById(R.id.nav_categories);
        navFavorites = findViewById(R.id.nav_favorites);
        navAccount = findViewById(R.id.nav_account);

        // Nút Back trên header
        ImageButton btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                Intent intent = new Intent(FavoriteActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            });
        }
    }

    private void setupRecyclerView() {
        productList = new ArrayList<>();
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        recyclerFavorites.setLayoutManager(layoutManager);

        adapter = new FavoriteProductAdapter(productList, this);
        recyclerFavorites.setAdapter(adapter);
    }

    private void setupBottomNavigation() {
        navHome.setOnClickListener(this);
        navCategories.setOnClickListener(this);
        navFavorites.setOnClickListener(this);
        navAccount.setOnClickListener(this);
    }

    private void loadWishlistData() {
        SharedPreferences prefs = getSharedPreferences("MY_APP_PREFS", Context.MODE_PRIVATE);
        String token = prefs.getString("ACCESS_TOKEN", null);

        if (token == null) {
            // Xử lý khi chưa login...
            if (tvEmptyNotify != null) tvEmptyNotify.setVisibility(View.VISIBLE);
            return;
        }

        // 1. HIỆN CACHE CŨ NGAY LẬP TỨC (Nếu có)
        if (DataStore.cachedFavorites != null) {
            productList.clear();
            productList.addAll(DataStore.cachedFavorites);
            adapter.notifyDataSetChanged();

            // Xử lý view trống/có dữ liệu cho cache cũ
            if (productList.isEmpty()) {
                if (tvEmptyNotify != null) tvEmptyNotify.setVisibility(View.VISIBLE);
                recyclerFavorites.setVisibility(View.GONE);
            } else {
                if (tvEmptyNotify != null) tvEmptyNotify.setVisibility(View.GONE);
                recyclerFavorites.setVisibility(View.VISIBLE);
            }

            Log.d("FavoriteActivity", "Đang hiện data cũ, chờ data mới...");
            // ❌ KHÔNG return ở đây nữa! Để code chạy tiếp xuống dưới gọi API
        }

        // 2. GỌI API ĐỂ LẤY SẢN PHẨM MỚI (Chạy ngầm)
        ApiService apiService = RetrofitClient.getClient(this).create(ApiService.class);
        apiService.getWishlistedProducts("wishlisted", 0, 100).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Product> newProducts = response.body().getProducts();
                    if (newProducts == null) newProducts = new ArrayList<>();

                    // A. CẬP NHẬT LẠI CACHE
                    DataStore.cachedFavorites = newProducts;

                    // B. CẬP NHẬT GIAO DIỆN (Sản phẩm mới sẽ hiện ra ở đây)
                    productList.clear();
                    productList.addAll(newProducts);
                    adapter.notifyDataSetChanged();

                    // C. Ẩn hiện view trống
                    if (productList.isEmpty()) {
                        if (tvEmptyNotify != null) tvEmptyNotify.setVisibility(View.VISIBLE);
                        recyclerFavorites.setVisibility(View.GONE);
                    } else {
                        if (tvEmptyNotify != null) tvEmptyNotify.setVisibility(View.GONE);
                        recyclerFavorites.setVisibility(View.VISIBLE);
                    }
                    Log.d("FavoriteActivity", "Đã cập nhật data mới từ Server");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                // Nếu lỗi mạng, user vẫn đang xem được list cũ (từ cache) nên không bị màn hình trắng
                Log.e("FavoriteActivity", "Lỗi mạng: " + t.getMessage());
            }
        });
    }

    // Hàm phụ trợ để cập nhật giao diện cho gọn code
    // Hàm này giúp code gọn gàng, chuyên nghiệp, không lặp lại logic
    private void updateUI(List<Product> data) {
        // Tắt vòng xoay loading (nếu có)
        // if (progressBar != null) progressBar.setVisibility(View.GONE);

        if (data == null || data.isEmpty()) {
            // TRƯỜNG HỢP TRỐNG: Ẩn RecyclerView, Hiện thông báo text giữa màn hình
            recyclerFavorites.setVisibility(View.GONE);
            if (tvEmptyNotify != null) {
                tvEmptyNotify.setVisibility(View.VISIBLE);
                tvEmptyNotify.setText("Bạn chưa có sản phẩm yêu thích nào");
            }
        } else {
            // TRƯỜNG HỢP CÓ DỮ LIỆU: Hiện RecyclerView, Ẩn thông báo text
            if (tvEmptyNotify != null) tvEmptyNotify.setVisibility(View.GONE);
            recyclerFavorites.setVisibility(View.VISIBLE);

            // Cập nhật Adapter
            productList.clear();
            productList.addAll(data);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        Intent intent = null;

        if (id == R.id.nav_home) {
            intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        else if (id == R.id.nav_categories) {
            intent = new Intent(this, CategoriesActivity.class);
        }
        else if (id == R.id.nav_favorites) {
            return; // Đang ở trang này rồi thì không làm gì cả
        }
        else if (id == R.id.nav_account) {
            intent = new Intent(this, ProfileActivity.class);
        }

        if (intent != null) {
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
            overridePendingTransition(0, 0);
        }
    }
}