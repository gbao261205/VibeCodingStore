package com.vibecoding.flowerstore.Activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.vibecoding.flowerstore.Adapter.CheckoutAdapter;
import com.vibecoding.flowerstore.Model.AddressDTO;
import com.vibecoding.flowerstore.Model.CartDTO;
import com.vibecoding.flowerstore.Service.CheckoutDetailsResponse;
import com.vibecoding.flowerstore.Service.PlaceOrderRequest;
import com.vibecoding.flowerstore.Service.PlaceOrderResponse;
import com.vibecoding.flowerstore.R;
import com.vibecoding.flowerstore.Service.ApiService;
import com.vibecoding.flowerstore.Service.RetrofitClient;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CheckoutActivity extends AppCompatActivity {

    private RecyclerView recyclerOrderSummary;
    private CheckoutAdapter checkoutAdapter;
    private TextView tvShippingAddress, btnChangeAddress;
    private TextView tvSubtotal, tvShippingFee, tvTotalAmount;
    private Button btnPlaceOrder;
    
    // Shipping UI
    private LinearLayout layoutShippingHeader, layoutShippingOptions;
    private TextView tvSelectedShippingMethod;
    private ImageView imgShippingArrow;
    private RadioGroup radioGroupShipping;
    private RadioButton rbStandard, rbFast, rbExpress;

    // Payment UI
    private LinearLayout layoutPaymentHeader, layoutPaymentOptions;
    private TextView tvSelectedPaymentMethod;
    private ImageView imgPaymentArrow;
    private RadioGroup radioGroupPayment;
    private RadioButton rbCod, rbVnpay;

    private String authToken;
    private AddressDTO selectedAddress;
    private int selectedCarrierId = 1; // Default Standard
    private double currentShippingFee = 30000;
    private CartDTO currentCart;
    private List<AddressDTO> addressList = new ArrayList<>();
    
    private String selectedPaymentMethod = "COD";
    
    // State variables
    private boolean isShippingExpanded = false;
    private boolean isPaymentExpanded = false;
    private boolean isOrderProcessing = false; // Prevent double click

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        setupToolbar();
        initViews();

        SharedPreferences prefs = getSharedPreferences("MY_APP_PREFS", Context.MODE_PRIVATE);
        String token = prefs.getString("ACCESS_TOKEN", null);
        if (token == null) {
            finish();
            return;
        }
        authToken = "Bearer " + token;

        loadCheckoutDetails();
        setupEvents();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    private void initViews() {
        recyclerOrderSummary = findViewById(R.id.recycler_order_summary);
        tvShippingAddress = findViewById(R.id.tv_shipping_address);
        btnChangeAddress = findViewById(R.id.btn_change_address);
        
        tvSubtotal = findViewById(R.id.tv_subtotal);
        tvShippingFee = findViewById(R.id.tv_shipping_fee);
        tvTotalAmount = findViewById(R.id.tv_total_amount);
        btnPlaceOrder = findViewById(R.id.btn_place_order);
        
        // Shipping UI
        layoutShippingHeader = findViewById(R.id.layout_shipping_header);
        layoutShippingOptions = findViewById(R.id.layout_shipping_options);
        tvSelectedShippingMethod = findViewById(R.id.tv_selected_shipping_method);
        imgShippingArrow = findViewById(R.id.img_shipping_arrow);
        radioGroupShipping = findViewById(R.id.radio_group_shipping);
        rbStandard = findViewById(R.id.rb_standard);
        rbFast = findViewById(R.id.rb_fast);
        rbExpress = findViewById(R.id.rb_express);
        
        // Payment UI
        layoutPaymentHeader = findViewById(R.id.layout_payment_header);
        layoutPaymentOptions = findViewById(R.id.layout_payment_options);
        tvSelectedPaymentMethod = findViewById(R.id.tv_selected_payment_method);
        imgPaymentArrow = findViewById(R.id.img_payment_arrow);
        radioGroupPayment = findViewById(R.id.radio_group_payment);
        rbCod = findViewById(R.id.rb_cod);
        rbVnpay = findViewById(R.id.rb_vnpay);

        recyclerOrderSummary.setLayoutManager(new LinearLayoutManager(this));
    }

    private void loadCheckoutDetails() {
        ApiService apiService = RetrofitClient.getClient(this).create(ApiService.class);
        Call<CheckoutDetailsResponse> call = apiService.getCheckoutDetails(authToken);

        call.enqueue(new Callback<CheckoutDetailsResponse>() {
            @Override
            public void onResponse(Call<CheckoutDetailsResponse> call, Response<CheckoutDetailsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    updateUI(response.body());
                } else {
                    Toast.makeText(CheckoutActivity.this, "Lỗi tải thông tin thanh toán", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CheckoutDetailsResponse> call, Throwable t) {
                Toast.makeText(CheckoutActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI(CheckoutDetailsResponse data) {
        currentCart = data.getCart();

        if (currentCart != null && currentCart.getItems() != null) {
            checkoutAdapter = new CheckoutAdapter(currentCart.getItems(), this);
            recyclerOrderSummary.setAdapter(checkoutAdapter);
            tvSubtotal.setText(formatCurrency(currentCart.getTotalAmount()));
        }

        addressList = data.getAddresses();
        if (addressList != null && !addressList.isEmpty()) {
            selectedAddress = addressList.get(0);
            for (AddressDTO addr : addressList) {
                if (addr.isDefault()) {
                    selectedAddress = addr;
                    break;
                }
            }
            displayAddress(selectedAddress);
        } else {
            tvShippingAddress.setText("Vui lòng thêm địa chỉ giao hàng");
            selectedAddress = null;
        }
        
        // Mặc định chọn Standard
        rbStandard.setChecked(true);
        updateShippingSelection(1, 30000, "Tiêu chuẩn - 30.000đ");

        // Mặc định thanh toán COD
        rbCod.setChecked(true);
        updatePaymentSelection("COD", "Thanh toán khi nhận hàng (COD)");
        
        calculateTotal();
    }

    private void displayAddress(AddressDTO address) {
        if (address == null) return;
        String fullAddress = address.getRecipientName() + " | " + address.getPhoneNumber() + "\n" +
                             address.getDetailAddress() + ", " + address.getCity();
        tvShippingAddress.setText(fullAddress);
    }

    private void calculateTotal() {
        if (currentCart == null) return;

        double subtotal = currentCart.getTotalAmount();
        double total = subtotal + currentShippingFee;

        tvShippingFee.setText(formatCurrency(currentShippingFee));
        tvTotalAmount.setText(formatCurrency(total));
    }
    
    private void updateShippingSelection(int id, double fee, String displayText) {
        selectedCarrierId = id;
        currentShippingFee = fee;
        tvSelectedShippingMethod.setText(displayText);
        calculateTotal();
    }
    
    private void updatePaymentSelection(String methodCode, String displayText) {
        selectedPaymentMethod = methodCode;
        tvSelectedPaymentMethod.setText(displayText);
    }

    private void setupEvents() {
        btnChangeAddress.setOnClickListener(v -> showAddressSelectionDialog());
        
        // --- Toggle Shipping Options ---
        layoutShippingHeader.setOnClickListener(v -> {
            isShippingExpanded = !isShippingExpanded;
            layoutShippingOptions.setVisibility(isShippingExpanded ? View.VISIBLE : View.GONE);
            imgShippingArrow.setRotation(isShippingExpanded ? 270 : 90);
            
            // Nếu mở cái này thì đóng cái kia cho gọn
            if (isShippingExpanded && isPaymentExpanded) {
                 isPaymentExpanded = false;
                 layoutPaymentOptions.setVisibility(View.GONE);
                 imgPaymentArrow.setRotation(90);
            }
        });

        radioGroupShipping.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_standard) {
                updateShippingSelection(1, 30000, "Tiêu chuẩn - 30.000đ");
            } else if (checkedId == R.id.rb_fast) {
                updateShippingSelection(2, 50000, "Nhanh - 50.000đ");
            } else if (checkedId == R.id.rb_express) {
                updateShippingSelection(3, 100000, "Hỏa tốc - 100.000đ");
            }
            // Tự động đóng sau khi chọn (trải nghiệm mượt hơn)
            // layoutShippingHeader.performClick(); 
        });

        // --- Toggle Payment Options ---
        layoutPaymentHeader.setOnClickListener(v -> {
            isPaymentExpanded = !isPaymentExpanded;
            layoutPaymentOptions.setVisibility(isPaymentExpanded ? View.VISIBLE : View.GONE);
            imgPaymentArrow.setRotation(isPaymentExpanded ? 270 : 90);
            
            // Nếu mở cái này thì đóng cái kia cho gọn
            if (isPaymentExpanded && isShippingExpanded) {
                 isShippingExpanded = false;
                 layoutShippingOptions.setVisibility(View.GONE);
                 imgShippingArrow.setRotation(90);
            }
        });

        radioGroupPayment.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_cod) {
                updatePaymentSelection("COD", "Thanh toán khi nhận hàng (COD)");
            } else if (checkedId == R.id.rb_vnpay) {
                updatePaymentSelection("PAY", "Thanh toán Online (PAY)");
            }
        });

        btnPlaceOrder.setOnClickListener(v -> placeOrder());
    }

    private void showAddressSelectionDialog() {
        if (addressList == null || addressList.isEmpty()) {
            Toast.makeText(this, "Không có địa chỉ nào để chọn", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> addressStrings = new ArrayList<>();
        for (AddressDTO address : addressList) {
            addressStrings.add(address.getRecipientName() + " - " + address.getDetailAddress());
        }

        new AlertDialog.Builder(this)
                .setTitle("Chọn địa chỉ giao hàng")
                .setItems(addressStrings.toArray(new String[0]), (dialog, which) -> {
                    selectedAddress = addressList.get(which);
                    displayAddress(selectedAddress);
                })
                .show();
    }

    private void placeOrder() {
        // Prevent double click
        if (isOrderProcessing) return;

        if (selectedAddress == null) {
            Toast.makeText(this, "Vui lòng chọn địa chỉ giao hàng", Toast.LENGTH_SHORT).show();
            return;
        }

        isOrderProcessing = true;
        btnPlaceOrder.setEnabled(false); // Disable button immediately
        btnPlaceOrder.setText("Đang xử lý...");

        // Tính toán totalAmount cuối cùng trước khi gửi (bao gồm shipping fee)
        double finalTotalAmount = 0;
        if (currentCart != null) {
             finalTotalAmount = currentCart.getTotalAmount() + currentShippingFee;
        }

        PlaceOrderRequest request = new PlaceOrderRequest(
                selectedAddress.getId(),
                selectedCarrierId,
                selectedPaymentMethod,
                finalTotalAmount // Gửi tổng tiền ĐÃ CỘNG PHÍ SHIP
        );
        
        if (selectedCarrierId == 1) {
             request.setNotes("Giao tiêu chuẩn (trong 8h trước 20h)");
        } else if (selectedCarrierId == 2) {
             request.setNotes("Giao nhanh (trong 4h)");
        } else if (selectedCarrierId == 3) {
             request.setNotes("Giao hỏa tốc (trong 1h - nội thành SG)");
        }

        // Lưu biến tạm để truyền sang Activity sau
        final double totalAmountToPass = finalTotalAmount;

        ApiService apiService = RetrofitClient.getClient(this).create(ApiService.class);
        Call<PlaceOrderResponse> call = apiService.placeOrder(authToken, request);

        call.enqueue(new Callback<PlaceOrderResponse>() {
            @Override
            public void onResponse(Call<PlaceOrderResponse> call, Response<PlaceOrderResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(CheckoutActivity.this, "Đặt hàng thành công!", Toast.LENGTH_LONG).show();

                    // Chuyển hướng sang màn hình Đặt hàng thành công
                    Intent intent = new Intent(CheckoutActivity.this, OrderSuccessActivity.class);
                    // Dùng ID trả về từ server
                    intent.putExtra("ORDER_ID", response.body().getOrderId());
                    // Dùng số tiền từ response nếu có, hoặc dùng số tiền đã tính toán (ưu tiên response để chính xác)
                    double confirmedTotal = response.body().getTotalAmount() > 0 ? response.body().getTotalAmount() : totalAmountToPass;
                    intent.putExtra("TOTAL_AMOUNT", confirmedTotal);
                    
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(CheckoutActivity.this, "Đặt hàng thất bại: " + response.code(), Toast.LENGTH_SHORT).show();
                    resetOrderButtonState();
                }
            }

            @Override
            public void onFailure(Call<PlaceOrderResponse> call, Throwable t) {
                Toast.makeText(CheckoutActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                resetOrderButtonState();
            }
        });
    }
    
    private void resetOrderButtonState() {
        isOrderProcessing = false;
        btnPlaceOrder.setEnabled(true);
        btnPlaceOrder.setText("Đặt hàng");
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private String formatCurrency(double amount) {
        return NumberFormat.getCurrencyInstance(new Locale("vi", "VN")).format(amount);
    }
}
