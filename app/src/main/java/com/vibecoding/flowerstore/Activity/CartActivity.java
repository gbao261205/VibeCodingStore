package com.vibecoding.flowerstore.Activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.vibecoding.flowerstore.Adapter.CartAdapter;
import com.vibecoding.flowerstore.Model.CartDTO;
import com.vibecoding.flowerstore.Model.CartItem;
import com.vibecoding.flowerstore.R;
import com.vibecoding.flowerstore.Service.ApiService;
import com.vibecoding.flowerstore.Service.RetrofitClient;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CartActivity extends AppCompatActivity implements CartAdapter.CartListener {

    private static final String TAG = "CartActivity";
    private RecyclerView cartRecyclerView;
    private CartAdapter cartAdapter;
    private TextView totalAmountText;
    private Button checkoutButton;
    private ProgressBar loadingProgress;
    private String authToken;

    // Local data management
    private List<CartItem> localCartItems = new ArrayList<>();
    private Map<Integer, Integer> pendingUpdates = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_cart);

        setupToolbar();
        setupViews();
        setupRecyclerView();

        SharedPreferences prefs = getSharedPreferences("MY_APP_PREFS", Context.MODE_PRIVATE);
        String token = prefs.getString("ACCESS_TOKEN", null);

        if (token == null) {
            Toast.makeText(this, "Vui lòng đăng nhập để xem giỏ hàng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        authToken = "Bearer " + token;
        fetchCart();
        
        checkoutButton.setOnClickListener(v -> {
            syncCartUpdates(() -> {
                // Logic thanh toán cũ hoặc chuyển sang màn hình thanh toán
                // Hiện tại chưa có code chuyển màn hình thanh toán trong file gốc,
                // nên tôi sẽ để Toast hoặc log.
                // Nếu code gốc có logic ở checkout_button listener (chưa thấy trong file gốc tôi đọc được),
                // tôi sẽ giả định là chuyển trang hoặc thông báo.
                Toast.makeText(CartActivity.this, "Chuyển đến thanh toán", Toast.LENGTH_SHORT).show();
            });
        });
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(v -> handleBackPressed());
    }

    private void setupViews() {
        cartRecyclerView = findViewById(R.id.cart_recycler_view);
        totalAmountText = findViewById(R.id.total_amount_text);
        checkoutButton = findViewById(R.id.checkout_button);
        loadingProgress = findViewById(R.id.loading_progress);
    }

    private void setupRecyclerView() {
        cartAdapter = new CartAdapter(localCartItems, this, this);
        cartRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        cartRecyclerView.setAdapter(cartAdapter);
    }

    private void fetchCart() {
        if (authToken == null) return;

        showLoading(true);
        ApiService apiService = RetrofitClient.getClient(this).create(ApiService.class);
        Call<CartDTO> call = apiService.getCart(authToken);

        call.enqueue(new Callback<CartDTO>() {
            @Override
            public void onResponse(Call<CartDTO> call, Response<CartDTO> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    localCartItems = response.body().getItems();
                    // Reset pending updates when reloading from server
                    pendingUpdates.clear();
                    updateCartUI(localCartItems);
                } else {
                    Toast.makeText(CartActivity.this, "Lỗi khi tải giỏ hàng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CartDTO> call, Throwable t) {
                showLoading(false);
                Toast.makeText(CartActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onQuantityChanged(int productId, int newQuantity) {
        // Update local list
        for (CartItem item : localCartItems) {
            if (item.getProduct().getId() == productId) {
                item.setQuantity(newQuantity);
                // Recalculate subtotal if needed (optional for display if adapter handles it, 
                // but good for consistency)
                // Note: CartItem usually has subtotal from server, we might need to update it manually
                // to keep total calculation correct if we sum it up manually, 
                // OR we calculate total on the fly.
                break;
            }
        }
        
        // Add to pending updates
        pendingUpdates.put(productId, newQuantity);
        
        // Update UI immediately
        updateCartUI(localCartItems);
    }

    @Override
    public void onRemoveItem(int productId) {
        // For remove, we can still call API immediately as it's a destructive action
        // or we can remove locally and sync later.
        // Given the requirement "Khi người dùng nhấn nút tăng và giảm thì sẽ hiển thị thay trên màn hình trước",
        // remove is slightly different. But for consistency with "back button syncs changes", 
        // calling API immediately for removal is safer to avoid index issues.
        
        showLoading(true);
        ApiService apiService = RetrofitClient.getClient(this).create(ApiService.class);
        Call<CartDTO> call = apiService.removeFromCart(authToken, productId);

        call.enqueue(new Callback<CartDTO>() {
            @Override
            public void onResponse(Call<CartDTO> call, Response<CartDTO> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    localCartItems = response.body().getItems();
                    // If we remove an item, pending updates for it are irrelevant/handled by server
                    pendingUpdates.remove(productId); 
                    updateCartUI(localCartItems);
                } else {
                    Toast.makeText(CartActivity.this, "Lỗi xóa sản phẩm", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CartDTO> call, Throwable t) {
                showLoading(false);
                Toast.makeText(CartActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateCartUI(List<CartItem> items) {
        cartAdapter.updateData(items);
        
        // Calculate total locally
        double total = 0;
        for (CartItem item : items) {
            double price = item.getProduct().getDiscountedPrice() > 0 ? 
                           item.getProduct().getDiscountedPrice() : 
                           item.getProduct().getPrice();
            total += price * item.getQuantity();
        }

        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        totalAmountText.setText(currencyFormat.format(total));
    }

    private void syncCartUpdates(Runnable onComplete) {
        if (pendingUpdates.isEmpty()) {
            onComplete.run();
            return;
        }

        showLoading(true);
        ApiService apiService = RetrofitClient.getClient(this).create(ApiService.class);
        AtomicInteger pendingCount = new AtomicInteger(pendingUpdates.size());
        AtomicInteger errorCount = new AtomicInteger(0);

        for (Map.Entry<Integer, Integer> entry : pendingUpdates.entrySet()) {
            int productId = entry.getKey();
            int quantity = entry.getValue();

            Call<CartDTO> call = apiService.updateCartQuantity(authToken, productId, quantity);
            call.enqueue(new Callback<CartDTO>() {
                @Override
                public void onResponse(Call<CartDTO> call, Response<CartDTO> response) {
                    if (!response.isSuccessful()) {
                        errorCount.incrementAndGet();
                    }
                    checkCompletion(pendingCount, errorCount, onComplete);
                }

                @Override
                public void onFailure(Call<CartDTO> call, Throwable t) {
                    errorCount.incrementAndGet();
                    checkCompletion(pendingCount, errorCount, onComplete);
                }
            });
        }
    }

    private void checkCompletion(AtomicInteger pendingCount, AtomicInteger errorCount, Runnable onComplete) {
        if (pendingCount.decrementAndGet() == 0) {
            runOnUiThread(() -> {
                showLoading(false);
                pendingUpdates.clear(); // Clear updates as they are synced (or failed)
                if (errorCount.get() > 0) {
                    Toast.makeText(CartActivity.this, "Có lỗi khi cập nhật giỏ hàng", Toast.LENGTH_SHORT).show();
                    // Optional: fetchCart() again to sync with server state if errors occurred
                    fetchCart();
                } else {
                    onComplete.run();
                }
            });
        }
    }

    private void showLoading(boolean isLoading) {
        if (loadingProgress != null) {
            loadingProgress.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        if (checkoutButton != null) {
            checkoutButton.setEnabled(!isLoading);
        }
    }
    
    private void handleBackPressed() {
        syncCartUpdates(this::finish);
    }

    @Override
    public void onBackPressed() {
        handleBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            handleBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
