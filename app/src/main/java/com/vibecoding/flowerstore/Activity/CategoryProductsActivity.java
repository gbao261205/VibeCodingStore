package com.vibecoding.flowerstore.Activity;

import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.vibecoding.flowerstore.Adapter.ProductAdapter;
import com.vibecoding.flowerstore.Model.ApiResponse;
import com.vibecoding.flowerstore.Model.DataStore; // Import DataStore
import com.vibecoding.flowerstore.Model.Product;
import com.vibecoding.flowerstore.R;
import com.vibecoding.flowerstore.Service.ApiService;
import com.vibecoding.flowerstore.Service.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CategoryProductsActivity extends AppCompatActivity {

    private RecyclerView recyclerProducts;
    private ProductAdapter adapter;
    private TextView tvTitle;
    private ImageView btnBack;
    private ProgressBar progressBar;
    private String categorySlug;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_category_products);

        // 1. Nhận dữ liệu từ Intent
        categorySlug = getIntent().getStringExtra("category_slug");
        String categoryName = getIntent().getStringExtra("category_name");

        setupViews();

        if (categoryName != null) {
            tvTitle.setText(categoryName);
        }

        setupRecyclerView();

        // --- LOGIC CACHE MỚI ---
        loadData();
    }

    private void setupViews() {
        recyclerProducts = findViewById(R.id.recycler_category_products);
        tvTitle = findViewById(R.id.tv_category_title);
        btnBack = findViewById(R.id.btn_back);
        progressBar = findViewById(R.id.progress_bar);

        btnBack.setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        // Khởi tạo Adapter (Giả sử constructor của bạn là: List, Context)
        adapter = new ProductAdapter(new ArrayList<>(), this);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        recyclerProducts.setLayoutManager(layoutManager);
        recyclerProducts.setAdapter(adapter);
    }

    // Hàm kiểm tra Cache trước khi gọi API
    private void loadData() {
        // 1. Kiểm tra xem trong DataStore đã lưu danh mục này chưa
        if (DataStore.categoryCache != null && DataStore.categoryCache.containsKey(categorySlug)) {
            // NẾU CÓ: Lấy ra và hiển thị ngay lập tức -> Rất nhanh
            List<Product> cachedList = DataStore.categoryCache.get(categorySlug);
            if (cachedList != null && !cachedList.isEmpty()) {
                adapter.updateData(cachedList);
                progressBar.setVisibility(View.GONE);
                return; // Dừng lại, không gọi API nữa
            }
        }

        // 2. NẾU CHƯA CÓ: Thì mới gọi API
        fetchProductsByCategory(categorySlug);
    }

    private void fetchProductsByCategory(String slug) {
        progressBar.setVisibility(View.VISIBLE);

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<ApiResponse> call = apiService.getProductsByCategory(slug);

        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    // Kiểm tra API trả về getProducts() hay getContent() tùy vào model của bạn
                    // Ở đây mình dùng getProducts() theo code gốc bạn gửi
                    List<Product> products = response.body().getProducts();

                    if (products != null && !products.isEmpty()) {
                        // --- LƯU VÀO CACHE ---
                        // Để lần sau mở lại không cần load nữa
                        if (DataStore.categoryCache == null) DataStore.categoryCache = new java.util.HashMap<>();
                        DataStore.categoryCache.put(slug, products);

                        adapter.updateData(products);
                    } else {
                        Toast.makeText(CategoryProductsActivity.this, "Không có sản phẩm nào", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(CategoryProductsActivity.this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(CategoryProductsActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }
}