package com.vibecoding.flowerstore.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;

import com.vibecoding.flowerstore.R;
import com.vibecoding.flowerstore.Service.AdminDashboardResponse;
import com.vibecoding.flowerstore.Service.ApiService;
import com.vibecoding.flowerstore.Service.RetrofitClient;

import java.text.NumberFormat;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminDashboardActivity extends AppCompatActivity {

    private TextView tvTotalRevenue, tvTotalOrders, tvPendingShopRequests, tvActiveShops, tvPendingAppeals, tvTotalShops, tvTotalUsers;
    private ImageView btnBack;
    private ProgressBar progressBar;
    private NestedScrollView scrollView;
    private LinearLayout orderButton, dashboardButton, productButton, shopButton, userButton, shopButton2;
    private FrameLayout logoButton;

    // Cache static để lưu dữ liệu trong bộ nhớ
    private static AdminDashboardResponse cachedData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_admin_dashboard);

        initView();
        fetchDashboardData();
    }

    private void initView() {
        tvTotalRevenue = findViewById(R.id.tvTotalRevenue);
        tvTotalOrders = findViewById(R.id.tvTotalOrders);
        tvPendingShopRequests = findViewById(R.id.tvPendingShopRequests);
        tvActiveShops = findViewById(R.id.tvActiveShops);
        tvPendingAppeals = findViewById(R.id.tvPendingAppeals);
        tvTotalShops = findViewById(R.id.tvTotalShops);
        tvTotalUsers = findViewById(R.id.tvTotalUsers);
        btnBack = findViewById(R.id.btnBack);
        progressBar = findViewById(R.id.progressBar);
        scrollView = findViewById(R.id.scrollView);
        orderButton = findViewById(R.id.orderButton);
        dashboardButton = findViewById(R.id.dashboardButton);
        productButton = findViewById(R.id.productButton);
        shopButton = findViewById(R.id.shopButton);
        logoButton = findViewById(R.id.logoButton);
        userButton = findViewById(R.id.userButton);
        shopButton2 = findViewById(R.id.shopButton2);

        btnBack.setOnClickListener(v -> {
            cachedData = null; // Xóa cache khi thoát
            Intent intent = new Intent(AdminDashboardActivity.this, ProfileActivity.class);
            startActivity(intent);
            finish();
        });

        orderButton.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, AdminOrderManagementActivity.class);
            startActivity(intent);
        });
        
        logoButton.setOnClickListener(v->{});
        
        productButton.setOnClickListener(v->{
            Intent intent = new Intent(AdminDashboardActivity.this, AdminProductManagementActivity.class);
            startActivity(intent);
        });
        
        shopButton.setOnClickListener(v->{
            Intent intent = new Intent(AdminDashboardActivity.this, AdminStoreManagementActivity.class);
            startActivity(intent);
        });

        shopButton2.setOnClickListener(v->{
            Intent intent = new Intent(AdminDashboardActivity.this, AdminStoreManagementActivity.class);
            startActivity(intent);
        });
        
        dashboardButton.setOnClickListener(v -> {
            // Đang ở dashboard rồi, có thể reload hoặc không làm gì
        });

        userButton.setOnClickListener(v->{
            Intent intent = new Intent(AdminDashboardActivity.this, AdminUserManagementActivity.class);
            startActivity(intent);
        });
    }

    private void fetchDashboardData() {
        // Kiểm tra cache trước
        if (cachedData != null) {
            progressBar.setVisibility(View.GONE);
            scrollView.setVisibility(View.VISIBLE);
            updateUI(cachedData);
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        scrollView.setVisibility(View.GONE);

        ApiService apiService = RetrofitClient.getClient(this).create(ApiService.class);
        Call<AdminDashboardResponse> call = apiService.getAdminDashboard();

        call.enqueue(new Callback<AdminDashboardResponse>() {
            @Override
            public void onResponse(Call<AdminDashboardResponse> call, Response<AdminDashboardResponse> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    scrollView.setVisibility(View.VISIBLE);
                    AdminDashboardResponse data = response.body();
                    cachedData = data; // Lưu vào cache
                    updateUI(data);
                } else {
                    Toast.makeText(AdminDashboardActivity.this, "Không thể tải dữ liệu: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AdminDashboardResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AdminDashboardActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI(AdminDashboardResponse data) {
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

        tvTotalRevenue.setText(currencyFormat.format(data.getTotalRevenue()));
        tvTotalOrders.setText(String.valueOf(data.getTotalOrders()));
        
        // Cập nhật theo JSON response mới
        tvPendingShopRequests.setText(String.valueOf(data.getPendingShopRequests())); // Đăng ký shop
        tvActiveShops.setText(String.valueOf(data.getTotalShops())); // Shop hoạt động (dùng totalShops)
        tvPendingAppeals.setText(String.valueOf(data.getPendingAppealsCount())); // Khiếu nại

        // Phần hoạt động hệ thống
        tvTotalShops.setText(String.valueOf(data.getTotalShops()));
        tvTotalUsers.setText(String.valueOf(data.getTotalUsers()));
    }
    
    @Override
    public void onBackPressed() {
        cachedData = null; // Xóa cache khi ấn nút Back cứng của điện thoại
        super.onBackPressed();
    }
}
