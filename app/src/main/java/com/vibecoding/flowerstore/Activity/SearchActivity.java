package com.vibecoding.flowerstore.Activity;

import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.vibecoding.flowerstore.Adapter.ProductAdapter;
import com.vibecoding.flowerstore.Model.ApiResponse;
import com.vibecoding.flowerstore.Model.Product;
import com.vibecoding.flowerstore.R;
import com.vibecoding.flowerstore.Service.ApiService;
import com.vibecoding.flowerstore.Service.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchActivity extends AppCompatActivity {

    private RecyclerView recyclerSearch;
    private ProductAdapter adapter;
    private TextView tvTitle;
    private ImageButton btnBack;
    private ProgressBar progressBar;
    private TextView tvNoResult; // Text thông báo nếu không tìm thấy
    private String keyword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Fullscreen setup
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_search); // Bạn cần tạo layout này (giống layout category)

        // 1. Nhận từ khóa tìm kiếm
        keyword = getIntent().getStringExtra("SEARCH_KEYWORD");

        setupViews();
        setupRecyclerView();

        if (keyword != null && !keyword.isEmpty()) {
            tvTitle.setText("Kết quả cho: \"" + keyword + "\"");
            performSearch(keyword);
        }
    }

    private void setupViews() {
        recyclerSearch = findViewById(R.id.recycler_search_products);
        tvTitle = findViewById(R.id.tv_search_title);
        btnBack = findViewById(R.id.btn_back);
        progressBar = findViewById(R.id.progress_bar);
        tvNoResult = findViewById(R.id.tv_no_result); // Thêm cái này vào XML nha

        btnBack.setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new ProductAdapter(new ArrayList<>(), this);
        // Grid 2 cột
        recyclerSearch.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerSearch.setAdapter(adapter);
    }

    private void performSearch(String keyword) {
        progressBar.setVisibility(View.VISIBLE);

        // 1. Chuẩn hóa từ khóa: chuyển về chữ thường, xóa khoảng trắng thừa
        String searchKey = keyword.toLowerCase().trim();

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);

        // Gọi API lấy dữ liệu (cứ lấy về hết rồi mình lọc sau)
        apiService.getProducts(keyword, 0, 100, "newest").enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    // Danh sách gốc từ API (đang bị lẫn lộn)
                    List<Product> rawList = response.body().getProducts();

                    // Tạo danh sách mới để chứa kết quả đã lọc
                    List<Product> filteredList = new ArrayList<>();

                    if (rawList != null) {
                        // --- ĐOẠN LỌC QUAN TRỌNG ---
                        for (Product p : rawList) {
                            // Chỉ lấy những sản phẩm mà TÊN CỦA NÓ có chứa từ khóa
                            if (p.getName() != null && p.getName().toLowerCase().contains(searchKey)) {
                                filteredList.add(p);
                            }
                        }
                    }

                    // Hiển thị danh sách ĐÃ LỌC (filteredList) thay vì danh sách gốc
                    if (!filteredList.isEmpty()) {
                        adapter.updateData(filteredList);
                        recyclerSearch.setVisibility(View.VISIBLE);
                        tvNoResult.setVisibility(View.GONE);
                    } else {
                        // Không có kết quả sau khi lọc
                        recyclerSearch.setVisibility(View.GONE);
                        tvNoResult.setVisibility(View.VISIBLE);
                        tvNoResult.setText("Không tìm thấy: " + keyword);
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(SearchActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }
}