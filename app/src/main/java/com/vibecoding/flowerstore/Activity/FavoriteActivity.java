package com.vibecoding.flowerstore.Activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.vibecoding.flowerstore.Adapter.FavoriteProductAdapter;
import com.vibecoding.flowerstore.Model.DataStore; // <-- QUAN TRỌNG: Import DataStore để dùng Cache
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
    private TextView tvEmptyNotify; // Nếu bạn có TextView báo trống trong XML

    // Bottom Navigation Elements
    private LinearLayout navHome;
    private LinearLayout navCategories;
    private LinearLayout navFavorites;
    private LinearLayout navAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);

        initViews();
        setupRecyclerView();
        setupBottomNavigation();

        // Gọi hàm tải dữ liệu (Logic Cache + API thật)
        loadWishlistData();
    }

    private void initViews() {
        recyclerFavorites = findViewById(R.id.recycler_favorites);

        // Navigation Elements
        navHome = findViewById(R.id.nav_home);
        navCategories = findViewById(R.id.nav_categories);
        navFavorites = findViewById(R.id.nav_favorites);
        navAccount = findViewById(R.id.nav_account);

        // Nút Back trên header
        ImageButton btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
    }

    private void setupRecyclerView() {
        productList = new ArrayList<>();
        // Sử dụng GridLayoutManager 2 cột
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
        // 1. Lấy Token để kiểm tra đăng nhập
        SharedPreferences prefs = getSharedPreferences("MY_APP_PREFS", Context.MODE_PRIVATE);
        String token = prefs.getString("ACCESS_TOKEN", null);

        if (token == null) {
            Toast.makeText(this, "Bạn chưa đăng nhập!", Toast.LENGTH_SHORT).show();
            // TODO: Chuyển hướng sang LoginActivity
            // Intent intent = new Intent(this, LoginActivity.class);
            // startActivity(intent);
            // finish();
            return;
        }

        // 2. KIỂM TRA CACHE (Bộ nhớ tạm)
        // Nếu đã từng tải rồi thì lấy ra dùng luôn, KHÔNG gọi API nữa
        if (DataStore.cachedFavorites != null && !DataStore.cachedFavorites.isEmpty()) {
            productList.clear();
            productList.addAll(DataStore.cachedFavorites);
            adapter.notifyDataSetChanged();
            Log.d("FavoriteActivity", "Dùng dữ liệu từ Cache (Siêu nhanh)");
            return;
        }

        // 3. GỌI API (Nếu Cache đang rỗng)
        // Dùng RetrofitClient.getClient(this) để tự động gắn Token vào Header
        ApiService apiService = RetrofitClient.getClient(this).create(ApiService.class);

        apiService.getWishlistedProducts("wishlisted").enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Product> newProducts = response.body().getProducts();

                    if (newProducts != null) {
                        // A. CẬP NHẬT CACHE
                        DataStore.cachedFavorites = newProducts;

                        // B. HIỂN THỊ LÊN MÀN HÌNH
                        productList.clear();
                        productList.addAll(newProducts);
                        adapter.notifyDataSetChanged();

                        if (productList.isEmpty()) {
                            Toast.makeText(FavoriteActivity.this, "Danh sách yêu thích trống", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    // Xử lý lỗi Token hết hạn (401) hoặc lỗi khác
                    if (response.code() == 401) {
                        Toast.makeText(FavoriteActivity.this, "Phiên đăng nhập hết hạn", Toast.LENGTH_SHORT).show();
                        // TODO: Xóa token và logout
                    } else {
                        Toast.makeText(FavoriteActivity.this, "Lỗi tải dữ liệu: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Log.e("FavoriteActivity", "Error: " + t.getMessage());
                Toast.makeText(FavoriteActivity.this, "Lỗi kết nối mạng!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        Intent intent = null;

        if (id == R.id.nav_home) {
            intent = new Intent(this, MainActivity.class);
            // Dọn sạch back stack để khi back từ Home sẽ thoát app
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        else if (id == R.id.nav_categories) {
            intent = new Intent(this, CategoriesActivity.class);
        }
        else if (id == R.id.nav_favorites) {
            return;
        }
        else if (id == R.id.nav_account) {
            intent = new Intent(this, ProfileActivity.class);
        }

        // --- KHỐI LỆNH CHUYỂN TRANG KHÔNG HIỆU ỨNG ---
        if (intent != null) {
            startActivity(intent);
            // 1. Tắt hiệu ứng khi trang MỚI hiện lên
            overridePendingTransition(0, 0);

            // 2. Đóng trang hiện tại để không bị chồng trang
            finish();

            // 3. Tắt hiệu ứng khi trang CŨ đóng lại
            overridePendingTransition(0, 0);
        }
    }
}