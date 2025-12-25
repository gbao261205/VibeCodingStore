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
import android.widget.ImageButton; // Import ImageButton
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.vibecoding.flowerstore.Model.CartDTO;
import com.vibecoding.flowerstore.Model.DataStore; // Import DataStore
import com.vibecoding.flowerstore.Model.Product;
import com.vibecoding.flowerstore.R;
import com.vibecoding.flowerstore.Service.ApiService;
import com.vibecoding.flowerstore.Service.RetrofitClient;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductDetailActivity extends AppCompatActivity {

    private ImageView imgProduct, btnBack, btnDecrease, btnIncrease;
    private ImageButton btnFavorite; // Khai báo nút Tim
    private TextView tvName, tvPrice, tvSupplier, tvDescription, tvQuantity, tvStockStatus;
    private Button btnAddToCart;

    private Product currentProduct;
    private int quantity = 1;
    private boolean isFavorite = false; // Biến theo dõi trạng thái yêu thích

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_product_detail);

        initViews();
        loadProductData();
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

        // Nút tim mới thêm
        btnFavorite = findViewById(R.id.btn_favorite_detail);

        // Phần số lượng
        btnDecrease = findViewById(R.id.btn_decrease);
        btnIncrease = findViewById(R.id.btn_increase);
        tvQuantity = findViewById(R.id.tv_quantity);
        btnAddToCart = findViewById(R.id.btn_add_to_cart);
    }

    private void loadProductData() {
        int productId = getIntent().getIntExtra("product_id", -1);

        // 1. TÌM SẢN PHẨM TRONG CACHE (HOME HOẶC FAVORITE)
        // Vì có thể user bấm từ trang Home HOẶC từ trang Favorite sang
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
            // B. Nếu chưa thấy, tìm trong cache yêu thích (Favorite)
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
            // --- CẬP NHẬT UI CƠ BẢN ---
            tvName.setText(currentProduct.getName());
            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            tvPrice.setText(formatter.format(currentProduct.getPrice()));

            if (currentProduct.getImage() != null && !currentProduct.getImage().isEmpty()) {
                Glide.with(this).load(currentProduct.getImage()).placeholder(R.drawable.placeholder_product).into(imgProduct);
            } else {
                imgProduct.setImageResource(R.drawable.banner1);
            }

            // Shop Info
            if (currentProduct.getShop() != null) {
                tvSupplier.setText("Cung cấp bởi: " + currentProduct.getShop().getName());
            } else {
                tvSupplier.setText("Cung cấp bởi: StarShop");
            }

            // Stock
            int stock = currentProduct.getStock();
            if (stock > 0) {
                tvStockStatus.setText("Tình trạng: Còn hàng (" + stock + ")");
                tvStockStatus.setTextColor(android.graphics.Color.parseColor("#4CAF50"));
                btnAddToCart.setEnabled(true);
                btnAddToCart.setAlpha(1.0f);
            } else {
                tvStockStatus.setText("Tình trạng: Hết hàng");
                tvStockStatus.setTextColor(android.graphics.Color.RED);
                btnAddToCart.setEnabled(false);
                btnAddToCart.setAlpha(0.5f);
            }

            // Description
            String categoryName = (currentProduct.getCategory() != null) ? currentProduct.getCategory().getName() : "Hoa tươi";
            String shopName = (currentProduct.getShop() != null) ? currentProduct.getShop().getName() : "Cửa hàng";
            String fakeDescription = "Sản phẩm " + currentProduct.getName() + " thuộc danh mục " + categoryName + ".\n" +
                    "Phân phối bởi " + shopName + ". Cam kết chất lượng.";
            tvDescription.setText(fakeDescription);

            // --- CHECK TRẠNG THÁI YÊU THÍCH (MỚI) ---
            checkFavoriteStatus();

        } else {
            Toast.makeText(this, "Không tìm thấy thông tin sản phẩm", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    // Hàm kiểm tra xem sản phẩm này có đang được tim không
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

        // Xử lý số lượng
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

        // Xử lý thêm vào giỏ hàng
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

        // --- XỬ LÝ NÚT TIM (TOGGLE: THÊM/XÓA) ---
        btnFavorite.setOnClickListener(v -> toggleFavorite());
    }

    // Logic Thêm/Xóa yêu thích (Optimistic UI)
    private void toggleFavorite() {
        // 1. Check Login
        SharedPreferences prefs = getSharedPreferences("MY_APP_PREFS", Context.MODE_PRIVATE);
        String token = prefs.getString("ACCESS_TOKEN", null);

        if (token == null) {
            Toast.makeText(this, "Vui lòng đăng nhập để lưu yêu thích!", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. Đảo ngược trạng thái ngay lập tức (UI Feedback)
        isFavorite = !isFavorite;
        updateFavoriteIcon();
        btnFavorite.setEnabled(false); // Khóa nút tạm thời

        // 3. Cập nhật Cache cục bộ ngay lập tức
        if (DataStore.cachedFavorites == null) DataStore.cachedFavorites = new ArrayList<>();

        if (isFavorite) {
            // Nếu chuyển thành Thích -> Thêm vào cache
            boolean exists = false;
            for(Product p : DataStore.cachedFavorites) {
                if(p.getId() == currentProduct.getId()) { exists = true; break;}
            }
            if(!exists) DataStore.cachedFavorites.add(0, currentProduct);
        } else {
            // Nếu bỏ Thích -> Xóa khỏi cache
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

        // 4. Gọi API ngầm (Add hoặc Remove)
        ApiService apiService = RetrofitClient.getClient(this).create(ApiService.class);
        Call<ResponseBody> call;

        if (isFavorite) {
            call = apiService.addToWishlist(currentProduct.getId());
        } else {
            call = apiService.removeFromWishlist(currentProduct.getId());
        }

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                btnFavorite.setEnabled(true);
                if (!response.isSuccessful()) {
                    // Lỗi -> Hoàn tác UI
                    revertFavoriteState();
                    Toast.makeText(ProductDetailActivity.this, "Lỗi server: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                btnFavorite.setEnabled(true);
                // Lỗi mạng -> Hoàn tác UI
                revertFavoriteState();
                Toast.makeText(ProductDetailActivity.this, "Lỗi mạng!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void revertFavoriteState() {
        // Đảo ngược lại trạng thái nếu API lỗi
        isFavorite = !isFavorite;
        updateFavoriteIcon();

        // Hoàn tác Cache (Logic ngược lại với toggleFavorite)
        if (isFavorite) {
            DataStore.cachedFavorites.add(0, currentProduct);
        } else {
            if (DataStore.cachedFavorites != null) {
                try {
                    DataStore.cachedFavorites.removeIf(p -> p.getId() == currentProduct.getId());
                } catch (Exception e) { /* Handle old Java logic */ }
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
                Toast.makeText(ProductDetailActivity.this, "Lỗi kết nối mạng", Toast.LENGTH_SHORT).show();
            }
        });
    }
}