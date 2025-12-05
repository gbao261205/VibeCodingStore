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
import com.vibecoding.flowerstore.Service.RetrofitClient;
import com.vibecoding.flowerstore.Service.VerifyOtpRequest;
import com.vibecoding.flowerstore.Service.VerifyOtpResponse;

import retrofit2.Call;
import retrofit2.Callback;

public class OtpActivity extends AppCompatActivity {
    private static final String TAG = "OtpActivity";
    private EditText edtOtp1, edtOtp2, edtOtp3, edtOtp4, edtOtp5, edtOtp6;
    private Button btnConfirm;
    private TextView tvResend, tvNotice;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);

        //Ánh xạ
        edtOtp1 = findViewById(R.id.edtOtp1);
        edtOtp2 = findViewById(R.id.edtOtp2);
        edtOtp3 = findViewById(R.id.edtOtp3);
        edtOtp4 = findViewById(R.id.edtOtp4);
        edtOtp5 = findViewById(R.id.edtOtp5);
        edtOtp6 = findViewById(R.id.edtOtp6);
        btnConfirm = findViewById(R.id.btnConfirm);
        tvResend = findViewById(R.id.tvResend);
        tvNotice = findViewById(R.id.tvNotice);

        //Lấy dữ liệu từ intent
        username = getIntent().getStringExtra("username");


        //Xử lý sự kiện cho nút Resend
        tvResend.setOnClickListener(v -> {
            ResendOtp();
        });

        //Xử lý sự kiện cho nút Confirm
        btnConfirm.setOnClickListener(v -> {
            ConfirmOtp();
        });

    }

    private void ConfirmOtp() {
        String otpCode = edtOtp1.getText().toString()
                + edtOtp2.getText().toString()
                + edtOtp3.getText().toString()
                + edtOtp4.getText().toString()
                + edtOtp5.getText().toString()
                + edtOtp6.getText().toString();

        APIService apiService = RetrofitClient.getClient().create(APIService.class);
        VerifyOtpRequest verifyOtpRequest = new VerifyOtpRequest(username, otpCode);

        Call<VerifyOtpResponse> call = apiService.verifyOtp(verifyOtpRequest);

        call.enqueue(new Callback<VerifyOtpResponse>() {
            @Override
            public void onResponse(Call<VerifyOtpResponse> call, retrofit2.Response<VerifyOtpResponse> response) {
                if (response.isSuccessful()) {
                    VerifyOtpResponse verifyOtpResponse = response.body();
                    if (verifyOtpResponse != null) {
                        Intent intent = new Intent(OtpActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    }
                    else {
                        Log.d(TAG, "Lỗi xác thực OTP: " + response.code());
                        tvNotice.setText("Xác thực thất bại");
                        tvNotice.setVisibility(View.VISIBLE);
                    }
                }
            }
            @Override
            public void onFailure(Call<VerifyOtpResponse> call, Throwable t) {
                Log.e(TAG, "Lỗi xác thực OTP: " + t.getMessage());
                tvNotice.setText("Xác thực thất bại. Vui lòng kiểm tra lại kết nối mạng.");
                tvNotice.setVisibility(View.VISIBLE);
            }
        });
    }

    private void ResendOtp() {
    }
}