package com.vibecoding.flowerstore.Activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.ChipGroup;
import com.vibecoding.flowerstore.Adapter.OrderAdapter;
import com.vibecoding.flowerstore.Model.OrderDTO;
import com.vibecoding.flowerstore.Model.User;
import com.vibecoding.flowerstore.R;
import com.vibecoding.flowerstore.Service.ApiService;
import com.vibecoding.flowerstore.Service.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderHistoryActivity extends AppCompatActivity {

    private static final String TAG = "OrderHistoryActivity";
    private RecyclerView rvOrderHistory;
    private OrderAdapter orderAdapter;
    private ChipGroup chipGroupFilter;
    private ImageButton btnBack;
    private ProgressBar progressBar;
    private ApiService apiService;
    private String userToken;
    private static User cachedUser;

    // Biến lưu trữ TOÀN BỘ đơn hàng tạm thời trong Activity
    private List<OrderDTO> masterOrderList = new ArrayList<>();

    // Biến lưu trạng thái người dùng MUỐN lọc khi dữ liệu chưa tải xong
    private String pendingFilterStatus = null;
    
    // Biến kiểm tra xem API có đang chạy không
    private boolean isLoading = false, statusOrderHistory = true;

    // Biến để quản lý Toast, tránh hiện trùng lặp
    private Toast mToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);//will hide the title not the title bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//int flag, int mask
        setContentView(R.layout.activity_order_history);

        // 1. Ánh xạ View
        rvOrderHistory = findViewById(R.id.rvOrderHistory);
        chipGroupFilter = findViewById(R.id.chipGroupFilter);
        btnBack = findViewById(R.id.btnBack);
        progressBar = findViewById(R.id.progressBar);

        // 2. Setup RecyclerView
        rvOrderHistory.setLayoutManager(new LinearLayoutManager(this));
        orderAdapter = new OrderAdapter(this, new ArrayList<>());
        rvOrderHistory.setAdapter(orderAdapter);

        // 3. Khởi tạo API Service
        apiService = RetrofitClient.getClient(this).create(ApiService.class);

        // 4. Kiểm tra đăng nhập và load dữ liệu ban đầu
        checkLoginStatusAndFetchData();

        // 6. Xử lý sự kiện Filter Chips (Lọc nội bộ từ masterOrderList)
        chipGroupFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;

            int id = checkedIds.get(0);
            if (id == R.id.chipAll) {
                filterOrdersLocal(null);
            } else if (id == R.id.chipNew) {
                filterOrdersLocal("Đơn hàng mới");
            } else if (id == R.id.chipWaitConfirm) {
                filterOrdersLocal("Chờ xác nhận");
            } else if (id == R.id.chipWaitPickup) {
                filterOrdersLocal("Chờ lấy hàng");
            } else if (id == R.id.chipDelivering) {
                filterOrdersLocal("Đang giao");
            } else if (id == R.id.chipSuccess) {
                filterOrdersLocal("Giao thành công");
            } else if (id == R.id.chipFail) {
                filterOrdersLocal("Giao thất bại");
            } else if (id == R.id.chipCancelled) {
                filterOrdersLocal("Đã huỷ");
            }
        });

        // 7. Nút Back
        btnBack.setOnClickListener(v -> finish());
    }

    private void checkLoginStatusAndFetchData() {
        SharedPreferences prefs = getSharedPreferences("MY_APP_PREFS", MODE_PRIVATE);
        String rawToken = prefs.getString("ACCESS_TOKEN", null);

        if (rawToken == null) {
            showToast("Vui lòng đăng nhập để xem lịch sử");
            finish(); 
            return;
        }
        userToken = "Bearer " + rawToken;
        
        if (cachedUser != null) {
             Log.d(TAG, "User profile found in cache: " + cachedUser.getFullName());
        } else {
             fetchUserProfile();
        }

        // Tải toàn bộ đơn hàng MỘT LẦN DUY NHẤT
        fetchAllOrdersFromServer();
    }

    private void fetchUserProfile() {
        Call<User> call = apiService.getProfile(userToken);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    cachedUser = response.body();
                } else if (response.code() == 401 || response.code() == 403) {
                    handleAuthenticationError();
                }
            }
            @Override
            public void onFailure(Call<User> call, Throwable t) {
                 Log.e(TAG, "Profile API call failed: " + t.getMessage());
            }
        });
    }

    private void handleAuthenticationError() {
        SharedPreferences prefs = getSharedPreferences("MY_APP_PREFS", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove("ACCESS_TOKEN");
        editor.apply();

        cachedUser = null;
        showToast("Phiên đăng nhập đã hết hạn");
        finish();
    }

    // Hàm gọi API lấy tất cả đơn hàng
    private void fetchAllOrdersFromServer() {
        if (isLoading) return; // Nếu đang tải rồi thì không gọi lại

        isLoading = true;
        progressBar.setVisibility(View.VISIBLE);
        rvOrderHistory.setVisibility(View.GONE);

        // Truyền null để lấy tất cả
        Call<List<OrderDTO>> call = apiService.getOrderHistory(userToken, null);

        call.enqueue(new Callback<List<OrderDTO>>() {
            @Override
            public void onResponse(Call<List<OrderDTO>> call, Response<List<OrderDTO>> response) {
                isLoading = false;
                progressBar.setVisibility(View.GONE);
                rvOrderHistory.setVisibility(View.VISIBLE);

                if (response.isSuccessful() && response.body() != null) {
                    // Lưu vào biến tạm thời
                    masterOrderList = response.body();
                    
                    // KIỂM TRA: Có yêu cầu lọc nào đang chờ không?
                    if (pendingFilterStatus != null) {
                        Log.d(TAG, "Applying pending filter: " + pendingFilterStatus);
                        // Thực hiện lọc theo yêu cầu chờ
                        filterOrdersLocal(pendingFilterStatus);
                        // Reset pending status (vì mình dùng UI chip selection để control, 
                        // nhưng reset ở đây để tránh tự động filter lần sau nếu refresh)
                        // Tuy nhiên, logic filterOrdersLocal sẽ handle việc hiển thị.
                    } else {
                        // Nếu không có yêu cầu nào, hiển thị tất cả
                        orderAdapter.setOrderList(masterOrderList);
                    }

                    if (masterOrderList.isEmpty()) {
                        showToast("Bạn chưa có đơn hàng nào");
                        statusOrderHistory = false;
                    }
                } else {
                    showToast("Lỗi tải dữ liệu: " + response.code());
                     if (response.code() == 401 || response.code() == 403) {
                         handleAuthenticationError();
                     }
                }
            }

            @Override
            public void onFailure(Call<List<OrderDTO>> call, Throwable t) {
                isLoading = false;
                progressBar.setVisibility(View.GONE);
                rvOrderHistory.setVisibility(View.VISIBLE);
                Log.e("OrderHistory", "Error: " + t.getMessage());
                showToast("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    // Hàm lọc nội bộ
    private void filterOrdersLocal(String status) {
        // Nếu danh sách gốc CHƯA có dữ liệu và trạng thái lịch sử đơn hàng là có
        if ((masterOrderList == null || masterOrderList.isEmpty()) && statusOrderHistory) {
            // Lưu lại mong muốn của người dùng
            pendingFilterStatus = status;
            
            // Nếu API chưa chạy (hoặc bị lỗi trước đó), hãy gọi lại API
            if (!isLoading) {
                fetchAllOrdersFromServer();
            } else {
                // Nếu đang loading (isLoading = true), chỉ cần đợi
                // Khi onResponse chạy xong, nó sẽ kiểm tra pendingFilterStatus
                // và tự động filter cho user.
                showToast("Đang tải dữ liệu...");
            }
            return;
        }

        // Nếu dữ liệu đã có sẵn, thực hiện lọc ngay lập tức
        // Reset pending status vì yêu cầu đã được đáp ứng
        pendingFilterStatus = null;

        // Nếu status là null -> hiển thị tất cả
        if (status == null) {
            orderAdapter.setOrderList(masterOrderList);
            return;
        }

        // Tạo danh sách kết quả lọc
        List<OrderDTO> filteredList = new ArrayList<>();
        for (OrderDTO order : masterOrderList) {
            // So sánh status của đơn hàng với status đang chọn
            if (order.getStatus() != null && order.getStatus().equals(status)) {
                filteredList.add(order);
            }
        }

        // Cập nhật RecyclerView
        orderAdapter.setOrderList(filteredList);
        
        if (filteredList.isEmpty() && statusOrderHistory) {
            showToast("Không có đơn hàng nào ở trạng thái này");
        }
    }

    // Hàm helper để hiển thị Toast duy nhất
    private void showToast(String message) {
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        mToast.show();
    }
}