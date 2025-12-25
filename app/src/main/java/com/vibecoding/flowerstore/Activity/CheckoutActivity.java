package com.vibecoding.flowerstore.Activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
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
import java.util.Arrays;
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
    private Spinner spinnerShipping, spinnerPaymentMethod;

    private String authToken;
    private AddressDTO selectedAddress;
    private CheckoutDetailsResponse.ShippingCarrier selectedCarrier;
    private CartDTO currentCart;
    private List<AddressDTO> addressList = new ArrayList<>();
    private List<CheckoutDetailsResponse.ShippingCarrier> carrierList = new ArrayList<>();
    private String selectedPaymentMethod = "COD";

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
        spinnerShipping = findViewById(R.id.spinner_shipping);
        spinnerPaymentMethod = findViewById(R.id.spinner_payment_method);
        tvSubtotal = findViewById(R.id.tv_subtotal);
        tvShippingFee = findViewById(R.id.tv_shipping_fee);
        tvTotalAmount = findViewById(R.id.tv_total_amount);
        btnPlaceOrder = findViewById(R.id.btn_place_order);

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

        carrierList = data.getShippingCarriers();
        if (carrierList != null && !carrierList.isEmpty()) {
            setupShippingSpinner(carrierList);
        }

        setupPaymentSpinner();
        calculateTotal();
    }

    private void setupShippingSpinner(List<CheckoutDetailsResponse.ShippingCarrier> carriers) {
        List<String> carrierNames = new ArrayList<>();
        for (CheckoutDetailsResponse.ShippingCarrier carrier : carriers) {
            carrierNames.add(carrier.getName() + " - " + formatCurrency(carrier.getShippingFee()));
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, carrierNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerShipping.setAdapter(adapter);
    }

    private void setupPaymentSpinner() {
        List<String> paymentMethods = Arrays.asList("Thanh toán khi nhận hàng (COD)", "Thanh toán Online (PAY)");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, paymentMethods);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPaymentMethod.setAdapter(adapter);
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
        double shippingFee = (selectedCarrier != null) ? selectedCarrier.getShippingFee() : 0;
        double total = subtotal + shippingFee;

        tvShippingFee.setText(formatCurrency(shippingFee));
        tvTotalAmount.setText(formatCurrency(total));
    }

    private void setupEvents() {
        btnChangeAddress.setOnClickListener(v -> showAddressSelectionDialog());

        spinnerShipping.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCarrier = carrierList.get(position);
                calculateTotal();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        spinnerPaymentMethod.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedPaymentMethod = (position == 0) ? "COD" : "PAY";
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
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
        if (selectedAddress == null) {
            Toast.makeText(this, "Vui lòng chọn địa chỉ giao hàng", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedCarrier == null) {
            Toast.makeText(this, "Vui lòng chọn đơn vị vận chuyển", Toast.LENGTH_SHORT).show();
            return;
        }

        PlaceOrderRequest request = new PlaceOrderRequest(
                selectedAddress.getId(),
                selectedCarrier.getId(),
                selectedPaymentMethod,
                currentCart.getTotalAmount()
        );

        ApiService apiService = RetrofitClient.getClient(this).create(ApiService.class);
        Call<PlaceOrderResponse> call = apiService.placeOrder(authToken, request);

        call.enqueue(new Callback<PlaceOrderResponse>() {
            @Override
            public void onResponse(Call<PlaceOrderResponse> call, Response<PlaceOrderResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(CheckoutActivity.this, "Đặt hàng thành công!", Toast.LENGTH_LONG).show();

                    Intent intent = new Intent(CheckoutActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(CheckoutActivity.this, "Đặt hàng thất bại: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<PlaceOrderResponse> call, Throwable t) {
                Toast.makeText(CheckoutActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
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
