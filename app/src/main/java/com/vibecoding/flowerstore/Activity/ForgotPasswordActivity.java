package com.vibecoding.flowerstore.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.vibecoding.flowerstore.R;
import com.vibecoding.flowerstore.Service.APIService;
import com.vibecoding.flowerstore.Service.ForgotPasswordRequest;
import com.vibecoding.flowerstore.Service.ForgotPasswordResponse;
import com.vibecoding.flowerstore.Service.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForgotPasswordActivity extends AppCompatActivity {

    private static final String TAG = "ForgotPasswordActivity";
    private ImageView ivBack;
    private EditText edtEmail;
    private Button btnSend;
    private TextView tvBackToLogin, tvNotice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        //Ánh xạ
        ivBack = findViewById(R.id.ivBack);
        edtEmail = findViewById(R.id.edtEmail);
        btnSend = findViewById(R.id.btnSend);
        tvBackToLogin = findViewById(R.id.tvBackToLogin);
        tvNotice = findViewById(R.id.tvNotice);


        ivBack.setOnClickListener(v -> {
            Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        tvBackToLogin.setOnClickListener(v -> {
            Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        btnSend.setOnClickListener(v -> {
            String email = edtEmail.getText().toString();
            if (email.isEmpty()) {
                edtEmail.setError("Vui lòng nhập email");
            } else {
                ForgotPassword(email);
            }
        });
    }

    private void ForgotPassword(String email) {
        APIService apiService = RetrofitClient.getClient().create(APIService.class);

        ForgotPasswordRequest forgotPasswordRequest = new ForgotPasswordRequest(email);
        Call<ForgotPasswordResponse> call = apiService.forgotPassword(forgotPasswordRequest);
        call.enqueue(new Callback<ForgotPasswordResponse>() {
            @Override
            public void onResponse(Call<ForgotPasswordResponse> call, Response<ForgotPasswordResponse> response) {
                if (response.isSuccessful()) {
                    ForgotPasswordResponse forgotPasswordResponse = response.body();
                    if (forgotPasswordResponse != null) {
                        Intent intent = new Intent(ForgotPasswordActivity.this, OtpForgotPasswordActivity.class);
                        intent.putExtra("email", email);
                        startActivity(intent);
                    }
                } else {
                    // Xử lý thông báo lỗi
                    Log.d(TAG, "onResponse: " + response.message());
                    tvNotice.setText("Kiểm tra lại địa chỉ email");
                    tvNotice.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<ForgotPasswordResponse> call, Throwable t) {
                Log.e(TAG, "onFailure: " + t);
                tvNotice.setText("Kiểm tra lại kết nối");
                tvNotice.setVisibility(View.VISIBLE);
            }
        });
    }
}