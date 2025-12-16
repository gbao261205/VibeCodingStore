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

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.vibecoding.flowerstore.R;
import com.vibecoding.flowerstore.Service.ApiService;
import com.vibecoding.flowerstore.Service.ResendOtpRequest;
import com.vibecoding.flowerstore.Service.ResendOtpResponse;
import com.vibecoding.flowerstore.Service.RetrofitClient;
import com.vibecoding.flowerstore.Service.VerifyOtpRequest;
import com.vibecoding.flowerstore.Service.VerifyOtpResponse;

import retrofit2.Call;
import retrofit2.Callback;

public class OtpActivity extends AppCompatActivity {
    private static final String TAG = "OtpActivity";
    private EditText edtOtp;
    private Button btnConfirm;
    private TextView tvResend, tvNotice;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);//will hide the title not the title bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//int flag, int mask
        setContentView(R.layout.activity_otp);

        //Ánh xạ
        edtOtp = findViewById(R.id.edtOtp);
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
        String otpCode = edtOtp.getText().toString();
        
        if (otpCode.length() != 6) {
            tvNotice.setText("Vui lòng nhập mã OTP gồm 6 chữ số.");
            tvNotice.setVisibility(View.VISIBLE);
            return;
        }

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        VerifyOtpRequest verifyOtpRequest = new VerifyOtpRequest(username, otpCode);

        Call<VerifyOtpResponse> call = apiService.verifyOtp(verifyOtpRequest);

        call.enqueue(new Callback<VerifyOtpResponse>() {
            @Override
            public void onResponse(Call<VerifyOtpResponse> call, retrofit2.Response<VerifyOtpResponse> response) {
                if (response.isSuccessful()) {
                    VerifyOtpResponse verifyOtpResponse = response.body();
                    if (verifyOtpResponse != null) {
                        Intent intent = new Intent(OtpActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Log.d(TAG, "Lỗi xác thực OTP: Phản hồi thành công nhưng nội dung trống.");
                        tvNotice.setText("Đã xảy ra lỗi không mong muốn. Vui lòng thử lại.");
                        tvNotice.setVisibility(View.VISIBLE);
                    }
                } else {
                    Log.d(TAG, "Lỗi xác thực OTP: " + response.code());
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            Log.e(TAG, "Chi tiết lỗi: " + errorBody);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    tvNotice.setText("Mã OTP không đúng hoặc đã hết hạn.");
                    tvNotice.setVisibility(View.VISIBLE);
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
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        ResendOtpRequest resendOtpRequest = new ResendOtpRequest(username);
        Call<ResendOtpResponse> call = apiService.resendOtp(resendOtpRequest);

        call.enqueue(new Callback<ResendOtpResponse>() {
            @Override
            public void onResponse(Call<ResendOtpResponse> call, retrofit2.Response<ResendOtpResponse> response){
                if (response.isSuccessful()) {
                    ResendOtpResponse resendOtpResponse = response.body();
                    if (resendOtpResponse != null) {
                        tvNotice.setText("Mã OTP đã được gửi lại. Vui lòng kiểm tra email.");
                        tvNotice.setVisibility(View.VISIBLE);
                    } else {
                        Log.d(TAG, "Lỗi gửi OTP: Phản hồi thành công nhưng nội dung trống.");
                        tvNotice.setText("Đã xảy ra lỗi không mong muốn. Vui lòng thử lại.");
                        tvNotice.setVisibility(View.VISIBLE);
                    }
                }
            }
            @Override
            public void onFailure(Call<ResendOtpResponse> call, Throwable t) {
                Log.e(TAG, "Lỗi gửi OTP: " + t.getMessage());
                tvNotice.setText("Gửi OTP thất bại. Vui lòng kiểm tra lại kết nối mạng.");
                tvNotice.setVisibility(View.VISIBLE);
            }
        });


    }
}