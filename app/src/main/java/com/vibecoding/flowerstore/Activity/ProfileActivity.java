package com.vibecoding.flowerstore.Activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.vibecoding.flowerstore.Model.DataStore;
import com.vibecoding.flowerstore.Model.User;
import com.vibecoding.flowerstore.R;
import com.vibecoding.flowerstore.Service.ApiService;
import com.vibecoding.flowerstore.Service.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";
    private TextView userName, userEmail;
    private Button logoutButton;
    private MaterialButton orderHistoryButton, savedAddressesButton, paymentMethodsButton, helpSupportButton;
    private LinearLayout userInfoLayout;
    private LinearLayout navHome, navCategories, navFavorites, navAccount;
    private ImageView avatar;

    private static User cachedUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        setupViews();
        setupNavigation();

        // Xử lý click Avatar
        if (avatar != null) {
            avatar.setOnClickListener(v -> {
                Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
                startActivity(intent);
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkLoginStatusAndFetchProfile();
    }

    private void setupViews() {
        userName = findViewById(R.id.user_name);
        userEmail = findViewById(R.id.user_email);
        logoutButton = findViewById(R.id.logout_button);
        orderHistoryButton = findViewById(R.id.order_history_button);
        savedAddressesButton = findViewById(R.id.saved_addresses_button);
        paymentMethodsButton = findViewById(R.id.payment_methods_button);
        helpSupportButton = findViewById(R.id.help_support_button);
        userInfoLayout = findViewById(R.id.user_info_layout);
        avatar = findViewById(R.id.avatar);

        navHome = findViewById(R.id.nav_home);
        navCategories = findViewById(R.id.nav_categories);
        navFavorites = findViewById(R.id.nav_favorites);
        navAccount = findViewById(R.id.nav_account);
    }

    // --- ĐÂY LÀ PHẦN SỬA ĐỔI NAVIGATION ---
    private void setupNavigation() {
        // 1. Về Trang Chủ: Cần finish() để xóa các trang cũ, tránh nặng máy
        navHome.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
            // Cờ này giúp xóa sạch các trang đang mở đè lên Home
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            overridePendingTransition(0, 0); // Tắt hiệu ứng
            finish();
        });

        // 2. Sang Danh Mục: KHÔNG finish(), chỉ thêm cờ SingleTop
        navCategories.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, CategoriesActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP); // Tránh mở 2 lần trang giống nhau
            startActivity(intent);
            overridePendingTransition(0, 0);
            // KHÔNG gọi finish() -> Trang cũ sẽ nằm dưới, tạo cảm giác mượt hơn
        });

        // 3. Sang Yêu Thích: KHÔNG finish()
        navFavorites.setOnClickListener(v ->{
            Intent intent = new Intent(ProfileActivity.this, FavoriteActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });

        // 4. Tài khoản (Đang ở đây rồi thì không làm gì hoặc reload)
        navAccount.setOnClickListener(v -> {
            // Không làm gì vì đang ở trang này
        });
    }

    private void checkLoginStatusAndFetchProfile() {
        SharedPreferences prefs = getSharedPreferences("MY_APP_PREFS", Context.MODE_PRIVATE);
        String token = prefs.getString("ACCESS_TOKEN", null);

        if (token != null) {
            if (cachedUser != null) {
                showLoggedInUI(cachedUser);
            } else {
                fetchUserProfile("Bearer " + token);
            }
        } else {
            showGuestUI();
        }
    }

    private void fetchUserProfile(String authToken) {
        ApiService apiService = RetrofitClient.getClient(this).create(ApiService.class);
        Call<User> call = apiService.getProfile(authToken);

        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();
                    cachedUser = user;
                    showLoggedInUI(user);
                } else {
                    handleAuthenticationError();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(ProfileActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                // Không auto logout khi lỗi mạng, chỉ báo lỗi thôi
            }
        });
    }

    private void showLoggedInUI(User user) {
        if(userName != null) userName.setText(user.getFullName());

        if (user.getEmail() != null && userEmail != null) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                userEmail.setText(Html.fromHtml(user.getEmail(), Html.FROM_HTML_MODE_LEGACY));
            } else {
                userEmail.setText(Html.fromHtml(user.getEmail()));
            }
            userEmail.setVisibility(View.VISIBLE);
        }

        if(orderHistoryButton != null) orderHistoryButton.setVisibility(View.VISIBLE);
        if(savedAddressesButton != null) savedAddressesButton.setVisibility(View.VISIBLE);
        if(paymentMethodsButton != null) paymentMethodsButton.setVisibility(View.VISIBLE);
        if(helpSupportButton != null) helpSupportButton.setVisibility(View.VISIBLE);
        if(logoutButton != null) logoutButton.setVisibility(View.VISIBLE);

        userInfoLayout.setClickable(true);
        userInfoLayout.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
            startActivity(intent);
        });

        if(logoutButton != null) logoutButton.setOnClickListener(v -> handleAuthenticationError());
    }

    private void showGuestUI() {
        if(userName != null) userName.setText("Xin hãy đăng nhập");
        if(userEmail != null) userEmail.setText("Chào mừng đến với Flower Store");
        cachedUser = null;

        userInfoLayout.setClickable(true);
        userInfoLayout.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        // Ẩn các nút chức năng khi chưa login
        if(orderHistoryButton != null) orderHistoryButton.setVisibility(View.GONE);
        if(savedAddressesButton != null) savedAddressesButton.setVisibility(View.GONE);
        if(paymentMethodsButton != null) paymentMethodsButton.setVisibility(View.GONE);
        if(helpSupportButton != null) helpSupportButton.setVisibility(View.GONE);
        if(logoutButton != null) logoutButton.setVisibility(View.GONE);
    }

    private void handleAuthenticationError() {
        SharedPreferences prefs = getSharedPreferences("MY_APP_PREFS", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove("ACCESS_TOKEN");
        editor.apply();

        cachedUser = null; // Xóa cache user
        DataStore.cachedFavorites = null; // Xóa cache yêu thích

        Toast.makeText(this, "Đã đăng xuất", Toast.LENGTH_SHORT).show();

        // Reset về Home hoặc Login
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    public static void invalidateProfileCache() {
        cachedUser = null;
    }
}