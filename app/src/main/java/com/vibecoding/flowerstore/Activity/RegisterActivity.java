package com.vibecoding.flowerstore.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
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
import com.vibecoding.flowerstore.Service.ApiService;
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
    private EditText edtFullName, edtEmail, edtPassword, edtConfirmPassword, edtPhoneNumber, edtUsername;
    private TextView tvLogin, tvNotice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);//will hide the title not the title bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//int flag, int mask
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
        edtUsername = findViewById(R.id.edtUsername);

        //Xử lý sự kiện cho nút Back
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
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);

        RegisterRequest registerRequest = new RegisterRequest(edtUsername.getText().toString(), edtPassword.getText().toString(), edtEmail.getText().toString(), edtFullName.getText().toString(), edtPhoneNumber.getText().toString());

        Call<RegisterResponse> call = apiService.register(registerRequest);

        call.enqueue(new Callback<RegisterResponse>() {
            @Override
            public void onResponse(Call<RegisterResponse> call, Response<RegisterResponse> response) {
                if (response.isSuccessful()) {
                    RegisterResponse registerResponse = response.body();
                    if (registerResponse != null) {
                        if (registerResponse.getMessage() != null) {
                            Intent intent = new Intent(RegisterActivity.this, OtpActivity.class);
                            intent.putExtra("username", edtUsername.getText().toString());
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