package com.vibecoding.flowerstore.Activity;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.vibecoding.flowerstore.Adapter.FeedbackAdapter;
import com.vibecoding.flowerstore.Model.Review;
import com.vibecoding.flowerstore.R;
import com.vibecoding.flowerstore.Service.ApiService;
import com.vibecoding.flowerstore.Service.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AllReviewsActivity extends AppCompatActivity {

    private RecyclerView recyclerAllReviews;
    private FeedbackAdapter adapter;
    private ImageView btnBack;
    private int productId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_reviews);

        productId = getIntent().getIntExtra("product_id", -1);

        btnBack = findViewById(R.id.btn_back_reviews);
        recyclerAllReviews = findViewById(R.id.recycler_all_reviews);

        btnBack.setOnClickListener(v -> finish());

        setupRecyclerView();
        fetchAllReviews();
    }

    private void setupRecyclerView() {
        adapter = new FeedbackAdapter(new ArrayList<>(), this);
        recyclerAllReviews.setLayoutManager(new LinearLayoutManager(this));
        recyclerAllReviews.setAdapter(adapter);
    }

    private void fetchAllReviews() {
        if (productId == -1) return;

        ApiService apiService = RetrofitClient.getClient(this).create(ApiService.class);
        apiService.getProductReviews(productId).enqueue(new Callback<List<Review>>() {
            @Override
            public void onResponse(Call<List<Review>> call, Response<List<Review>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapter.updateData(response.body());
                }
            }

            @Override
            public void onFailure(Call<List<Review>> call, Throwable t) {
                Toast.makeText(AllReviewsActivity.this, "Lỗi tải đánh giá", Toast.LENGTH_SHORT).show();
            }
        });
    }
}