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
import com.vibecoding.flowerstore.BuildConfig; // Đã import BuildConfig
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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.momo.momo_partner.AppMoMoLib;

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
    
    // MoMo Info (Lấy từ BuildConfig để bảo mật)
    private String partnerCode = BuildConfig.MOMO_PARTNER_CODE; 
    private String accessKey = BuildConfig.MOMO_ACCESS_KEY;
    private String secretKey = BuildConfig.MOMO_SECRET_KEY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);
        
        // Kiểm tra xem Key có được load lên không (Chỉ dùng để debug, sau này xóa đi)
        if (partnerCode == null || partnerCode.isEmpty()) {
            Toast.makeText(this, "Cảnh báo: Chưa cấu hình MoMo Partner Code trong local.properties", Toast.LENGTH_LONG).show();
        }

        // Khởi tạo môi trường Test MoMo (DEVELOPMENT)
        AppMoMoLib.getInstance().setEnvironment(AppMoMoLib.ENVIRONMENT.DEVELOPMENT);

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
        });

        // --- Toggle Payment Options ---
        layoutPaymentHeader.setOnClickListener(v -> {
            isPaymentExpanded = !isPaymentExpanded;
            layoutPaymentOptions.setVisibility(isPaymentExpanded ? View.VISIBLE : View.GONE);
            imgPaymentArrow.setRotation(isPaymentExpanded ? 270 : 90);
            
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
                updatePaymentSelection("PAY", "Thanh toán Online (MoMo)");
            }
        });

        btnPlaceOrder.setOnClickListener(v -> {
            if (isOrderProcessing) return;

            if (selectedAddress == null) {
                Toast.makeText(this, "Vui lòng chọn địa chỉ giao hàng", Toast.LENGTH_SHORT).show();
                return;
            }

            // Nếu chọn MoMo thì request Payment trước
            if ("PAY".equals(selectedPaymentMethod)) {
                requestMoMoPayment();
            } else {
                // COD thì gọi API luôn
                callPlaceOrderApi();
            }
        });
    }

    // --- MOMO LOGIC ---
    private void requestMoMoPayment() {
        double finalTotalAmount = 0;
        if (currentCart != null) {
            finalTotalAmount = currentCart.getTotalAmount() + currentShippingFee;
        }
        
        long amount = (long) finalTotalAmount;
        String orderId = "DH" + System.currentTimeMillis();
        String orderInfo = "Thanh toan don hang #" + orderId;

        AppMoMoLib.getInstance().setAction(AppMoMoLib.ACTION.PAYMENT);
        AppMoMoLib.getInstance().setActionType(AppMoMoLib.ACTION_TYPE.GET_TOKEN);

        Map<String, Object> eventValue = new HashMap<>();
        eventValue.put("merchantname", "Flower Store"); // Tên hiển thị trên MoMo
        eventValue.put("merchantcode", partnerCode);
        eventValue.put("amount", amount);
        eventValue.put("orderId", orderId);
        eventValue.put("orderLabel", "Mã đơn hàng"); 

        eventValue.put("merchantnamelabel", "Dịch vụ");
        eventValue.put("fee", 0);
        eventValue.put("description", orderInfo);
        
        eventValue.put("requestId", orderId);
        eventValue.put("partnerCode", partnerCode);
        
        // Thêm extraData nếu cần
        eventValue.put("extraData", "");
        eventValue.put("extra", "");

        try {
            AppMoMoLib.getInstance().requestMoMoCallBack(this, eventValue);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi khởi tạo MoMo: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == AppMoMoLib.getInstance().REQUEST_CODE_MOMO && resultCode == -1) {
            if(data != null) {
                if(data.getIntExtra("status", -1) == 0) {
                    // TOKEN IS AVAILABLE -> Thanh toán thành công ở client
                    Toast.makeText(this, "Thanh toán MoMo thành công!", Toast.LENGTH_SHORT).show();
                    // String token = data.getStringExtra("data"); 
                    // Gọi API đặt hàng sau khi thanh toán thành công
                    callPlaceOrderApi();
                } else {
                    String message = data.getStringExtra("message");
                    Toast.makeText(this, "Thanh toán thất bại: " + (message != null ? message : "Đã hủy"), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Thanh toán thất bại: Không nhận được phản hồi", Toast.LENGTH_SHORT).show();
            }
        }
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

    private void callPlaceOrderApi() {
        isOrderProcessing = true;
        btnPlaceOrder.setEnabled(false);
        btnPlaceOrder.setText("Đang xử lý...");

        // Tính toán totalAmount cuối cùng
        double finalTotalAmount = 0;
        if (currentCart != null) {
             finalTotalAmount = currentCart.getTotalAmount() + currentShippingFee;
        }

        PlaceOrderRequest request = new PlaceOrderRequest(
                selectedAddress.getId(),
                selectedCarrierId,
                selectedPaymentMethod, // "COD" hoặc "PAY"
                finalTotalAmount
        );
        
        if (selectedCarrierId == 1) {
             request.setNotes("Giao tiêu chuẩn (trong 8h trước 20h)");
        } else if (selectedCarrierId == 2) {
             request.setNotes("Giao nhanh (trong 4h)");
        } else if (selectedCarrierId == 3) {
             request.setNotes("Giao hỏa tốc (trong 1h - nội thành SG)");
        }
        
        // Nếu là MoMo, có thể append thêm ghi chú
        if ("PAY".equals(selectedPaymentMethod)) {
            String currentNote = request.getNotes() != null ? request.getNotes() : "";
            request.setNotes(currentNote + " - Đã thanh toán qua MoMo");
        }

        final double totalAmountToPass = finalTotalAmount;

        ApiService apiService = RetrofitClient.getClient(this).create(ApiService.class);
        Call<PlaceOrderResponse> call = apiService.placeOrder(authToken, request);

        call.enqueue(new Callback<PlaceOrderResponse>() {
            @Override
            public void onResponse(Call<PlaceOrderResponse> call, Response<PlaceOrderResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(CheckoutActivity.this, "Đặt hàng thành công!", Toast.LENGTH_LONG).show();

                    Intent intent = new Intent(CheckoutActivity.this, OrderSuccessActivity.class);
                    intent.putExtra("ORDER_ID", response.body().getOrderId());
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
