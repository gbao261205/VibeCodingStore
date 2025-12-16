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
        requestWindowFeature(Window.FEATURE_NO_TITLE);//will hide the title not the title bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//int flag, int mask
        setContentView(R.layout.activity_category_products);

        // 1. Nhận dữ liệu từ Intent
        categorySlug = getIntent().getStringExtra("category_slug");
        String categoryName = getIntent().getStringExtra("category_name");

        setupViews();

        // Set tên danh mục lên header
        if (categoryName != null) {
            tvTitle.setText(categoryName);
        }

        setupRecyclerView();

        // Gọi API lấy sản phẩm
        fetchProductsByCategory(categorySlug);
    }

    private void setupViews() {
        recyclerProducts = findViewById(R.id.recycler_category_products);
        tvTitle = findViewById(R.id.tv_category_title);
        btnBack = findViewById(R.id.btn_back);
        progressBar = findViewById(R.id.progress_bar);

        btnBack.setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        // Dùng lại ProductAdapter (Grid 2 cột)
        adapter = new ProductAdapter(new ArrayList<>(), this);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        recyclerProducts.setLayoutManager(layoutManager);
        recyclerProducts.setAdapter(adapter);
    }

    private void fetchProductsByCategory(String slug) {
        progressBar.setVisibility(View.VISIBLE);

        // Dùng Client Public
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);

        // Gọi API lọc theo slug
        // Lưu ý: Tên hàm trong ApiService phải khớp (ví dụ: getProductsByCategory)
        Call<ApiResponse> call = apiService.getProductsByCategory(slug);

        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    List<Product> products = response.body().getProducts();

                    if (products != null && !products.isEmpty()) {
                        adapter.updateData(products);
                    } else {
                        Toast.makeText(CategoryProductsActivity.this, "Không có sản phẩm nào trong danh mục này", Toast.LENGTH_SHORT).show();
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