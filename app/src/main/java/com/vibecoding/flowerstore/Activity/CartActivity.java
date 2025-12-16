package com.vibecoding.flowerstore.Activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.vibecoding.flowerstore.Adapter.CartAdapter;
import com.vibecoding.flowerstore.Model.CartDTO;
import com.vibecoding.flowerstore.R;
import com.vibecoding.flowerstore.Service.ApiService;
import com.vibecoding.flowerstore.Service.RetrofitClient;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CartActivity extends AppCompatActivity implements CartAdapter.CartListener {

    private static final String TAG = "CartActivity";
    private RecyclerView cartRecyclerView;
    private CartAdapter cartAdapter;
    private TextView totalAmountText;
    private Button checkoutButton;
    private String authToken;

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
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupViews() {
        cartRecyclerView = findViewById(R.id.cart_recycler_view);
        totalAmountText = findViewById(R.id.total_amount_text);
        checkoutButton = findViewById(R.id.checkout_button);
    }

    private void setupRecyclerView() {
        cartAdapter = new CartAdapter(new ArrayList<>(), this, this);
        cartRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        cartRecyclerView.setAdapter(cartAdapter);
    }

    private void fetchCart() {
        if (authToken == null) return;

        ApiService apiService = RetrofitClient.getClient(this).create(ApiService.class);
        Call<CartDTO> call = apiService.getCart(authToken);

        call.enqueue(new Callback<CartDTO>() {
            @Override
            public void onResponse(Call<CartDTO> call, Response<CartDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    updateCartUI(response.body());
                } else {
                    Toast.makeText(CartActivity.this, "Lỗi khi tải giỏ hàng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CartDTO> call, Throwable t) {
                Toast.makeText(CartActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onQuantityChanged(int productId, int newQuantity) {
        ApiService apiService = RetrofitClient.getClient(this).create(ApiService.class);
        Call<CartDTO> call = apiService.updateCartQuantity(authToken, productId, newQuantity);

        call.enqueue(new Callback<CartDTO>() {
            @Override
            public void onResponse(Call<CartDTO> call, Response<CartDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    updateCartUI(response.body());
                } else {
                    Toast.makeText(CartActivity.this, "Lỗi cập nhật số lượng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CartDTO> call, Throwable t) {
                Toast.makeText(CartActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onRemoveItem(int productId) {
        ApiService apiService = RetrofitClient.getClient(this).create(ApiService.class);
        Call<CartDTO> call = apiService.removeFromCart(authToken, productId);

        call.enqueue(new Callback<CartDTO>() {
            @Override
            public void onResponse(Call<CartDTO> call, Response<CartDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    updateCartUI(response.body());
                } else {
                    Toast.makeText(CartActivity.this, "Lỗi xóa sản phẩm", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CartDTO> call, Throwable t) {
                Toast.makeText(CartActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateCartUI(CartDTO cart) {
        cartAdapter.updateData(cart.getItems());
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        totalAmountText.setText(currencyFormat.format(cart.getTotalAmount()));
    }
}
