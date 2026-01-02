package com.vibecoding.flowerstore.Activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.vibecoding.flowerstore.Adapter.FeedbackAdapter;
import com.vibecoding.flowerstore.Model.CartDTO;
import com.vibecoding.flowerstore.Model.DataStore;
import com.vibecoding.flowerstore.Model.Product;
import com.vibecoding.flowerstore.Model.Review;
import com.vibecoding.flowerstore.R;
import com.vibecoding.flowerstore.Service.ApiService;
import com.vibecoding.flowerstore.Service.RetrofitClient;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductDetailActivity extends AppCompatActivity {

    private static final String TAG = "ProductDetailActivity";

    // Views
    private ImageView imgProduct, btnBack, btnDecrease, btnIncrease;
    private ImageButton btnFavorite;
    private TextView tvName, tvPrice, tvSupplier, tvDescription, tvQuantity, tvStockStatus;
    private Button btnAddToCart;

    // Feedback Views
    private RecyclerView recyclerFeedback;
    private FeedbackAdapter feedbackAdapter;
    private TextView tvSeeAllReviews;

    // Data
    private Product currentProduct;
    private int quantity = 1;
    private boolean isFavorite = false;
    private int productId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_product_detail);

        initViews();

        // 1. Nhận dữ liệu và Load thông tin sản phẩm
        productId = getIntent().getIntExtra("product_id", -1);
        loadProductData();

        // 2. Setup RecyclerView cho Feedback
        setupFeedbackRecyclerView();

        // 3. Gọi API lấy đánh giá sản phẩm
        if (productId != -1) {
            fetchReviews(productId);
        }

        setupEvents();
    }

    private void initViews() {
        imgProduct = findViewById(R.id.img_detail_product);
        btnBack = findViewById(R.id.btn_back);
        tvName = findViewById(R.id.tv_detail_name);
        tvPrice = findViewById(R.id.tv_detail_price);
        tvSupplier = findViewById(R.id.tv_supplier);
        tvDescription = findViewById(R.id.tv_detail_description);
        tvStockStatus = findViewById(R.id.tv_stock_status);
        btnFavorite = findViewById(R.id.btn_favorite_detail);

        btnDecrease = findViewById(R.id.btn_decrease);
        btnIncrease = findViewById(R.id.btn_increase);
        tvQuantity = findViewById(R.id.tv_quantity);
        btnAddToCart = findViewById(R.id.btn_add_to_cart);

        // Feedback
        recyclerFeedback = findViewById(R.id.recycler_feedback);
        tvSeeAllReviews = findViewById(R.id.tv_see_all_reviews);
    }

    private void setupFeedbackRecyclerView() {
        feedbackAdapter = new FeedbackAdapter(new ArrayList<>(), this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerFeedback.setLayoutManager(layoutManager);
        recyclerFeedback.setAdapter(feedbackAdapter);
        recyclerFeedback.setNestedScrollingEnabled(false); // Để scroll mượt cùng NestedScrollView
    }

    private void loadProductData() {
        if (productId != -1) {
            // A. Tìm trong cache danh sách sản phẩm (Home)
            if (DataStore.cachedProducts != null) {
                for (Product p : DataStore.cachedProducts) {
                    if (p.getId() == productId) {
                        currentProduct = p;
                        break;
                    }
                }
            }
            // B. Nếu chưa thấy, tìm trong cache yêu thích
            if (currentProduct == null && DataStore.cachedFavorites != null) {
                for (Product p : DataStore.cachedFavorites) {
                    if (p.getId() == productId) {
                        currentProduct = p;
                        break;
                    }
                }
            }
        }

        if (currentProduct != null) {
            // --- BIND DATA ---
            tvName.setText(currentProduct.getName());
            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            tvPrice.setText(formatter.format(currentProduct.getPrice()));

            Glide.with(this)
                    .load(currentProduct.getImage())
                    .placeholder(R.drawable.banner1) // Ảnh mặc định nếu lỗi
                    .into(imgProduct);

            // Supplier
            if (currentProduct.getShop() != null) {
                tvSupplier.setText("Cung cấp bởi: " + currentProduct.getShop().getName());
            } else {
                tvSupplier.setText("Cung cấp bởi: VibeStore");
            }

            // Stock Status
            int stock = currentProduct.getStock();
            if (stock > 0) {
                tvStockStatus.setText("Tình trạng: Còn hàng (" + stock + " sản phẩm)");
                tvStockStatus.setTextColor(android.graphics.Color.parseColor("#4CAF50"));
                btnAddToCart.setEnabled(true);
                btnAddToCart.setAlpha(1.0f);
            } else {
                tvStockStatus.setText("Tình trạng: Hết hàng");
                tvStockStatus.setTextColor(android.graphics.Color.RED);
                btnAddToCart.setEnabled(false);
                btnAddToCart.setAlpha(0.5f);
            }

            // Description (Giả lập nếu null)
            String desc = "Sản phẩm " + currentProduct.getName() + " chất lượng cao.\n\n" +
                    "- Xuất xứ: Việt Nam\n" +
                    "- Bảo quản: Nơi khô ráo, thoáng mát\n" +
                    "- Hạn sử dụng: Xem trên bao bì.\n\n" +
                    "Liên hệ cửa hàng để biết thêm chi tiết.";

            // Nếu sản phẩm có trường description thật thì dùng (cần thêm field vào Product model)
            // tvDescription.setText(currentProduct.getDescription() != null ? currentProduct.getDescription() : desc);
            tvDescription.setText(desc);

            // Check Favorite Status
            checkFavoriteStatus();

        } else {
            Toast.makeText(this, "Không tìm thấy sản phẩm", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void fetchReviews(int id) {
        ApiService apiService = RetrofitClient.getClient(this).create(ApiService.class);
        apiService.getProductReviews(id).enqueue(new Callback<List<Review>>() {
            @Override
            public void onResponse(Call<List<Review>> call, Response<List<Review>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Review> allReviews = response.body();

                    // Cập nhật text "Xem tất cả (n)"
                    tvSeeAllReviews.setText("Xem tất cả (" + allReviews.size() + ") >");

                    // Lấy tối đa 3 comment đầu tiên
                    List<Review> previewList = new ArrayList<>();
                    for (int i = 0; i < Math.min(3, allReviews.size()); i++) {
                        previewList.add(allReviews.get(i));
                    }

                    feedbackAdapter.updateData(previewList);
                }
            }

            @Override
            public void onFailure(Call<List<Review>> call, Throwable t) {
                Log.e(TAG, "Lỗi tải review: " + t.getMessage());
            }
        });
    }

    private void checkFavoriteStatus() {
        if (DataStore.cachedFavorites != null) {
            for (Product p : DataStore.cachedFavorites) {
                if (p.getId() == currentProduct.getId()) {
                    isFavorite = true;
                    break;
                }
            }
        }
        updateFavoriteIcon();
    }

    private void updateFavoriteIcon() {
        if (isFavorite) {
            btnFavorite.setImageResource(R.drawable.ic_heart_filled);
        } else {
            btnFavorite.setImageResource(R.drawable.ic_heart_outline);
        }
    }

    private void setupEvents() {
        btnBack.setOnClickListener(v -> finish());

        // Xem tất cả đánh giá
        tvSeeAllReviews.setOnClickListener(v -> {
            Intent intent = new Intent(ProductDetailActivity.this, AllReviewsActivity.class);
            intent.putExtra("product_id", productId);
            startActivity(intent);
        });

        // Tăng giảm số lượng
        btnDecrease.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                tvQuantity.setText(String.valueOf(quantity));
            }
        });

        btnIncrease.setOnClickListener(v -> {
            if (currentProduct != null && quantity < currentProduct.getStock()) {
                quantity++;
                tvQuantity.setText(String.valueOf(quantity));
            } else {
                Toast.makeText(this, "Đã đạt giới hạn kho", Toast.LENGTH_SHORT).show();
            }
        });

        // Thêm vào giỏ
        btnAddToCart.setOnClickListener(v -> {
            SharedPreferences prefs = getSharedPreferences("MY_APP_PREFS", Context.MODE_PRIVATE);
            String token = prefs.getString("ACCESS_TOKEN", null);

            if (token != null) {
                addToCart("Bearer " + token);
            } else {
                Toast.makeText(this, "Vui lòng đăng nhập!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, LoginActivity.class));
            }
        });

        // Nút Tim
        btnFavorite.setOnClickListener(v -> toggleFavorite());
    }

    private void toggleFavorite() {
        SharedPreferences prefs = getSharedPreferences("MY_APP_PREFS", Context.MODE_PRIVATE);
        String token = prefs.getString("ACCESS_TOKEN", null);

        if (token == null) {
            Toast.makeText(this, "Vui lòng đăng nhập để lưu yêu thích!", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. Optimistic UI Update
        isFavorite = !isFavorite;
        updateFavoriteIcon();
        btnFavorite.setEnabled(false);

        // 2. Update Local Cache
        if (DataStore.cachedFavorites == null) DataStore.cachedFavorites = new ArrayList<>();
        if (isFavorite) {
            boolean exists = false;
            for(Product p : DataStore.cachedFavorites) {
                if(p.getId() == currentProduct.getId()) { exists = true; break; }
            }
            if(!exists) DataStore.cachedFavorites.add(0, currentProduct);
        } else {
            try {
                DataStore.cachedFavorites.removeIf(p -> p.getId() == currentProduct.getId());
            } catch (Exception e) {
                for (int i = 0; i < DataStore.cachedFavorites.size(); i++) {
                    if (DataStore.cachedFavorites.get(i).getId() == currentProduct.getId()) {
                        DataStore.cachedFavorites.remove(i);
                        break;
                    }
                }
            }
        }

        // 3. Call API
        ApiService apiService = RetrofitClient.getClient(this).create(ApiService.class);
        Call<ResponseBody> call;

        // Giả sử dùng chung endpoint toggle
        // Nếu API bạn tách riêng add/remove thì check if(isFavorite) add else remove
        call = apiService.addToWishlist(currentProduct.getId());

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                btnFavorite.setEnabled(true);
                if (!response.isSuccessful()) {
                    revertFavoriteState();
                    Toast.makeText(ProductDetailActivity.this, "Lỗi server", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                btnFavorite.setEnabled(true);
                revertFavoriteState();
                Toast.makeText(ProductDetailActivity.this, "Lỗi mạng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void revertFavoriteState() {
        isFavorite = !isFavorite;
        updateFavoriteIcon();
        // Revert cache logic (simplified)
        if (isFavorite) DataStore.cachedFavorites.add(0, currentProduct);
        else {
            if (DataStore.cachedFavorites != null) {
                for (int i = 0; i < DataStore.cachedFavorites.size(); i++) {
                    if (DataStore.cachedFavorites.get(i).getId() == currentProduct.getId()) {
                        DataStore.cachedFavorites.remove(i);
                        break;
                    }
                }
            }
        }
    }

    private void addToCart(String authToken) {
        if (currentProduct == null) return;
        ApiService apiService = RetrofitClient.getClient(this).create(ApiService.class);
        Call<CartDTO> call = apiService.addToCart(authToken, currentProduct.getId(), quantity);

        call.enqueue(new Callback<CartDTO>() {
            @Override
            public void onResponse(Call<CartDTO> call, Response<CartDTO> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ProductDetailActivity.this, "Đã thêm vào giỏ hàng!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ProductDetailActivity.this, "Lỗi: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CartDTO> call, Throwable t) {
                Toast.makeText(ProductDetailActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }
}