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

import com.google.android.material.chip.ChipGroup;
import com.vibecoding.flowerstore.Adapter.AdminOrderAdapter;
import com.vibecoding.flowerstore.Model.OrderDTO;
import com.vibecoding.flowerstore.R;
import com.vibecoding.flowerstore.Service.ApiService;
import com.vibecoding.flowerstore.Service.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminOrderManagementActivity extends AppCompatActivity implements AdminOrderAdapter.OnOrderClickListener {

    private static final String TAG = "AdminOrderActivity";
    private RecyclerView rvOrderList;
    private AdminOrderAdapter orderAdapter;
    private ChipGroup chipGroupFilter;
    private ImageView btnBack;
    private EditText etSearch;
    private ProgressBar progressBar;
    private ApiService apiService;
    private String userToken;
    private List<OrderDTO> masterOrderList = new ArrayList<>();
    private String currentStatusFilter = null;
    private String currentSearchQuery = "";
    LinearLayout orderButton, dashboardButton, productButton, shopButton;
    FrameLayout logoButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_admin_order_management);

        initView();
        setupRecyclerView();
        
        apiService = RetrofitClient.getClient(this).create(ApiService.class);
        checkLoginAndFetchData();

        setupListeners();
    }

    private void initView() {
        rvOrderList = findViewById(R.id.rvOrderList);
        chipGroupFilter = findViewById(R.id.chipGroupFilter);
        btnBack = findViewById(R.id.btnBack);
        etSearch = findViewById(R.id.etSearch);
        progressBar = findViewById(R.id.progressBar);
        orderButton = findViewById(R.id.orderButton);
        dashboardButton = findViewById(R.id.dashboardButton);
        productButton = findViewById(R.id.productButton);
        shopButton = findViewById(R.id.shopButton);
        logoButton = findViewById(R.id.logoButton);
    }

    private void setupRecyclerView() {
        rvOrderList.setLayoutManager(new LinearLayoutManager(this));
        // Truyền 'this' làm listener
        orderAdapter = new AdminOrderAdapter(this, new ArrayList<>(), this);
        rvOrderList.setAdapter(orderAdapter);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(AdminOrderManagementActivity.this, AdminDashboardActivity.class);
            startActivity(intent);
            finish();
        });
        orderButton.setOnClickListener(v -> {});

        logoButton.setOnClickListener(v->{
            Intent intent = new Intent(AdminOrderManagementActivity.this, AdminDashboardActivity.class);
            startActivity(intent);
            finish();
        });

        productButton.setOnClickListener(v->{
            Intent intent = new Intent(AdminOrderManagementActivity.this, AdminProductManagementActivity.class);
            startActivity(intent);
            finish();
        });

        shopButton.setOnClickListener(v->{
            Intent intent = new Intent(AdminOrderManagementActivity.this, AdminStoreManagementActivity.class);
            startActivity(intent);
            finish();
        });

        dashboardButton.setOnClickListener(v -> {
            Intent intent = new Intent(AdminOrderManagementActivity.this, AdminDashboardActivity.class);
            startActivity(intent);
            finish();
        });

        chipGroupFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;

            int id = checkedIds.get(0);
            if (id == R.id.chipAll) currentStatusFilter = null;
            else if (id == R.id.chipNew) currentStatusFilter = "Đơn hàng mới";
            else if (id == R.id.chipWaitConfirm) currentStatusFilter = "Chờ xác nhận";
            else if (id == R.id.chipWaitPickup) currentStatusFilter = "Chờ lấy hàng";
            else if (id == R.id.chipDelivering) currentStatusFilter = "Đang giao";
            else if (id == R.id.chipSuccess) currentStatusFilter = "Giao thành công";
            else if (id == R.id.chipFail) currentStatusFilter = "Giao thất bại";
            else if (id == R.id.chipCancelled) currentStatusFilter = "Đã huỷ";
            
            filterAndSearch();
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s.toString().toLowerCase().trim();
                filterAndSearch();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
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
        fetchOrders();
    }

    private void fetchOrders() {
        progressBar.setVisibility(View.VISIBLE);
        rvOrderList.setVisibility(View.GONE);

        Call<List<OrderDTO>> call = apiService.getAllOrders(userToken);
        call.enqueue(new Callback<List<OrderDTO>>() {
            @Override
            public void onResponse(Call<List<OrderDTO>> call, Response<List<OrderDTO>> response) {
                progressBar.setVisibility(View.GONE);
                rvOrderList.setVisibility(View.VISIBLE);

                if (response.isSuccessful() && response.body() != null) {
                    masterOrderList = response.body();
                    filterAndSearch(); // Hiển thị dữ liệu ban đầu
                } else {
                    Toast.makeText(AdminOrderManagementActivity.this, "Lỗi tải dữ liệu: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<OrderDTO>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                rvOrderList.setVisibility(View.VISIBLE);
                Toast.makeText(AdminOrderManagementActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterAndSearch() {
        List<OrderDTO> filteredList = new ArrayList<>();

        for (OrderDTO order : masterOrderList) {
            boolean matchesStatus = (currentStatusFilter == null) || 
                                    (order.getStatus() != null && order.getStatus().equals(currentStatusFilter));

            boolean matchesSearch = true;
            if (!currentSearchQuery.isEmpty()) {
                String orderIdStr = String.valueOf(order.getId());
                String customerName = (order.getUser() != null && order.getUser().getFullName() != null) 
                                      ? order.getUser().getFullName().toLowerCase() : "";
                
                matchesSearch = orderIdStr.contains(currentSearchQuery) || customerName.contains(currentSearchQuery);
            }

            if (matchesStatus && matchesSearch) {
                filteredList.add(order);
            }
        }

        orderAdapter.setOrderList(filteredList);
    }

    @Override
    public void onOrderClick(OrderDTO order) {
        // Chuyển sang OrderDetailActivity khi click vào đơn hàng
        // Đã sửa: Truyền toàn bộ object OrderDTO thay vì chỉ ID để phù hợp với OrderDetailActivity của User
        Intent intent = new Intent(AdminOrderManagementActivity.this, OrderDetailActivity.class);
        intent.putExtra("ORDER_DATA", order); // Dùng key "ORDER_DATA"
        startActivity(intent);
    }
}
