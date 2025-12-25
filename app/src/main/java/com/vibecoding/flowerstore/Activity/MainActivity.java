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
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.view.View;
import android.content.Intent;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.facebook.shimmer.ShimmerFrameLayout;

import com.vibecoding.flowerstore.Adapter.ProductAdapter;
import com.vibecoding.flowerstore.Adapter.SlideAdapter;
import com.vibecoding.flowerstore.Model.DataStore;
import com.vibecoding.flowerstore.Model.Product;
import com.vibecoding.flowerstore.Model.ProductDTO;
import com.vibecoding.flowerstore.Model.Category;
import com.vibecoding.flowerstore.Adapter.CategoryAdapter;
import com.vibecoding.flowerstore.Model.SlideItem;
import com.vibecoding.flowerstore.R;
import com.vibecoding.flowerstore.Service.ApiService;
import com.vibecoding.flowerstore.Service.RetrofitClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import android.os.Handler;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private RecyclerView recyclerProducts;
    private ProductAdapter productAdapter;
    private RecyclerView recyclerCategories;
    private CategoryAdapter categoryAdapter;

    private ViewPager2 viewPagerImageSlider;
    private LinearLayout bannerDotsLayout;
    private SlideAdapter slideAdapter;
    private final Handler sliderHandler = new Handler(Looper.getMainLooper());
    private Runnable sliderRunnable;

    // --- BIẾN SHIMMER ---
    private ShimmerFrameLayout shimmerFrameLayout;

    private LinearLayout navHome;
    private LinearLayout navCategories;
    private LinearLayout navFavorites;
    private LinearLayout navAccount;
    private ImageButton cartButton;

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Cấu hình Fullscreen (Ẩn thanh trạng thái nếu muốn)
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);

        setupViews();
        setupImageSliderAndDots();
        setupRecyclerView();

        // 1. Logic Categories (Cache trước -> API sau)
        fetchCategoriesFromApi();

        // 2. Logic Products (Cache trước -> Shimmer/API sau)
        if (DataStore.cachedProducts != null && !DataStore.cachedProducts.isEmpty()) {
            productAdapter.updateData(DataStore.cachedProducts);
            stopShimmer();
        } else {
            startShimmer();
        }

        // Luôn gọi API để làm mới dữ liệu
        fetchHomeProductsMixed();

        setupNavBarListeners();
    }

    private void setupViews() {
        viewPagerImageSlider = findViewById(R.id.view_pager_image_slider);
        bannerDotsLayout = findViewById(R.id.banner_dots);
        recyclerProducts = findViewById(R.id.recycler_products);
        recyclerCategories = findViewById(R.id.recycler_categories);
        shimmerFrameLayout = findViewById(R.id.shimmer_view_container);

        navHome = findViewById(R.id.nav_home);
        navCategories = findViewById(R.id.nav_categories);
        navFavorites = findViewById(R.id.nav_favorites);
        navAccount = findViewById(R.id.nav_account);
        cartButton = findViewById(R.id.button_cart);
    }

    // --- SHIMMER CONTROLS ---
    private void startShimmer() {
        if (shimmerFrameLayout != null) {
            shimmerFrameLayout.setVisibility(View.VISIBLE);
            shimmerFrameLayout.startShimmer();
            recyclerProducts.setVisibility(View.GONE);
        }
    }

    private void stopShimmer() {
        if (shimmerFrameLayout != null) {
            shimmerFrameLayout.stopShimmer();
            shimmerFrameLayout.setVisibility(View.GONE);
            recyclerProducts.setVisibility(View.VISIBLE);
        }
    }

    // --- SỬA LỖI MAPPING DỮ LIỆU TẠI ĐÂY ---
    private void fetchHomeProductsMixed() {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<Map<String, List<ProductDTO>>> call = apiService.getHomeProducts();

        call.enqueue(new Callback<Map<String, List<ProductDTO>>>() {
            @Override
            public void onResponse(Call<Map<String, List<ProductDTO>>> call, Response<Map<String, List<ProductDTO>>> response) {
                stopShimmer();

                if (response.isSuccessful() && response.body() != null) {
                    Map<String, List<ProductDTO>> mapData = response.body();
                    List<Product> allProducts = new ArrayList<>();

                    for (List<ProductDTO> dtoList : mapData.values()) {
                        for (ProductDTO dto : dtoList) {
                            Product p = new Product();

                            // Map ID & Name
                            p.setId(dto.getId());
                            p.setName(dto.getName());

                            // Map Image (Dùng getImageUrl từ DTO)
                            p.setImage(dto.getImageUrl());

                            // Map Price (Chuyển BigDecimal -> double)
                            if (dto.getPrice() != null) {
                                p.setPrice(dto.getPrice().doubleValue());
                            }
                            if (dto.getDiscountedPrice() != null) {
                                p.setDiscountedPrice(dto.getDiscountedPrice().doubleValue());
                            }

                            // *** QUAN TRỌNG: Map Stock và Active để không bị báo hết hàng ***
                            p.setStock(dto.getStock());
                            p.setActive(dto.isActive());

                            // Map Shop và Category nếu cần thiết (và nếu DTO có trả về)
                            // p.setCategory(convertCategoryDtoToModel(dto.getCategory()));

                            allProducts.add(p);
                        }
                    }

                    if (!allProducts.isEmpty()) {
                        productAdapter.updateData(allProducts);
                        DataStore.cachedProducts = allProducts;
                    }
                } else {
                    Log.e(TAG, "Lỗi API: " + response.code());
                    if (DataStore.cachedProducts == null || DataStore.cachedProducts.isEmpty()) {
                        Toast.makeText(MainActivity.this, "Không tải được sản phẩm", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<Map<String, List<ProductDTO>>> call, Throwable t) {
                stopShimmer();
                Log.e(TAG, "Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    private void fetchCategoriesFromApi() {
        if (DataStore.cachedCategories != null && !DataStore.cachedCategories.isEmpty()) {
            categoryAdapter.updateData(DataStore.cachedCategories);
            return;
        }

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<List<Category>> call = apiService.getCategories();

        call.enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Category> categories = response.body();
                    if (!categories.isEmpty()) {
                        DataStore.cachedCategories = categories;
                        categoryAdapter.updateData(categories);
                    }
                }
            }
            @Override
            public void onFailure(Call<List<Category>> call, Throwable t) {
                Log.e(TAG, "Lỗi Categories: " + t.getMessage());
            }
        });
    }

    // --- SETUP UI KHÁC ---
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

    private void setupNavBarListeners() {
        navHome.setOnClickListener(this);
        navCategories.setOnClickListener(this);
        navFavorites.setOnClickListener(this);
        navAccount.setOnClickListener(this);
        cartButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        Intent intent = null;

        if (id == R.id.button_cart) {
            SharedPreferences prefs = getSharedPreferences("MY_APP_PREFS", Context.MODE_PRIVATE);
            String token = prefs.getString("ACCESS_TOKEN", null);
            if (token == null) {
                Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            } else {
                intent = new Intent(this, CartActivity.class);
                startActivity(intent);
                // Cart thường là activity con, không cần finish Main
            }
            return;
        }

        if (id == R.id.nav_home) {
            return; // Đang ở Home
        }
        else if (id == R.id.nav_categories) {
            intent = new Intent(this, CategoriesActivity.class);
        }
        else if (id == R.id.nav_favorites) {
            intent = new Intent(this, FavoriteActivity.class);
        }
        else if (id == R.id.nav_account) {
            intent = new Intent(this, ProfileActivity.class);
        }

        if (intent != null) {
            // Thêm cờ NO_ANIMATION
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);

            startActivity(intent);

            // Tắt hiệu ứng chuyển cảnh ngay lập tức
            overridePendingTransition(0, 0);

            // Đóng Activity hiện tại
            finish();
        }
    }

    // --- PHẦN SLIDER (GIỮ NGUYÊN) ---
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
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
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
        // Tắt hiệu ứng khi thoát Activity này (quan trọng để chống nhấp nháy)
        overridePendingTransition(0, 0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sliderHandler.postDelayed(sliderRunnable, 3000);
    }
}