package com.vibecoding.flowerstore.Activity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.vibecoding.flowerstore.Adapter.OrderDetailProductAdapter;
import com.vibecoding.flowerstore.Model.OrderDTO;
import com.vibecoding.flowerstore.R;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public class OrderDetailActivity extends AppCompatActivity {

    private TextView tvOrderId, tvStatus, tvDetailDate, tvReceiverName, tvReceiverPhone, tvReceiverAddress;
    private TextView tvPaymentMethod, tvShippingFee, tvDiscount, tvFinalTotal;
    private RecyclerView rvProducts;
    private ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_order_detail);

        initViews();

        // Lấy dữ liệu từ Intent
        OrderDTO order = (OrderDTO) getIntent().getSerializableExtra("ORDER_DATA");
        if (order != null) {
            fillData(order);
        } else {
            Toast.makeText(this, "Không tìm thấy thông tin đơn hàng", Toast.LENGTH_SHORT).show();
            finish();
        }

        btnBack.setOnClickListener(v -> finish());
    }

    private void initViews() {
        tvOrderId = findViewById(R.id.tvDetailOrderId);
        tvStatus = findViewById(R.id.tvDetailStatus);
        tvDetailDate = findViewById(R.id.tvDetailDate);
        tvReceiverName = findViewById(R.id.tvReceiverName);
        tvReceiverPhone = findViewById(R.id.tvReceiverPhone);
        tvReceiverAddress = findViewById(R.id.tvReceiverAddress);
        tvPaymentMethod = findViewById(R.id.tvPaymentMethod);
        tvShippingFee = findViewById(R.id.tvShippingFee);
        tvDiscount = findViewById(R.id.tvDiscount);
        tvFinalTotal = findViewById(R.id.tvFinalTotal);
        btnBack = findViewById(R.id.btnBackDetail);

        rvProducts = findViewById(R.id.rvDetailProducts);
        rvProducts.setLayoutManager(new LinearLayoutManager(this));
    }

    private void fillData(OrderDTO order) {
        // 1. Thông tin chung
        tvOrderId.setText("Mã đơn: #" + order.getId());

        String dateStr = order.getCreatedAt() != null ? order.getCreatedAt().replace("T", " ").split("\\.")[0] : "N/A";
        tvDetailDate.setText("Ngày đặt: " + dateStr);

        updateStatusColor(tvStatus, order.getStatus());

        // 2. Địa chỉ
        if (order.getUser() != null) {
            tvReceiverName.setText(order.getUser().getFullName());
        }
        tvReceiverPhone.setText(order.getShippingPhone());
        tvReceiverAddress.setText(order.getShippingAddress());

        // 3. Sản phẩm & Nút Đánh giá (Truyền Status vào Adapter)
        if (order.getOrderDetails() != null) {
            // QUAN TRỌNG: Truyền thêm order.getStatus() vào đây
            OrderDetailProductAdapter adapter = new OrderDetailProductAdapter(this, order.getOrderDetails(), order.getStatus());
            rvProducts.setAdapter(adapter);
        }

        // 4. Tài chính
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

        tvPaymentMethod.setText(order.getPaymentMethod());

        BigDecimal shipFee = BigDecimal.ZERO;
        if (order.getShippingCarrier() != null && order.getShippingCarrier().getShippingFee() != null) {
            shipFee = order.getShippingCarrier().getShippingFee();
        }
        tvShippingFee.setText(currencyFormat.format(shipFee));

        BigDecimal discount = order.getDiscountAmount() != null ? order.getDiscountAmount() : BigDecimal.ZERO;
        tvDiscount.setText("-" + currencyFormat.format(discount));

        tvFinalTotal.setText(currencyFormat.format(order.getTotalAmount()));
    }

    private void updateStatusColor(TextView tv, String status) {
        if (status == null) return;
        tv.setText(status);

        switch (status) {
            case "COMPLETED":
            case "DELIVERED":
            case "Giao thành công":
                tv.setBackgroundResource(R.drawable.bg_status_green);
                tv.setTextColor(Color.parseColor("#166534"));
                break;
            case "PENDING":
            case "PENDING_PAYMENT":
            case "Chờ xác nhận":
            case "Chờ lấy hàng":
            case "Đang giao":
            case "SHIPPING":
                tv.setBackgroundResource(R.drawable.bg_status_orange);
                tv.setTextColor(Color.parseColor("#9A3412"));
                break;
            case "CANCELLED":
            case "Đã huỷ":
            case "Giao thất bại":
                tv.setBackgroundResource(R.drawable.bg_status_gray);
                tv.setTextColor(Color.parseColor("#374151"));
                break;
            default:
                tv.setBackgroundResource(R.drawable.bg_status_gray);
                tv.setTextColor(Color.BLACK);
        }
    }
}