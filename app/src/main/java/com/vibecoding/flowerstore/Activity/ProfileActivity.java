package com.vibecoding.flowerstore.Activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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
    private Button editProfileButton, logoutButton;
    private MaterialButton orderHistoryButton, savedAddressesButton, paymentMethodsButton, helpSupportButton;
    private LinearLayout userInfoLayout;
    private LinearLayout homeButton, categoryButton, favoriteButton, profileButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        setupViews();
        setupNavigation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkLoginStatusAndFetchProfile();
    }

    private void setupViews() {
        userName = findViewById(R.id.user_name);
        userEmail = findViewById(R.id.user_email);
        editProfileButton = findViewById(R.id.edit_profile_button);
        logoutButton = findViewById(R.id.logout_button);
        orderHistoryButton = findViewById(R.id.order_history_button);
        savedAddressesButton = findViewById(R.id.saved_addresses_button);
        paymentMethodsButton = findViewById(R.id.payment_methods_button);
        helpSupportButton = findViewById(R.id.help_support_button);
        userInfoLayout = findViewById(R.id.user_info_layout);

        homeButton = findViewById(R.id.home_button);
        categoryButton = findViewById(R.id.category_button);
        favoriteButton = findViewById(R.id.favorite_button);
        profileButton = findViewById(R.id.profile_button);
    }

    private void setupNavigation() {
        homeButton.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
        profileButton.setOnClickListener(v -> Toast.makeText(ProfileActivity.this, "Bạn đang ở trang Tài khoản", Toast.LENGTH_SHORT).show());
        categoryButton.setOnClickListener(v -> Toast.makeText(ProfileActivity.this, "Chức năng Danh mục sắp có!", Toast.LENGTH_SHORT).show());
        favoriteButton.setOnClickListener(v -> Toast.makeText(ProfileActivity.this, "Chức năng Yêu thích sắp có!", Toast.LENGTH_SHORT).show());
    }

    private void checkLoginStatusAndFetchProfile() {
        SharedPreferences prefs = getSharedPreferences("MY_APP_PREFS", Context.MODE_PRIVATE);
        String token = prefs.getString("ACCESS_TOKEN", null);

        if (token != null) {
            Log.d(TAG, "Token found. Fetching user profile...");
            fetchUserProfile("Bearer " + token);
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
        userName.setText(user.getFullName());
        userEmail.setText(user.getEmail());
        userEmail.setVisibility(View.VISIBLE);

        editProfileButton.setVisibility(View.VISIBLE);
        orderHistoryButton.setVisibility(View.VISIBLE);
        savedAddressesButton.setVisibility(View.VISIBLE);
        paymentMethodsButton.setVisibility(View.VISIBLE);
        helpSupportButton.setVisibility(View.VISIBLE);
        logoutButton.setVisibility(View.VISIBLE);

        userInfoLayout.setClickable(false);

        logoutButton.setOnClickListener(v -> handleAuthenticationError());
    }

    private void showGuestUI() {
        userName.setText("Xin hãy đăng nhập");
        userEmail.setText("Chào mừng đến với Flower Store");

        editProfileButton.setVisibility(View.GONE);
        orderHistoryButton.setVisibility(View.GONE);
        savedAddressesButton.setVisibility(View.GONE);
        paymentMethodsButton.setVisibility(View.GONE);
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

        Toast.makeText(this, "Phiên đăng nhập đã hết hạn", Toast.LENGTH_SHORT).show();
        showGuestUI();
    }
}
