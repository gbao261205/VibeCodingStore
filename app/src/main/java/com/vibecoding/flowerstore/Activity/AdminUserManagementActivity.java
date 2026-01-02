package com.vibecoding.flowerstore.Activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.vibecoding.flowerstore.Adapter.AdminUserAdapter;
import com.vibecoding.flowerstore.Model.UserDTO;
import com.vibecoding.flowerstore.R;
import com.vibecoding.flowerstore.Service.ApiService;
import com.vibecoding.flowerstore.Service.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminUserManagementActivity extends AppCompatActivity implements View.OnClickListener {

    private RecyclerView rvUserList;
    private AdminUserAdapter userAdapter;
    private ImageView btnBack;
    private EditText etSearch;
    private ProgressBar progressBar;
    private FloatingActionButton fabAddUser;
    private ApiService apiService;
    private String userToken;
    private List<UserDTO> masterUserList = new ArrayList<>();
    private TextView chipAll, chipActive, chipLocked;

    // Trạng thái lọc hiện tại: 0 = Tất cả, 1 = Hoạt động, 2 = Đã khóa
    private int currentStatusFilter = 0; 

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_admin_user_management);

        initView();
        setupRecyclerView();
        
        apiService = RetrofitClient.getClient(this).create(ApiService.class);
        checkLoginAndFetchData();

        setupListeners();
    }

    private void initView() {
        rvUserList = findViewById(R.id.rvUserList);
        btnBack = findViewById(R.id.btnBack);
        etSearch = findViewById(R.id.etSearch);
        progressBar = findViewById(R.id.progressBar);
        fabAddUser = findViewById(R.id.fabAddUser);

        chipAll = findViewById(R.id.chipAll);
        chipActive = findViewById(R.id.chipActive);
        chipLocked = findViewById(R.id.chipLocked);
    }

    private void setupRecyclerView() {
        rvUserList.setLayoutManager(new LinearLayoutManager(this));
        userAdapter = new AdminUserAdapter(this, new ArrayList<>());
        rvUserList.setAdapter(userAdapter);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterUsers();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        fabAddUser.setOnClickListener(v -> {
            Toast.makeText(this, "Chức năng thêm người dùng đang phát triển", Toast.LENGTH_SHORT).show();
        });

        chipAll.setOnClickListener(this);
        chipActive.setOnClickListener(this);
        chipLocked.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.chipAll) {
            currentStatusFilter = 0;
            updateChipStyles(chipAll);
        } else if (v.getId() == R.id.chipActive) {
            currentStatusFilter = 1;
            updateChipStyles(chipActive);
        } else if (v.getId() == R.id.chipLocked) {
            currentStatusFilter = 2;
            updateChipStyles(chipLocked);
        }
        filterUsers();
    }

    private void updateChipStyles(TextView selectedChip) {
        // Reset all chips
        chipAll.setBackgroundResource(R.drawable.bg_chip_unselected);
        chipAll.setTextColor(getResources().getColor(R.color.text_sub));
        chipActive.setBackgroundResource(R.drawable.bg_chip_unselected);
        chipActive.setTextColor(getResources().getColor(R.color.text_sub));
        chipLocked.setBackgroundResource(R.drawable.bg_chip_unselected);
        chipLocked.setTextColor(getResources().getColor(R.color.text_sub));

        // Set selected chip
        selectedChip.setBackgroundResource(R.drawable.bg_chip_selected_pink);
        selectedChip.setTextColor(getResources().getColor(R.color.white));
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
        fetchUsers();
    }

    private void fetchUsers() {
        progressBar.setVisibility(View.VISIBLE);
        rvUserList.setVisibility(View.GONE);

        Call<List<UserDTO>> call = apiService.getAllUsers(userToken);
        call.enqueue(new Callback<List<UserDTO>>() {
            @Override
            public void onResponse(Call<List<UserDTO>> call, Response<List<UserDTO>> response) {
                progressBar.setVisibility(View.GONE);
                rvUserList.setVisibility(View.VISIBLE);

                if (response.isSuccessful() && response.body() != null) {
                    masterUserList = response.body();
                    // Cập nhật số lượng trên chip (tùy chọn)
                    int activeCount = 0;
                    for (UserDTO user : masterUserList) {
                        if (user.isActive()) activeCount++;
                    }
                    chipAll.setText("Tất cả (" + masterUserList.size() + ")");
                    chipActive.setText("Hoạt động (" + activeCount + ")");
                    chipLocked.setText("Đã khóa (" + (masterUserList.size() - activeCount) + ")");

                    filterUsers(); // Hiển thị lần đầu
                } else {
                    Toast.makeText(AdminUserManagementActivity.this, "Không thể tải danh sách người dùng: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<UserDTO>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AdminUserManagementActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterUsers() {
        String query = etSearch.getText().toString().toLowerCase().trim();
        List<UserDTO> filteredList = new ArrayList<>();

        for (UserDTO user : masterUserList) {
            // Lọc theo trạng thái
            boolean statusMatch = false;
            if (currentStatusFilter == 0) { // Tất cả
                statusMatch = true;
            } else if (currentStatusFilter == 1) { // Hoạt động
                statusMatch = user.isActive();
            } else if (currentStatusFilter == 2) { // Đã khóa
                statusMatch = !user.isActive();
            }

            if (!statusMatch) continue;

            // Lọc theo từ khóa tìm kiếm
            boolean searchMatch = true;
            if (!query.isEmpty()) {
                boolean nameMatch = user.getFullName() != null && user.getFullName().toLowerCase().contains(query);
                boolean usernameMatch = user.getUsername() != null && user.getUsername().toLowerCase().contains(query);
                boolean emailMatch = user.getEmail() != null && user.getEmail().toLowerCase().contains(query);
                boolean phoneMatch = user.getPhoneNumber() != null && user.getPhoneNumber().contains(query);
                searchMatch = nameMatch || usernameMatch || emailMatch || phoneMatch;
            }

            if (searchMatch) {
                filteredList.add(user);
            }
        }
        userAdapter.setUserList(filteredList);
    }
}
