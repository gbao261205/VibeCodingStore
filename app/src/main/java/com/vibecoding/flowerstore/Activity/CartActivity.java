package com.vibecoding.flowerstore.Activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
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
    private ImageButton btnDeleteCart;
    private ProgressBar loadingProgress;
    private String authToken;
    private boolean isDeleteMode = false;

    private List<CartItem> localCartItems = new ArrayList<>();
    
    // Lưu các thay đổi để sync sau
    private Map<Integer, Integer> pendingUpdates = new HashMap<>(); // productId -> quantity
    private Set<Integer> pendingRemovals = new HashSet<>(); // productId

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
            // Sync dữ liệu trước khi sang trang checkout
            syncCartData(() -> {
                Intent intent = new Intent(CartActivity.this, CheckoutActivity.class);
                startActivity(intent);
            });
        });

        btnDeleteCart.setOnClickListener(v -> {
            if (!isDeleteMode) {
                // Enter delete mode
                isDeleteMode = true;
                cartAdapter.setDeleteMode(true);
                Toast.makeText(this, "Chọn sản phẩm để xóa", Toast.LENGTH_SHORT).show();
            } else {
                // Already in delete mode
                Set<Integer> selectedItems = cartAdapter.getSelectedItems();
                if (selectedItems.isEmpty()) {
                    // Exit delete mode if nothing selected
                    isDeleteMode = false;
                    cartAdapter.setDeleteMode(false);
                } else {
                    // Perform delete local
                    deleteSelectedItemsLocal(selectedItems);
                }
            }
        });
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");
    }

    private void setupViews() {
        cartRecyclerView = findViewById(R.id.cart_recycler_view);
        totalAmountText = findViewById(R.id.total_amount_text);
        checkoutButton = findViewById(R.id.checkout_button);
        loadingProgress = findViewById(R.id.loading_progress);
        btnDeleteCart = findViewById(R.id.btn_delete_cart);
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
                    // Reset pending changes khi load mới từ server
                    pendingUpdates.clear();
                    pendingRemovals.clear();
                    updateCartUI(response.body());
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
    
    // Xử lý xóa nhiều item (Local)
    private void deleteSelectedItemsLocal(Set<Integer> selectedItems) {
        // Copy set để tránh ConcurrentModificationException
        Set<Integer> itemsToDelete = new HashSet<>(selectedItems);
        
        for (Integer productId : itemsToDelete) {
            onRemoveItem(productId);
        }
        
        // Thoát chế độ xóa
        isDeleteMode = false;
        cartAdapter.setDeleteMode(false);
        Toast.makeText(this, "Đã xóa " + itemsToDelete.size() + " sản phẩm", Toast.LENGTH_SHORT).show();
    }

    // Callback từ Adapter: Chỉ lưu thay đổi, KHÔNG gọi API ngay
    @Override
    public void onQuantityChanged(int productId, int newQuantity) {
        // Nếu sản phẩm đang nằm trong danh sách chờ xóa, bỏ nó ra khỏi danh sách xóa (trường hợp hiếm)
        pendingRemovals.remove(productId);
        
        // Lưu vào map update
        pendingUpdates.put(productId, newQuantity);
        
        // Cập nhật tổng tiền local
        calculateLocalTotal();
    }
    
    // Callback từ Adapter: Chỉ xóa local, KHÔNG gọi API ngay
    @Override
    public void onRemoveItem(int productId) {
        // Thêm vào danh sách chờ xóa server
        pendingRemovals.add(productId);
        // Nếu có update chờ cho item này thì bỏ đi vì đằng nào cũng xóa
        pendingUpdates.remove(productId);

        // Xóa khỏi danh sách hiển thị local
        Iterator<CartItem> iterator = localCartItems.iterator();
        while (iterator.hasNext()) {
            CartItem item = iterator.next();
            if (item.getProduct().getId() == productId) {
                iterator.remove();
                break;
            }
        }
        
        // Cập nhật UI
        cartAdapter.notifyDataSetChanged();
        calculateLocalTotal();
    }
    
    @Override
    public void onTotalPriceUpdated() {
        calculateLocalTotal();
    }

    @Override
    public void onSelectionChanged(int selectedCount) {
        // Optional UI update
    }

    private void updateCartUI(CartDTO cart) {
        if (cart != null) {
            localCartItems.clear();
            localCartItems.addAll(cart.getItems());
            cartAdapter.notifyDataSetChanged();
            calculateLocalTotal();
        }
    }
    
    private void calculateLocalTotal() {
        double total = 0;
        for (CartItem item : localCartItems) {
            double price = item.getProduct().getDiscountedPrice() > 0 
                    ? item.getProduct().getDiscountedPrice() 
                    : item.getProduct().getPrice();
            total += price * item.getQuantity();
        }
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        totalAmountText.setText(currencyFormat.format(total));
    }
    
    // Hàm đồng bộ dữ liệu lên Server
    private void syncCartData(Runnable onComplete) {
        if (pendingUpdates.isEmpty() && pendingRemovals.isEmpty()) {
            onComplete.run();
            return;
        }
        
        showLoading(true);
        ApiService apiService = RetrofitClient.getClient(this).create(ApiService.class);
        
        // Tổng số request cần thực hiện
        int totalRequests = pendingUpdates.size() + pendingRemovals.size();
        AtomicInteger completedRequests = new AtomicInteger(0);
        
        // 1. Thực hiện các request xóa
        for (Integer productId : pendingRemovals) {
            Call<CartDTO> call = apiService.removeFromCart(authToken, productId);
            call.enqueue(new Callback<CartDTO>() {
                @Override
                public void onResponse(Call<CartDTO> call, Response<CartDTO> response) {
                    checkSyncComplete(totalRequests, completedRequests, onComplete);
                }
                @Override
                public void onFailure(Call<CartDTO> call, Throwable t) {
                    // Vẫn tiếp tục đếm để không bị treo loading
                    checkSyncComplete(totalRequests, completedRequests, onComplete);
                }
            });
        }
        
        // 2. Thực hiện các request update số lượng
        for (Map.Entry<Integer, Integer> entry : pendingUpdates.entrySet()) {
            Call<CartDTO> call = apiService.updateCartQuantity(authToken, entry.getKey(), entry.getValue());
            call.enqueue(new Callback<CartDTO>() {
                @Override
                public void onResponse(Call<CartDTO> call, Response<CartDTO> response) {
                    checkSyncComplete(totalRequests, completedRequests, onComplete);
                }
                @Override
                public void onFailure(Call<CartDTO> call, Throwable t) {
                    checkSyncComplete(totalRequests, completedRequests, onComplete);
                }
            });
        }
    }
    
    private void checkSyncComplete(int total, AtomicInteger current, Runnable onComplete) {
        if (current.incrementAndGet() >= total) {
            // Xóa pending sau khi sync xong
            pendingUpdates.clear();
            pendingRemovals.clear();
            
            // Chạy callback trên UI thread
            runOnUiThread(() -> {
                showLoading(false);
                onComplete.run();
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
        if (btnDeleteCart != null) {
            btnDeleteCart.setEnabled(!isLoading);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (isDeleteMode) {
                isDeleteMode = false;
                cartAdapter.setDeleteMode(false);
                return true;
            }
            // Sync dữ liệu khi bấm nút Back
            syncCartData(this::finish);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    public void onBackPressed() {
        if (isDeleteMode) {
            isDeleteMode = false;
            cartAdapter.setDeleteMode(false);
        } else {
            // Sync dữ liệu khi bấm nút Back hệ thống
            syncCartData(super::onBackPressed);
        }
    }
}
