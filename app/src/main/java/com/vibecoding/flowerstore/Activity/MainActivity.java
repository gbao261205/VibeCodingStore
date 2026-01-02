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
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
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
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.lang.reflect.Type;
import android.os.Handler;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    // UI COMPONENTS
    private RecyclerView recyclerProducts;
    private ProductAdapter productAdapter;
    private RecyclerView recyclerCategories;
    private CategoryAdapter categoryAdapter;

    // SLIDER COMPONENTS (GIỮ NGUYÊN)
    private ViewPager2 viewPagerImageSlider;
    private LinearLayout bannerDotsLayout;
    private SlideAdapter slideAdapter;
    private final Handler sliderHandler = new Handler(Looper.getMainLooper());
    private Runnable sliderRunnable;

    private ShimmerFrameLayout shimmerFrameLayout;

    // NAVIGATION
    private LinearLayout navHome, navCategories, navFavorites, navAccount;
    private ImageButton cartButton;

    // SEARCH
    private EditText edtSearch;
    private View btnSearchIcon;

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        setupViews();
        setupImageSliderAndDots(); // Logic Slide của bạn
        setupRecyclerView();
        setupNavBarListeners();
        setupSearchFunctionality();

        // 1. TẢI DANH SÁCH YÊU THÍCH (Để Adapter biết cái nào tô đỏ)
        fetchWishlistRaw();

        // 2. Logic Categories
        fetchCategoriesFromApi();

        // 3. Logic Products
        if (DataStore.cachedProducts != null && !DataStore.cachedProducts.isEmpty()) {
            productAdapter.updateData(DataStore.cachedProducts);
            stopShimmer();
        } else {
            startShimmer();
        }
        fetchHomeProductsMixed();
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
        edtSearch = findViewById(R.id.edt_search);
        btnSearchIcon = findViewById(R.id.btn_search_icon);
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
            if (currentItem == slideItems.size() - 1) viewPagerImageSlider.setCurrentItem(0, true);
            else viewPagerImageSlider.setCurrentItem(currentItem + 1);
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
        if (dots.length > 0) dots[0].setImageDrawable(ContextCompat.getDrawable(this, R.drawable.dot_active));
    }

    private void updateBannerDots(int currentPage) {
        int childCount = bannerDotsLayout.getChildCount();
        for (int i = 0; i < childCount; i++) {
            ImageView imageView = (ImageView) bannerDotsLayout.getChildAt(i);
            if (i == currentPage) imageView.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.dot_active));
            else imageView.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.dot_inactive));
        }
    }
    // --- HẾT LOGIC SLIDER ---

    // --- HÀM TẢI WISHLIST (MỚI - DÙNG ResponseBody ĐỂ TRÁNH LỖI) ---
    private void fetchWishlistRaw() {
        SharedPreferences prefs = getSharedPreferences("MY_APP_PREFS", Context.MODE_PRIVATE);
        String token = prefs.getString("ACCESS_TOKEN", null);
        if (token == null) return;

        ApiService apiService = RetrofitClient.getClient(this).create(ApiService.class);

        // Bạn cần đảm bảo trong ApiService có: @GET("api/v1/products?sort=wishlisted") Call<ResponseBody> getWishlistRaw();
        // Hoặc dùng tạm @GET để test:
        // Lưu ý: apiService.getProductsByCategory("wishlisted") của bạn trả về ApiResponse bị lỗi
        // Nên mình sẽ dùng Call<ResponseBody> để lấy dữ liệu thô và tự parse.

        // Vui lòng thêm vào ApiService: @GET("api/v1/products?sort=wishlisted") Call<ResponseBody> getRawWishlist();
        // Nếu không thêm được, hãy bỏ qua hàm này, nhưng nút tim sẽ không đỏ khi mới vào app.
    }

    private void setupRecyclerView() {
        productAdapter = new ProductAdapter(new ArrayList<>(), this);
        recyclerProducts.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerProducts.setAdapter(productAdapter);

        categoryAdapter = new CategoryAdapter(new ArrayList<>(), this, category -> {
            Intent intent = new Intent(MainActivity.this, CategoryProductsActivity.class);
            intent.putExtra("category_slug", category.getSlug());
            startActivity(intent);
        });
        recyclerCategories.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerCategories.setAdapter(categoryAdapter);
    }

    private void fetchHomeProductsMixed() {
        ApiService apiService = RetrofitClient.getClient(this).create(ApiService.class);
        apiService.getHomeProducts().enqueue(new Callback<Map<String, List<ProductDTO>>>() {
            @Override
            public void onResponse(Call<Map<String, List<ProductDTO>>> call, Response<Map<String, List<ProductDTO>>> response) {
                stopShimmer();
                if (response.isSuccessful() && response.body() != null) {
                    List<Product> allProducts = new ArrayList<>();
                    for (List<ProductDTO> dtoList : response.body().values()) {
                        for (ProductDTO dto : dtoList) {
                            Product p = new Product();
                            p.setId(dto.getId()); p.setName(dto.getName()); p.setImage(dto.getImageUrl());
                            if (dto.getPrice() != null) p.setPrice(dto.getPrice().doubleValue());
                            allProducts.add(p);
                        }
                    }
                    DataStore.cachedProducts = allProducts;
                    productAdapter.updateData(allProducts);
                }
            }
            @Override public void onFailure(Call<Map<String, List<ProductDTO>>> call, Throwable t) { stopShimmer(); }
        });
    }

    private void fetchCategoriesFromApi() {
        ApiService apiService = RetrofitClient.getClient(this).create(ApiService.class);
        apiService.getCategories().enqueue(new Callback<List<Category>>() {
            @Override public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                if (response.isSuccessful() && response.body() != null) categoryAdapter.updateData(response.body());
            }
            @Override public void onFailure(Call<List<Category>> call, Throwable t) {}
        });
    }

    // Navigation & UI Helpers
    private void setupNavBarListeners() {
        navHome.setOnClickListener(this); navCategories.setOnClickListener(this);
        navFavorites.setOnClickListener(this); navAccount.setOnClickListener(this);
        cartButton.setOnClickListener(this);
    }

    @Override public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.nav_home) return;
        if (id == R.id.nav_categories) startActivity(new Intent(this, CategoriesActivity.class));
        if (id == R.id.nav_favorites) startActivity(new Intent(this, FavoriteActivity.class));
        if (id == R.id.nav_account) startActivity(new Intent(this, ProfileActivity.class));
        if (id == R.id.button_cart) startActivity(new Intent(this, CartActivity.class));
    }

    private void setupSearchFunctionality() {
        if (edtSearch != null) {
            edtSearch.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) { navigateToSearch(); return true; }
                return false;
            });
        }
        if (btnSearchIcon != null) btnSearchIcon.setOnClickListener(v -> navigateToSearch());
    }

    private void navigateToSearch() {
        String keyword = edtSearch.getText().toString().trim();
        if (!keyword.isEmpty()) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.hideSoftInputFromWindow(edtSearch.getWindowToken(), 0);
            Intent intent = new Intent(this, SearchActivity.class);
            intent.putExtra("SEARCH_KEYWORD", keyword);
            startActivity(intent);
        }
    }

    private void startShimmer() { shimmerFrameLayout.setVisibility(View.VISIBLE); shimmerFrameLayout.startShimmer(); recyclerProducts.setVisibility(View.GONE); }
    private void stopShimmer() { shimmerFrameLayout.stopShimmer(); shimmerFrameLayout.setVisibility(View.GONE); recyclerProducts.setVisibility(View.VISIBLE); }

    @Override protected void onResume() { super.onResume(); sliderHandler.postDelayed(sliderRunnable, 3000); }
    @Override protected void onPause() { super.onPause(); sliderHandler.removeCallbacks(sliderRunnable); }
}