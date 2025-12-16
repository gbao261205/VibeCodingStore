package com.vibecoding.flowerstore.Activity; // Thay bằng package thực tế của bạn

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;
import com.vibecoding.flowerstore.Model.User;
import com.vibecoding.flowerstore.R;
import com.vibecoding.flowerstore.Service.ApiService;
import com.vibecoding.flowerstore.Service.EditProfileRequest;
import com.vibecoding.flowerstore.Service.RetrofitClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProfileActivity extends AppCompatActivity {

    // Khai báo View theo đúng ID trong Layout XML
    private ImageView btnBack, ivAvatar;
    private EditText edtName, edtPhone, edtEmail;
    private TextView tvNameDisplay, tvEmailDisplay;
    private MaterialButton btnSave;

    // Biến lưu Uri ảnh được chọn
    private Uri selectedImageUri = null;

    // Launcher chọn ảnh mới
    private final ActivityResultLauncher<String> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    // Dùng Glide để hiển thị ảnh vừa chọn, có bo tròn
                    Glide.with(this).load(uri).circleCrop().into(ivAvatar);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile); // Đảm bảo tên layout đúng

        initViews();
        setupListeners();

        // Lấy dữ liệu User được truyền từ ProfileActivity và hiển thị lên UI
        if (getIntent().hasExtra("user")) {
            User user = (User) getIntent().getSerializableExtra("user");
            if (user != null) {
                populateUserData(user);
            } else {
                Toast.makeText(this, "Lỗi: Không nhận được dữ liệu người dùng.", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            // Nếu không có dữ liệu, không thể hoạt động -> đóng Activity
            Toast.makeText(this, "Lỗi: Không tìm thấy dữ liệu để chỉnh sửa.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        ivAvatar = findViewById(R.id.ivAvatar);

        tvNameDisplay = findViewById(R.id.tvNameDisplay);
        tvEmailDisplay = findViewById(R.id.tvEmailDisplay);

        edtName = findViewById(R.id.edtName);
        edtPhone = findViewById(R.id.edtPhone);
        edtEmail = findViewById(R.id.edtEmail); // Readonly

        btnSave = findViewById(R.id.btnSave);
    }

    private void populateUserData(User user) {
        // Hiển thị dữ liệu lên các View
        tvNameDisplay.setText(user.getFullName());
        edtName.setText(user.getFullName());
        edtPhone.setText(user.getPhoneNumber());

        // --- SỬA LỖI HIỂN THỊ EMAIL ---
        String encodedEmail = user.getEmail();
        if (encodedEmail != null) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                tvEmailDisplay.setText(Html.fromHtml(encodedEmail, Html.FROM_HTML_MODE_LEGACY));
                edtEmail.setText(Html.fromHtml(encodedEmail, Html.FROM_HTML_MODE_LEGACY));
            } else {
                tvEmailDisplay.setText(Html.fromHtml(encodedEmail));
                edtEmail.setText(Html.fromHtml(encodedEmail));
            }
        }
        // --- KẾT THÚC SỬA LỖI ---

        // Dùng Glide để tải và hiển thị ảnh đại diện, có bo tròn
        Glide.with(this)
                .load(user.getAvatar())
                .circleCrop() // Bo tròn ảnh
                .placeholder(R.drawable.ic_person) // Ảnh mặc định trong lúc tải
                .error(R.drawable.ic_person)       // Ảnh mặc định nếu lỗi
                .into(ivAvatar);
    }

    private void setupListeners() {
        // 1. Back button
        btnBack.setOnClickListener(v -> finish());

        // 2. Chọn ảnh avatar
        ivAvatar.setOnClickListener(v -> pickImageLauncher.launch("image/*"));

        // 3. Nút Lưu
        btnSave.setOnClickListener(v -> handleUpdateProfile());
    }

    private void handleUpdateProfile() {
        String fullName = edtName.getText().toString().trim();
        String phoneNumber = edtPhone.getText().toString().trim();

        if (fullName.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ tên", Toast.LENGTH_SHORT).show();
            return;
        }

        // --- BƯỚC 1: Xử lý phần JSON (request) ---
        EditProfileRequest profileRequest = new EditProfileRequest(fullName, phoneNumber);
        String jsonString = new Gson().toJson(profileRequest);

        // Tạo RequestBody với type là application/json cho part "request"
        RequestBody requestPart = RequestBody.create(
                MediaType.parse("application/json; charset=utf-8"),
                jsonString
        );

        // --- BƯỚC 2: Xử lý phần File Ảnh (avatarFile) ---
        MultipartBody.Part avatarFilePart = null;

        if (selectedImageUri != null) {
            File file = getFileFromUri(selectedImageUri);
            if (file != null) {
                // Tạo RequestBody cho file ảnh
                RequestBody requestFile = RequestBody.create(
                        MediaType.parse(getContentResolver().getType(selectedImageUri)),
                        file
                );
                // "avatarFile" phải khớp với key Server yêu cầu
                avatarFilePart = MultipartBody.Part.createFormData("avatarFile", file.getName(), requestFile);
            }
        }

        // --- BƯỚC 3: Gọi API thông qua RetrofitClient ---
        // Sử dụng getClient(this) để Retrofit tự động gắn Token
        ApiService apiService = RetrofitClient.getClient(this).create(ApiService.class);

        apiService.updateProfile(requestPart, avatarFilePart).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(EditProfileActivity.this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                    ProfileActivity.invalidateProfileCache();
                    finish();
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                        Toast.makeText(EditProfileActivity.this, "Lỗi: " + response.code() + " - " + errorBody, Toast.LENGTH_LONG).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(EditProfileActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("API_ERROR", t.getMessage());
            }
        });
    }

    // --- Helper: Chuyển Uri thành File thực tế (Bắt buộc cho Android 10+) ---
    private File getFileFromUri(Uri uri) {
        try {
            String fileName = getFileName(uri);
            File tempFile = new File(getCacheDir(), fileName);

            InputStream inputStream = getContentResolver().openInputStream(uri);
            OutputStream outputStream = new FileOutputStream(tempFile);

            if (inputStream != null) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }
                outputStream.close();
                inputStream.close();
            }
            return tempFile;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if(index >= 0) result = cursor.getString(index);
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result != null ? result : "temp_avatar.jpg";
    }
}
