package com.vibecoding.flowerstore.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.vibecoding.flowerstore.Adapter.AdminProductAdapter;
import com.vibecoding.flowerstore.Model.ProductDTO;
import com.vibecoding.flowerstore.R;
import com.vibecoding.flowerstore.Service.ApiService;
import com.vibecoding.flowerstore.Service.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminProductManagementActivity extends AppCompatActivity {

    private RecyclerView rvProductList;
    private AdminProductAdapter productAdapter;
    private ImageView btnBack, btnClearSearch;
    private EditText etSearch;
    private ProgressBar progressBar;
    private ApiService apiService;
    private List<ProductDTO> masterProductList = new ArrayList<>();
    private String currentSearchQuery = "";
    private LinearLayout orderButton, dashboardButton, productButton, shopButton;
    private FrameLayout logoButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_admin_product_management);

        initView();
        setupRecyclerView();
        
        apiService = RetrofitClient.getClient(this).create(ApiService.class);
        fetchProducts(""); // Load ban đầu

        setupListeners();
    }

    private void initView() {
        rvProductList = findViewById(R.id.rvProductList);
        btnBack = findViewById(R.id.btnBack);
        etSearch = findViewById(R.id.etSearch);
        btnClearSearch = findViewById(R.id.btnClearSearch);
        progressBar = findViewById(R.id.progressBar);
        orderButton = findViewById(R.id.orderButton);
        dashboardButton = findViewById(R.id.dashboardButton);
        productButton = findViewById(R.id.productButton);
        shopButton = findViewById(R.id.shopButton);
        logoButton = findViewById(R.id.logoButton);
    }

    private void setupRecyclerView() {
        rvProductList.setLayoutManager(new LinearLayoutManager(this));
        productAdapter = new AdminProductAdapter(this, new ArrayList<>());
        rvProductList.setAdapter(productAdapter);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(AdminProductManagementActivity.this, AdminDashboardActivity.class);
            startActivity(intent);
            finish();
        });

        orderButton.setOnClickListener(v -> {
            Intent intent = new Intent(AdminProductManagementActivity.this, AdminOrderManagementActivity.class);
            startActivity(intent);
            finish();
        });

        logoButton.setOnClickListener(v->{
            Intent intent = new Intent(AdminProductManagementActivity.this, AdminDashboardActivity.class);
            startActivity(intent);
            finish();
        });

        productButton.setOnClickListener(v->{
            Intent intent = new Intent(AdminProductManagementActivity.this, AdminProductManagementActivity.class);
            startActivity(intent);
            finish();
        });

        shopButton.setOnClickListener(v->{
            Intent intent = new Intent(AdminProductManagementActivity.this, AdminStoreManagementActivity.class);
            startActivity(intent);
            finish();
        });

        dashboardButton.setOnClickListener(v -> {
            Intent intent = new Intent(AdminProductManagementActivity.this, AdminDashboardActivity.class);
            startActivity(intent);
            finish();
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s.toString().trim();
                if (currentSearchQuery.isEmpty()) {
                    btnClearSearch.setVisibility(View.GONE);
                } else {
                    btnClearSearch.setVisibility(View.VISIBLE);
                }
                filterProducts(currentSearchQuery);
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnClearSearch.setOnClickListener(v -> etSearch.setText(""));
    }

    private void fetchProducts(String keyword) {
        progressBar.setVisibility(View.VISIBLE);
        rvProductList.setVisibility(View.GONE);

        // API này có thể lọc trên server nếu backend hỗ trợ keyword
        // Hoặc trả về list full nếu keyword rỗng
        Call<List<ProductDTO>> call = apiService.getAllProducts(keyword);
        call.enqueue(new Callback<List<ProductDTO>>() {
            @Override
            public void onResponse(Call<List<ProductDTO>> call, Response<List<ProductDTO>> response) {
                progressBar.setVisibility(View.GONE);
                rvProductList.setVisibility(View.VISIBLE);

                if (response.isSuccessful() && response.body() != null) {
                    masterProductList = response.body();
                    filterProducts(currentSearchQuery); // Áp dụng filter local nếu cần
                } else {
                    Toast.makeText(AdminProductManagementActivity.this, "Không thể tải danh sách sản phẩm", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<ProductDTO>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AdminProductManagementActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Hàm filter local (nếu muốn search nhanh trên client mà không gọi lại API liên tục)
    private void filterProducts(String query) {
        if (query.isEmpty()) {
            productAdapter.setProductList(masterProductList);
            return;
        }

        List<ProductDTO> filteredList = new ArrayList<>();
        String lowerQuery = query.toLowerCase();

        for (ProductDTO p : masterProductList) {
            if (p.getName().toLowerCase().contains(lowerQuery)) {
                filteredList.add(p);
            }
        }
        productAdapter.setProductList(filteredList);
    }
}
