package com.vibecoding.flowerstore.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.vibecoding.flowerstore.R;
import com.vibecoding.flowerstore.Service.APIService;
import com.vibecoding.flowerstore.Service.ChangePasswordRequest;
import com.vibecoding.flowerstore.Service.ChangePasswordResponse;
import com.vibecoding.flowerstore.Service.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChangePasswordActivity extends AppCompatActivity {

    private static final String TAG = "ChangePasswordActivity";
    private EditText edtNewPassword, edtConfirmPassword;
    private Button btnChangePassword;
    private TextView tvNotice;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        edtNewPassword = findViewById(R.id.edtNewPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        tvNotice = findViewById(R.id.tvNotice);

        email = getIntent().getStringExtra("email");

        btnChangePassword.setOnClickListener(v -> {
            String newPassword = edtNewPassword.getText().toString();
            String confirmPassword = edtConfirmPassword.getText().toString();

            if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
                tvNotice.setText("Vui lòng nhập đầy đủ thông tin.");
                tvNotice.setVisibility(View.VISIBLE);
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                tvNotice.setText("Mật khẩu không khớp.");
                tvNotice.setVisibility(View.VISIBLE);
                return;
            }

            changePassword(email, newPassword, confirmPassword);
        });
    }

    private void changePassword(String email, String newPassword, String confirmPassword) {
        APIService apiService = RetrofitClient.getClient().create(APIService.class);
        ChangePasswordRequest changePasswordRequest = new ChangePasswordRequest(email, newPassword, confirmPassword);

        Call<ChangePasswordResponse> call = apiService.changePassword(changePasswordRequest);
        call.enqueue(new Callback<ChangePasswordResponse>() {
            @Override
            public void onResponse(Call<ChangePasswordResponse> call, Response<ChangePasswordResponse> response) {
                if (response.isSuccessful()) {
                    Intent intent = new Intent(ChangePasswordActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Log.d(TAG, "Lỗi đổi mật khẩu: " + response.code());
                    tvNotice.setText("Đổi mật khẩu thất bại. Vui lòng thử lại.");
                    tvNotice.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<ChangePasswordResponse> call, Throwable t) {
                Log.e(TAG, "Lỗi đổi mật khẩu: " + t.getMessage());
                tvNotice.setText("Đã xảy ra lỗi. Vui lòng kiểm tra lại kết nối mạng.");
                tvNotice.setVisibility(View.VISIBLE);
            }
        });
    }
}
