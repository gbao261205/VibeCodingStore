package com.vibecoding.flowerstore.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.vibecoding.flowerstore.Adapter.CategoryGridAdapter;
import com.vibecoding.flowerstore.Model.Category;
import com.vibecoding.flowerstore.Model.DataStore; // <-- QUAN TRỌNG: Import DataStore
import com.vibecoding.flowerstore.R;
import com.vibecoding.flowerstore.Service.ApiService;
import com.vibecoding.flowerstore.Service.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CategoriesActivity extends AppCompatActivity implements View.OnClickListener {

    private RecyclerView recyclerCategoriesGrid;
    private CategoryGridAdapter adapter;
    private ImageView btnBack, btnCart;

    private LinearLayout navHome;
    private LinearLayout navCategories;
    private LinearLayout navFavorites;
    private LinearLayout navAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);//will hide the title not the title bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//int flag, int mask
        setContentView(R.layout.activity_categories);

        setupViews();
        setupRecyclerView();
        setupBottomNavigation();

        // Gọi hàm tải dữ liệu (đã nâng cấp Caching)
        fetchCategoriesFromApi();
    }

    private void setupViews() {
        recyclerCategoriesGrid = findViewById(R.id.recycler_categories_grid);
        btnBack = findViewById(R.id.btn_back);
        btnCart = findViewById(R.id.btn_cart);

        navHome = findViewById(R.id.nav_home);
        navCategories = findViewById(R.id.nav_categories);
        navFavorites = findViewById(R.id.nav_favorites);
        navAccount = findViewById(R.id.nav_account);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (btnCart != null) {
            btnCart.setOnClickListener(v -> {
                Toast.makeText(this, "Chức năng giỏ hàng", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void setupBottomNavigation() {
        navHome.setOnClickListener(this);
        navCategories.setOnClickListener(this);
        navFavorites.setOnClickListener(this);
        navAccount.setOnClickListener(this);
    }

    private void setupRecyclerView() {
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        recyclerCategoriesGrid.setLayoutManager(layoutManager);
        adapter = new CategoryGridAdapter(new ArrayList<>(), this);

        // --- BẮT ĐẦU THÊM MỚI ---
        adapter.setOnCategoryClickListener(new CategoryGridAdapter.OnCategoryClickListener() {
            @Override
            public void onCategoryClick(Category category) {
                // Chuyển sang màn hình danh sách sản phẩm
                Intent intent = new Intent(CategoriesActivity.this, CategoryProductsActivity.class);
                // Gửi kèm Slug (để gọi API) và Name (để hiển thị tiêu đề)
                intent.putExtra("category_slug", category.getSlug());
                intent.putExtra("category_name", category.getName());
                startActivity(intent);
            }
        });
        // --- KẾT THÚC THÊM MỚI ---
        recyclerCategoriesGrid.setAdapter(adapter);
    }

    // --- CẬP NHẬT: LOGIC KIỂM TRA KHO (CACHE) ---
    private void fetchCategoriesFromApi() {
        // 1. Kiểm tra xem kho có hàng chưa
        if (DataStore.cachedCategories != null && !DataStore.cachedCategories.isEmpty()) {
            adapter.updateData(DataStore.cachedCategories);
            Log.d("CategoriesActivity", "Dùng Cache: Categories (Không gọi API)");
            return; // Dừng luôn, không gọi mạng nữa
        }

        // 2. Nếu kho rỗng thì mới gọi API
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<List<Category>> call = apiService.getCategories();

        call.enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Category> categories = response.body();
                    if (!categories.isEmpty()) {
                        // 3. Có hàng về thì cất vào kho
                        DataStore.cachedCategories = categories;
                        adapter.updateData(categories);
                    } else {
                        Toast.makeText(CategoriesActivity.this, "Chưa có danh mục nào", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(CategoriesActivity.this, "Lỗi tải danh mục", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Category>> call, Throwable t) {
                Log.e("CategoriesActivity", "Error: " + t.getMessage());
                Toast.makeText(CategoriesActivity.this, "Lỗi kết nối hoặc Timeout", Toast.LENGTH_SHORT).show();
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
            // Đang ở trang Categories rồi thì không làm gì cả
            return;
        }
        else if (id == R.id.nav_favorites) {
            intent = new Intent(this, FavoriteActivity.class);
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