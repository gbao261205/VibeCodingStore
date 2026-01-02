package com.vibecoding.flowerstore.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.vibecoding.flowerstore.R;
import com.vibecoding.flowerstore.Service.ApiService;
import com.vibecoding.flowerstore.Service.MessageResponse;
import com.vibecoding.flowerstore.Service.RetrofitClient;
import com.vibecoding.flowerstore.Service.ShopRegisterRequest;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterShopActivity extends AppCompatActivity {

    private final static String TAG = "RegisterShopActivity";
    private EditText edtShopName, edtDesc;
    private Button btnRegister;
    private ImageView ivBack;
    private TextView tvNotice;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);//will hide the title not the title bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//int flag, int mask
        setContentView(R.layout.activity_register_shop);

        initViews();
        setupListeners();
    }

    private void setupListeners() {
        ivBack.setOnClickListener(v -> finish());
        btnRegister.setOnClickListener(v -> handleRegister());
    }

    private void handleRegister() {
        String shopName = edtShopName.getText().toString().trim();
        String description = edtDesc.getText().toString().trim();

        if (TextUtils.isEmpty(shopName)) {
            tvNotice.setText("Vui lòng nhập tên cửa hàng.");
            tvNotice.setVisibility(View.VISIBLE);
            return;
        }

        if (TextUtils.isEmpty(description)) {
            tvNotice.setText("Vui lòng nhập mô tả cửa hàng.");
            tvNotice.setVisibility(View.VISIBLE);
            return;
        }

        SharedPreferences prefs = getSharedPreferences("MY_APP_PREFS", Context.MODE_PRIVATE);
        String token = prefs.getString("ACCESS_TOKEN", null);

        if (token == null) {
            Toast.makeText(this, "Phiên đăng nhập hết hạn, vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            return;
        }

        tvNotice.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        btnRegister.setEnabled(false);

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        ShopRegisterRequest shopRegisterRequest = new ShopRegisterRequest(shopName, description);
        
        // Truyền token vào header Authorization
        // Sử dụng ResponseBody thay vì MessageResponse để tránh lỗi parse JSON
        Call<ResponseBody> call = apiService.registerShop("Bearer " + token, shopRegisterRequest);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                progressBar.setVisibility(View.GONE);
                btnRegister.setEnabled(true);

                // Sửa logic: Chỉ cần mã phản hồi là thành công (200-299) là chấp nhận
                if (response.isSuccessful()) {
                    tvNotice.setText("Đăng ký cửa hàng thành công");
                    tvNotice.setVisibility(View.VISIBLE);
                    
                    // Cập nhật lại cache user profile vì role đã thay đổi (thành SHOP_OWNER/VENDOR)
                    ProfileActivity.invalidateProfileCache();

                    Intent intent = new Intent(RegisterShopActivity.this, ProfileActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else if (response.code() == 401) {
                    Log.d(TAG, "Lỗi xác thực (401)");
                    tvNotice.setText("Phiên đăng nhập hết hạn. Vui lòng đăng nhập lại.");
                    tvNotice.setVisibility(View.VISIBLE);
                } else if (response.code() == 409) {
                    Log.d(TAG, "Lỗi xung đột (409)");
                    tvNotice.setText("Bạn đã có cửa hàng hoặc tên cửa hàng đã tồn tại.");
                    tvNotice.setVisibility(View.VISIBLE);
                } else {
                    String errorMsg = "";
                    try {
                        if (response.errorBody() != null) {
                            errorMsg = response.errorBody().string();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Log.d(TAG, "Lỗi đăng ký: " + response.code() + " - " + errorMsg);
                    tvNotice.setText("Lỗi đăng ký (" + response.code() + "). Vui lòng thử lại.");
                    tvNotice.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnRegister.setEnabled(true);

                Log.e(TAG, "Lỗi kết nối: " + t.getMessage());
                tvNotice.setText("Lỗi kết nối: " + t.getMessage());
                tvNotice.setVisibility(View.VISIBLE);
            }
        });
    }

    private void initViews() {
        edtShopName = findViewById(R.id.edtShopName);
        edtDesc = findViewById(R.id.edtDesc);
        btnRegister = findViewById(R.id.btnRegister);
        ivBack = findViewById(R.id.ivBack);
        tvNotice = findViewById(R.id.tvNotice);
        progressBar = findViewById(R.id.progressBar);
    }
}