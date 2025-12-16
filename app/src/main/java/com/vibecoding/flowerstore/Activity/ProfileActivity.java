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
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
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
    private ProgressBar progressBar;
    private ScrollView scrollView;

    private static User cachedUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        setupViews();
        setupNavigation();

        // Listener cho avatar và user info layout đã được chuyển vào showLoggedInUI và showGuestUI
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
        progressBar = findViewById(R.id.profile_progress_bar);
        scrollView = findViewById(R.id.scroll_view);

        navHome = findViewById(R.id.nav_home);
        navCategories = findViewById(R.id.nav_categories);
        navFavorites = findViewById(R.id.nav_favorites);
        navAccount = findViewById(R.id.nav_account);
    }

    private void setupNavigation() {
        navHome.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        });
        navCategories.setOnClickListener(v -> {
            Intent intent = new Intent(this, CategoriesActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        });
        navFavorites.setOnClickListener(v ->{
            Intent intent = new Intent(this, FavoriteActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        });
    }

    private void checkLoginStatusAndFetchProfile() {
        progressBar.setVisibility(View.VISIBLE);
        scrollView.setVisibility(View.GONE);

        SharedPreferences prefs = getSharedPreferences("MY_APP_PREFS", Context.MODE_PRIVATE);
        String token = prefs.getString("ACCESS_TOKEN", null);

        if (token != null) {
            if (cachedUser != null) {
                Log.d(TAG, "User profile found in cache. Displaying cached data.");
                showLoggedInUI(cachedUser);
            } else {
                Log.d(TAG, "Token found. Fetching user profile...");
                fetchUserProfile("Bearer " + token);
            }
        } else {
            Log.d(TAG, "No token found. Displaying guest UI.");
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
                    Log.d(TAG, "Profile fetched successfully: " + user.getFullName());
                    showLoggedInUI(user);
                } else {
                    Log.e(TAG, "Failed to fetch profile. Code: " + response.code());
                    handleAuthenticationError();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.e(TAG, "API call failed: " + t.getMessage());
                Toast.makeText(ProfileActivity.this, "Lỗi kết nối, không thể tải thông tin.", Toast.LENGTH_SHORT).show();
                handleAuthenticationError();
            }
        });
    }

    private void showLoggedInUI(User user) {
        progressBar.setVisibility(View.GONE);
        scrollView.setVisibility(View.VISIBLE);

        userName.setText(user.getFullName());
        if (user.getEmail() != null) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                userEmail.setText(Html.fromHtml(user.getEmail(), Html.FROM_HTML_MODE_LEGACY));
            } else {
                userEmail.setText(Html.fromHtml(user.getEmail()));
            }
        }
        userEmail.setVisibility(View.VISIBLE);

        // Tải ảnh đại diện bằng Glide và bo tròn
        if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
            Glide.with(ProfileActivity.this)
                    .load(user.getAvatar())
                    .circleCrop() // Bo tròn ảnh
                    .placeholder(R.drawable.placeholder_avatar) // Ảnh chờ
                    .error(R.drawable.placeholder_avatar)       // Ảnh lỗi
                    .into(avatar);
        } else {
            Glide.with(this).load(R.drawable.placeholder_avatar).circleCrop().into(avatar);
        }

        orderHistoryButton.setVisibility(View.VISIBLE);
        savedAddressesButton.setVisibility(View.VISIBLE);
        paymentMethodsButton.setVisibility(View.VISIBLE);
        helpSupportButton.setVisibility(View.VISIBLE);
        logoutButton.setVisibility(View.VISIBLE);

        userInfoLayout.setClickable(true);
        userInfoLayout.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
            intent.putExtra("user", cachedUser);
            startActivity(intent);
        });

        logoutButton.setOnClickListener(v -> handleLogout());
    }

    private void showGuestUI() {
        progressBar.setVisibility(View.GONE);
        scrollView.setVisibility(View.VISIBLE);

        userName.setText("Xin hãy đăng nhập");
        userEmail.setText("Chào mừng đến với Flower Store");
        cachedUser = null;

        // Reset avatar về ảnh mặc định
        Glide.with(this).load(R.drawable.placeholder_avatar).circleCrop().into(avatar);

        // Ẩn các nút không cần thiết
        orderHistoryButton.setVisibility(View.GONE);
        savedAddressesButton.setVisibility(View.GONE);
        paymentMethodsButton.setVisibility(View.GONE);
        helpSupportButton.setVisibility(View.GONE);
        logoutButton.setVisibility(View.GONE);

        userInfoLayout.setClickable(true);
        userInfoLayout.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            startActivity(intent);
        });
    }

    private void handleAuthenticationError() {
        SharedPreferences prefs = getSharedPreferences("MY_APP_PREFS", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove("ACCESS_TOKEN");
        editor.apply();

        // Xóa cache và cập nhật giao diện ngay lập tức
        invalidateProfileCache();
        Toast.makeText(this, "Phiên đăng nhập đã hết hạn", Toast.LENGTH_SHORT).show();
        showGuestUI();
    }

    private void handleLogout() {
        SharedPreferences prefs = getSharedPreferences("MY_APP_PREFS", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove("ACCESS_TOKEN");
        editor.apply();

        // Xóa cache và cập nhật giao diện ngay lập tức
        invalidateProfileCache();
        Toast.makeText(this, "Đăng xuất thành công", Toast.LENGTH_SHORT).show();
        showGuestUI();
    }

    public static void invalidateProfileCache() {
        cachedUser = null;
    }
}
