package com.vibecoding.flowerstore.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.vibecoding.flowerstore.R;
import com.vibecoding.flowerstore.Service.APIService;
import com.vibecoding.flowerstore.Service.LoginRequest;
import com.vibecoding.flowerstore.Service.LoginResponse;
import com.vibecoding.flowerstore.Service.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private TextView tvSignUp, tvForgotPassword, tvNotice;
    private EditText edtUsername, edtPassword;
    private Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Ánh xạ
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
        APIService apiService = RetrofitClient.getClient().create(APIService.class);
        LoginRequest loginRequest = new LoginRequest(edtUsername.getText().toString(), edtPassword.getText().toString());

        Call<LoginResponse> call = apiService.login(loginRequest);

        call.enqueue(new Callback <LoginResponse>(){
            @Override
            public void onResponse(Call<LoginResponse> call, retrofit2.Response<LoginResponse> response) {
                if (response.isSuccessful()) {
                    LoginResponse loginResponse = response.body();
                    if (loginResponse != null){
                        SharedPreferences prefs = getSharedPreferences("MY_APP_PREFS", MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("ACCESS_TOKEN", response.body().getToken()); // Lưu token server trả về
                        editor.apply();
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                    }
                } else {
                    //Xử lý lỗi
                    Log.e(TAG, "Lỗi đăng nhập: " + response.code());
                    tvNotice.setVisibility(View.VISIBLE);
                    tvNotice.setText("Đăng nhập thất bại. Vui lòng kiểm tra lại thông tin đăng nhập.");
                }
            }
            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Log.e(TAG, "Lỗi kết nối mạng: " + t.getMessage());
                //Xử lý lỗi kết nối mạng
                tvNotice.setVisibility(View.VISIBLE);
                tvNotice.setText("Đăng nhập thất bại. Vui lòng kiểm tra lại kết nối mạng.");
            }
        });
    }
}