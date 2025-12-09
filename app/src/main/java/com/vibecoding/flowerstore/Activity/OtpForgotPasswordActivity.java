package com.vibecoding.flowerstore.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.vibecoding.flowerstore.R;
import com.vibecoding.flowerstore.Service.APIService;
import com.vibecoding.flowerstore.Service.ResendOtpForgotPassRequest;
import com.vibecoding.flowerstore.Service.ResendOtpRequest;
import com.vibecoding.flowerstore.Service.ResendOtpResponse;
import com.vibecoding.flowerstore.Service.ResetPasswordVerifyRequest;
import com.vibecoding.flowerstore.Service.ResetPasswordVerifyResponse;
import com.vibecoding.flowerstore.Service.RetrofitClient;
import com.vibecoding.flowerstore.Service.VerifyOtpRequest;
import com.vibecoding.flowerstore.Service.VerifyOtpResponse;

import retrofit2.Call;
import retrofit2.Callback;

public class OtpForgotPasswordActivity extends AppCompatActivity {

    private static final String TAG = "OtpForgotPasswordActivity";
    private EditText edtOtp;
    private Button btnConfirm;
    private TextView tvNotice, tvResend;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_forgot_password);

        //Ánh xạ
        edtOtp = findViewById(R.id.edtOtp);
        btnConfirm = findViewById(R.id.btnConfirm);
        tvNotice = findViewById(R.id.tvNotice);
        tvResend = findViewById(R.id.tvResend);

        email = getIntent().getStringExtra("email");

        tvResend.setOnClickListener(v -> {
            resendOtp();
        });

        btnConfirm.setOnClickListener(v -> {
            String otp = edtOtp.getText().toString();
            if (otp.isEmpty()) {
                edtOtp.setError("Vui lòng nhập mã OTP");
            } else {
                ConfirmOtp();
            }
        });
    }

    private void ConfirmOtp() {
        String otpCode = edtOtp.getText().toString();

        if (otpCode.length() != 6) {
            tvNotice.setText("Vui lòng nhập mã OTP gồm 6 chữ số.");
            tvNotice.setVisibility(View.VISIBLE);
            return;
        }

        APIService apiService = RetrofitClient.getClient().create(APIService.class);
        ResetPasswordVerifyRequest resetPasswordVerifyRequest = new ResetPasswordVerifyRequest(email, otpCode);

        Call<ResetPasswordVerifyResponse> call = apiService.verifyResetPasswordOtp(resetPasswordVerifyRequest);

        call.enqueue(new Callback<ResetPasswordVerifyResponse>() {
            @Override
            public void onResponse(Call<ResetPasswordVerifyResponse> call, retrofit2.Response<ResetPasswordVerifyResponse> response) {
                if (response.isSuccessful()) {
                    ResetPasswordVerifyResponse resetPasswordVerifyResponse = response.body();
                    if (resetPasswordVerifyResponse != null) {
                        Intent intent = new Intent(OtpForgotPasswordActivity.this, ChangePasswordActivity.class);
                        intent.putExtra("email", email);
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
            public void onFailure(Call<ResetPasswordVerifyResponse> call, Throwable t) {
                Log.e(TAG, "Lỗi xác thực OTP: " + t.getMessage());
                tvNotice.setText("Xác thực thất bại. Vui lòng kiểm tra lại kết nối mạng.");
                tvNotice.setVisibility(View.VISIBLE);
            }
        });
    }

    private void resendOtp(){
        APIService apiService = RetrofitClient.getClient().create(APIService.class);
        ResendOtpForgotPassRequest resendOtpForgotPassRequest = new ResendOtpForgotPassRequest(email);
        Call<ResendOtpResponse> call = apiService.resendResetOtp(resendOtpForgotPassRequest);

        call.enqueue(new Callback<ResendOtpResponse>() {
           @Override
           public void onResponse(Call<ResendOtpResponse> call, retrofit2.Response<ResendOtpResponse> response) {
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