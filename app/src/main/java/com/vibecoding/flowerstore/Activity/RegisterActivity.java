package com.vibecoding.flowerstore.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.vibecoding.flowerstore.R;
import com.vibecoding.flowerstore.Service.APIService;
import com.vibecoding.flowerstore.Service.RegisterRequest;
import com.vibecoding.flowerstore.Service.RegisterResponse;
import com.vibecoding.flowerstore.Service.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "RegisterActivity";
    private Button btnRegister;
    private ImageView ivBack;
    private EditText edtFullName, edtEmail, edtPassword, edtConfirmPassword, edtPhoneNumber;
    private TextView tvLogin, tvNotice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //Ánh xạ
        btnRegister = findViewById(R.id.btnRegister);
        ivBack = findViewById(R.id.ivBack);
        edtFullName = findViewById(R.id.edtFullName);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        edtPhoneNumber = findViewById(R.id.edtPhoneNumber);
        tvLogin = findViewById(R.id.tvLogin);
        tvNotice = findViewById(R.id.tvNotice);

        ivBack.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
        tvLogin.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        btnRegister.setOnClickListener(v -> {
            Register();
        });

    }

    private void Register() {
        APIService apiService = RetrofitClient.getClient().create(APIService.class);

        RegisterRequest registerRequest = new RegisterRequest(edtFullName.getText().toString(), edtPassword.getText().toString(), edtEmail.getText().toString(), edtConfirmPassword.getText().toString(), edtPhoneNumber.getText().toString());

        Call<RegisterResponse> call = apiService.register(registerRequest);

        call.enqueue(new Callback<RegisterResponse>() {
            @Override
            public void onResponse(Call<RegisterResponse> call, Response<RegisterResponse> response) {
                if (response.isSuccessful()) {
                    RegisterResponse registerResponse = response.body();
                    if (registerResponse != null) {
                        if (registerResponse.getMessage() != null) {
                            Intent intent = new Intent(RegisterActivity.this, OtpActivity.class);
                            startActivity(intent);
                        } else {
                            Log.d(TAG, "Lỗi đăng ký: " + response.code());
                            tvNotice.setText("Lỗi đăng ký");
                            tvNotice.setVisibility(View.VISIBLE);
                        }
                    }
                }
            }
            @Override
            public void onFailure(Call<RegisterResponse> call, Throwable t) {
                Log.e(TAG, "Lỗi đăng ký: " + t.getMessage());
                tvNotice.setText("Đăng ký thất bại. Vui lòng kiểm tra lại kết nối mạng.");
                tvNotice.setVisibility(View.VISIBLE);
            }

        });

    }
}