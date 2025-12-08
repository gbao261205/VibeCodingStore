package com.vibecoding.flowerstore.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.vibecoding.flowerstore.Adapter.ProductAdapter;
import com.vibecoding.flowerstore.Adapter.SlideAdapter;
import com.vibecoding.flowerstore.Model.ApiResponse;
import com.vibecoding.flowerstore.Model.Product;
import com.vibecoding.flowerstore.Model.Category;
import com.vibecoding.flowerstore.Adapter.CategoryAdapter;
import com.vibecoding.flowerstore.Model.SlideItem;
import com.vibecoding.flowerstore.R;
import com.vibecoding.flowerstore.Service.ApiClient;
import com.vibecoding.flowerstore.Service.APIService;

import java.util.ArrayList;
import java.util.List;
import android.os.Handler;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerProducts;
    private ProductAdapter productAdapter;
    private RecyclerView recyclerCategories;
    private CategoryAdapter categoryAdapter;

    private ViewPager2 viewPagerImageSlider;
    private LinearLayout bannerDotsLayout;
    private SlideAdapter slideAdapter;
    private final Handler sliderHandler = new Handler(Looper.getMainLooper());
    private Runnable sliderRunnable;
    private static final String TAG = "MainActivity";

    // Thêm các biến cho nút điều hướng
    private LinearLayout homeButton, categoryButton, favoriteButton, profileButton;
    private ImageButton cartButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupViews();
        setupNavigation(); // Thêm hàm để cài đặt điều hướng
        setupImageSliderAndDots();
        setupRecyclerView();
        fetchProductsFromApi();
        fetchCategoriesFromApi();
    }

    private void setupViews() {
        viewPagerImageSlider = findViewById(R.id.view_pager_image_slider);
        bannerDotsLayout = findViewById(R.id.banner_dots);
        recyclerProducts = findViewById(R.id.recycler_products);
        recyclerCategories = findViewById(R.id.recycler_categories);
        cartButton = findViewById(R.id.button_cart); // Ánh xạ nút giỏ hàng

        // Ánh xạ các nút điều hướng
        homeButton = findViewById(R.id.home_button);
        categoryButton = findViewById(R.id.category_button);
        favoriteButton = findViewById(R.id.favorite_button);
        profileButton = findViewById(R.id.profile_button);
    }

    private void setupNavigation() {
        profileButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        cartButton.setOnClickListener(v -> {
            SharedPreferences prefs = getSharedPreferences("MY_APP_PREFS", Context.MODE_PRIVATE);
            if (prefs.getString("ACCESS_TOKEN", null) != null) {
                // Đã đăng nhập -> Mở giỏ hàng
                startActivity(new Intent(MainActivity.this, CartActivity.class));
            } else {
                // Chưa đăng nhập -> Yêu cầu đăng nhập
                Toast.makeText(this, "Vui lòng đăng nhập để xem giỏ hàng", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
            }
        });

        homeButton.setOnClickListener(v -> Toast.makeText(MainActivity.this, "Bạn đang ở Trang chủ", Toast.LENGTH_SHORT).show());
        categoryButton.setOnClickListener(v -> Toast.makeText(MainActivity.this, "Chức năng Danh mục sắp có!", Toast.LENGTH_SHORT).show());
        favoriteButton.setOnClickListener(v -> Toast.makeText(MainActivity.this, "Chức năng Yêu thích sắp có!", Toast.LENGTH_SHORT).show());
    }

    private void setupImageSliderAndDots() {
        List<SlideItem> slideItems = new ArrayList<>();
        slideItems.add(new SlideItem(R.drawable.banner1));
        slideItems.add(new SlideItem(R.drawable.banner2));
        slideItems.add(new SlideItem(R.drawable.banner3));

        slideAdapter = new SlideAdapter(slideItems);
        viewPagerImageSlider.setAdapter(slideAdapter);

        CompositePageTransformer transformer = new CompositePageTransformer();
        transformer.addTransformer(new MarginPageTransformer(40));
        transformer.addTransformer((page, position) -> {
            float r = 1 - Math.abs(position);
            page.setScaleY(0.85f + r * 0.15f);
        });
        viewPagerImageSlider.setPageTransformer(transformer);

        setupBannerDots(slideItems.size());

        viewPagerImageSlider.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateBannerDots(position);
                sliderHandler.removeCallbacks(sliderRunnable);
                sliderHandler.postDelayed(sliderRunnable, 3000);
            }
        });

        sliderRunnable = () -> {
            int currentItem = viewPagerImageSlider.getCurrentItem();
            if (currentItem == slideItems.size() - 1) {
                viewPagerImageSlider.setCurrentItem(0, true);
            } else {
                viewPagerImageSlider.setCurrentItem(currentItem + 1);
            }
        };
    }

    private void setupBannerDots(int count) {
        ImageView[] dots = new ImageView[count];
        bannerDotsLayout.removeAllViews();

        for (int i = 0; i < dots.length; i++) {
            dots[i] = new ImageView(this);
            dots[i].setImageDrawable(ContextCompat.getDrawable(this, R.drawable.dot_inactive));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(8, 0, 8, 0);
            bannerDotsLayout.addView(dots[i], params);
        }

        if (dots.length > 0) {
            dots[0].setImageDrawable(ContextCompat.getDrawable(this, R.drawable.dot_active));
        }
    }

    private void updateBannerDots(int currentPage) {
        int childCount = bannerDotsLayout.getChildCount();
        for (int i = 0; i < childCount; i++) {
            ImageView imageView = (ImageView) bannerDotsLayout.getChildAt(i);
            if (i == currentPage) {
                imageView.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.dot_active));
            } else {
                imageView.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.dot_inactive));
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        sliderHandler.removeCallbacks(sliderRunnable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sliderHandler.postDelayed(sliderRunnable, 3000);
    }

    private void setupRecyclerView() {
        productAdapter = new ProductAdapter(new ArrayList<>(), this);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        recyclerProducts.setLayoutManager(layoutManager);
        recyclerProducts.setAdapter(productAdapter);

        categoryAdapter = new CategoryAdapter(new ArrayList<>(), this);
        LinearLayoutManager categoryLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerCategories.setLayoutManager(categoryLayoutManager);
        recyclerCategories.setAdapter(categoryAdapter);
    }

    private void fetchCategoriesFromApi() {
        APIService apiService = ApiClient.getClient().create(APIService.class);
        Call<List<Category>> call = apiService.getCategories();

        call.enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Category> categories = response.body();
                    if (!categories.isEmpty()) {
                        categoryAdapter.updateData(categories);
                        Log.d(TAG, "Tải thành công " + categories.size() + " danh mục.");
                    } else {
                        Log.d(TAG, "API không trả về danh mục nào.");
                    }
                } else {
                    Log.e(TAG, "Lỗi API Categories: " + response.code());
                    Toast.makeText(MainActivity.this, "Lỗi tải danh mục", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Category>> call, Throwable t) {
                Log.e(TAG, "Lỗi kết nối khi lấy danh mục: " + t.getMessage());
                Toast.makeText(MainActivity.this, "Lỗi mạng khi tải danh mục", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchProductsFromApi() {
        APIService apiService = ApiClient.getClient().create(APIService.class);

        String query = "";
        String page = "";
        String size = "";
        String sortBy = "bestselling";

        Call<ApiResponse> call = apiService.getProducts(query, page, size, sortBy);

        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Product> bestsellingProducts = response.body().getProducts();

                    if (bestsellingProducts != null && !bestsellingProducts.isEmpty()) {
                        productAdapter.updateData(bestsellingProducts);
                        Log.d(TAG, "Tải và hiển thị thành công " + bestsellingProducts.size() + " sản phẩm nổi bật.");
                    } else {
                        Log.d(TAG, "API không trả về sản phẩm nào cho query 'sort=" + sortBy + "'.");
                        Toast.makeText(MainActivity.this, "Hiện không có sản phẩm nổi bật nào", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    try {
                        Log.e(TAG, "Lỗi API: " + response.code() + " - " + response.errorBody().string());
                    } catch (Exception e) {
                        Log.e(TAG, "Lỗi khi đọc errorBody", e);
                    }
                    Toast.makeText(MainActivity.this, "Lỗi khi tải dữ liệu từ server", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Log.e(TAG, "Lỗi kết nối: " + t.getMessage(), t);
                Toast.makeText(MainActivity.this, "Lỗi kết nối mạng, vui lòng thử lại", Toast.LENGTH_LONG).show();
            }
        });
    }
}
