package com.vibecoding.flowerstore.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.vibecoding.flowerstore.R;

import java.text.NumberFormat;
import java.util.Locale;

public class OrderSuccessActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_success);

        TextView tvOrderId = findViewById(R.id.tv_order_id);
        TextView tvTotalAmount = findViewById(R.id.tv_total_amount);
        Button btnContinueShopping = findViewById(R.id.btn_continue_shopping);
        Button btnViewHistory = findViewById(R.id.btn_view_history);

        // Lấy dữ liệu từ Intent
        int orderId = getIntent().getIntExtra("ORDER_ID", 0);
        double totalAmount = getIntent().getDoubleExtra("TOTAL_AMOUNT", 0);

        if (orderId > 0) {
            tvOrderId.setText("#" + orderId);
        } else {
            // Trường hợp lỗi không lấy được ID, hiện "Đang cập nhật" hoặc "--"
            tvOrderId.setText("#--");
        }
        
        tvTotalAmount.setText(NumberFormat.getCurrencyInstance(new Locale("vi", "VN")).format(totalAmount));

        btnContinueShopping.setOnClickListener(v -> {
            Intent intent = new Intent(OrderSuccessActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        btnViewHistory.setOnClickListener(v -> {
            Intent intent = new Intent(OrderSuccessActivity.this, OrderHistoryActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }
    
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(OrderSuccessActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
