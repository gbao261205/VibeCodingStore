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

import com.vibecoding.flowerstore.Adapter.AdminShopAdapter;
import com.vibecoding.flowerstore.Model.ShopDTO;
import com.vibecoding.flowerstore.R;
import com.vibecoding.flowerstore.Service.ApiService;
import com.vibecoding.flowerstore.Service.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminStoreManagementActivity extends AppCompatActivity {

    private RecyclerView rvShopList;
    private AdminShopAdapter shopAdapter;
    private ImageView btnBack, btnClearSearch;
    private EditText etSearch;
    private ProgressBar progressBar;
    private ApiService apiService;
    private String userToken;
    private List<ShopDTO> masterShopList = new ArrayList<>();
    private String currentSearchQuery = "";
    LinearLayout orderButton, dashboardButton, productButton, shopButton;
    FrameLayout logoButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_admin_store_management);

        initView();
        setupRecyclerView();
        
        apiService = RetrofitClient.getClient(this).create(ApiService.class);
        checkLoginAndFetchData();

        setupListeners();
    }

    private void initView() {
        rvShopList = findViewById(R.id.rvShopList);
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
        rvShopList.setLayoutManager(new LinearLayoutManager(this));
        shopAdapter = new AdminShopAdapter(this, new ArrayList<>());
        rvShopList.setAdapter(shopAdapter);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(AdminStoreManagementActivity.this, AdminDashboardActivity.class);
            startActivity(intent);
            finish();
        });
        orderButton.setOnClickListener(v -> {
            Intent intent = new Intent(AdminStoreManagementActivity.this, AdminOrderManagementActivity.class);
            startActivity(intent);
            finish();
        });

        logoButton.setOnClickListener(v->{
            Intent intent = new Intent(AdminStoreManagementActivity.this, AdminDashboardActivity.class);
            startActivity(intent);
            finish();
        });

        productButton.setOnClickListener(v->{
            Intent intent = new Intent(AdminStoreManagementActivity.this, AdminProductManagementActivity.class);
            startActivity(intent);
            finish();
        });

        shopButton.setOnClickListener(v->{});

        dashboardButton.setOnClickListener(v -> {
            Intent intent = new Intent(AdminStoreManagementActivity.this, AdminDashboardActivity.class);
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
                filterShops(currentSearchQuery);
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnClearSearch.setOnClickListener(v -> etSearch.setText(""));
    }

    private void checkLoginAndFetchData() {
        SharedPreferences prefs = getSharedPreferences("MY_APP_PREFS", MODE_PRIVATE);
        String rawToken = prefs.getString("ACCESS_TOKEN", null);

        if (rawToken == null) {
            Toast.makeText(this, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        userToken = "Bearer " + rawToken;
        fetchShops();
    }

    private void fetchShops() {
        progressBar.setVisibility(View.VISIBLE);
        rvShopList.setVisibility(View.GONE);

        // API getAllShop có thêm tham số keyword nhưng ở đây ta fetch all rồi search local cho nhanh
        // hoặc nếu muốn search server thì truyền keyword vào
        Call<List<ShopDTO>> call = apiService.getAllShop(userToken, ""); 
        
        call.enqueue(new Callback<List<ShopDTO>>() {
            @Override
            public void onResponse(Call<List<ShopDTO>> call, Response<List<ShopDTO>> response) {
                progressBar.setVisibility(View.GONE);
                rvShopList.setVisibility(View.VISIBLE);

                if (response.isSuccessful() && response.body() != null) {
                    masterShopList = response.body();
                    filterShops(currentSearchQuery);
                } else {
                    Toast.makeText(AdminStoreManagementActivity.this, "Không thể tải danh sách cửa hàng: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<ShopDTO>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AdminStoreManagementActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterShops(String query) {
        if (query.isEmpty()) {
            shopAdapter.setShopList(masterShopList);
            return;
        }

        List<ShopDTO> filteredList = new ArrayList<>();
        String lowerQuery = query.toLowerCase();

        for (ShopDTO shop : masterShopList) {
            boolean nameMatch = shop.getName() != null && shop.getName().toLowerCase().contains(lowerQuery);
            
            if (nameMatch) {
                filteredList.add(shop);
            }
        }
        shopAdapter.setShopList(filteredList);
    }
}
