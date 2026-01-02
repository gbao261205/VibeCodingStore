package com.vibecoding.flowerstore.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.vibecoding.flowerstore.Model.DataStore; // Nhớ import DataStore
import com.vibecoding.flowerstore.R;
import com.vibecoding.flowerstore.Service.ApiService;
import com.vibecoding.flowerstore.Service.LoginRequest;
import com.vibecoding.flowerstore.Service.LoginResponse;
import com.vibecoding.flowerstore.Service.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private TextView tvSignUp, tvForgotPassword, tvNotice;
    private EditText edtUsername, edtPassword;
    private Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_login);

        // Ánh xạ View
        tvSignUp = findViewById(R.id.tvSignUp);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvNotice = findViewById(R.id.tvNotice);

        tvSignUp.setOnClickListener(v ->{
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        tvForgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });

        btnLogin.setOnClickListener(v -> {
            Login();
        });
    }

    private void Login() {
        tvNotice.setVisibility(View.GONE);

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        LoginRequest loginRequest = new LoginRequest(edtUsername.getText().toString(), edtPassword.getText().toString());

        Call<LoginResponse> call = apiService.login(loginRequest);

        call.enqueue(new Callback<LoginResponse>(){
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful()) {
                    LoginResponse loginResponse = response.body();
                    if (loginResponse != null){
                        // 1. Lưu Token
                        SharedPreferences prefs = getSharedPreferences("MY_APP_PREFS", MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("ACCESS_TOKEN", loginResponse.getToken());
                        editor.apply();

                        // 2. --- XÓA DANH SÁCH HIỂN THỊ TRANG HOME (QUAN TRỌNG) ---
                        // Dòng này giúp trang Home load lại từ đầu, không hiện list cũ
                        if (DataStore.cachedProducts != null) {
                            DataStore.cachedProducts.clear();
                        }

                        // Vẫn cần xóa Favorites cache để tránh hiện tim đỏ của user cũ trong tích tắc
                        if (DataStore.cachedFavorites != null) {
                            DataStore.cachedFavorites.clear();
                        }

                        // Xóa luôn cache category nếu muốn mọi thứ mới hoàn toàn (Tùy chọn)
                        if (DataStore.categoryCache != null) {
                            DataStore.categoryCache.clear();
                        }

                        // 3. Chuyển sang Main và Reset Task
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }
                } else {
                    Log.e(TAG, "Lỗi đăng nhập: " + response.code());
                    tvNotice.setVisibility(View.VISIBLE);
                    tvNotice.setText("Đăng nhập thất bại. Vui lòng kiểm tra lại thông tin.");
                }
            }
            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Log.e(TAG, "Lỗi kết nối mạng: " + t.getMessage());
                tvNotice.setVisibility(View.VISIBLE);
                tvNotice.setText("Đăng nhập thất bại. Vui lòng kiểm tra lại kết nối mạng.");
            }
        });
    }
}