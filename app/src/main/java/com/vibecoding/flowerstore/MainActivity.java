package com.vibecoding.flowerstore;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.vibecoding.flowerstore.Adapter.ProductAdapter;
import com.vibecoding.flowerstore.Model.ApiResponse;
import com.vibecoding.flowerstore.Model.Product;
import com.vibecoding.flowerstore.Service.ApiService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit; // Thêm import này

import okhttp3.OkHttpClient; // Thêm import này
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private RecyclerView productRow1, productRow2;
    private ProductAdapter adapter1, adapter2;

    private static final String API_BASE_URL = "https://holetinnghia-vibe-coding-store-api.hf.space/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupViews();
        setupRecyclerViews();
        fetchProductsFromApi();
    }

    private void setupViews() {
        productRow1 = findViewById(R.id.product_row_1);
        productRow2 = findViewById(R.id.product_row_2);
    }

    private void setupRecyclerViews() {
        adapter1 = new ProductAdapter(new ArrayList<>(), this);
        productRow1.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        productRow1.setAdapter(adapter1);

        adapter2 = new ProductAdapter(new ArrayList<>(), this);
        productRow2.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        productRow2.setAdapter(adapter2);
    }

    private void fetchProductsFromApi() {
        // --- BẮT ĐẦU THAY ĐỔI ---

        // 1. Tạo một OkHttpClient với thời gian chờ tùy chỉnh
        // Ở đây chúng ta đặt thời gian chờ là 30 giây
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS) // Thời gian chờ để thiết lập kết nối
                .readTimeout(30, TimeUnit.SECONDS)    // Thời gian chờ để đọc dữ liệu
                .writeTimeout(30, TimeUnit.SECONDS)   // Thời gian chờ để ghi dữ liệu
                .build();

        // 2. Xây dựng Retrofit và gắn OkHttpClient đã cấu hình vào
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(API_BASE_URL)
                .client(okHttpClient) // Sử dụng client đã được cấu hình timeout
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        // --- KẾT THÚC THAY ĐỔI ---

        ApiService apiService = retrofit.create(ApiService.class);
        Call<ApiResponse> call = apiService.getProducts();

        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Product> allProducts = response.body().getProducts();

                    if (allProducts != null && !allProducts.isEmpty()) {
                        int midPoint = allProducts.size() / 2;
                        List<Product> firstHalf = allProducts.subList(0, midPoint);
                        List<Product> secondHalf = allProducts.subList(midPoint, allProducts.size());

                        adapter1.updateData(firstHalf);
                        adapter2.updateData(secondHalf);
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Lỗi khi tải dữ liệu", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Log.e("API_ERROR", "Lỗi kết nối: " + t.getMessage(), t);
                Toast.makeText(MainActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
