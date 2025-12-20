package com.vibecoding.flowerstore.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.vibecoding.flowerstore.Model.OrderDTO;
import com.vibecoding.flowerstore.Model.OrderDetailDTO;
import com.vibecoding.flowerstore.R;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private Context context;
    private List<OrderDTO> orderList;

    public OrderAdapter(Context context, List<OrderDTO> orderList) {
        this.context = context;
        this.orderList = orderList;
    }

    public void setOrderList(List<OrderDTO> orderList) {
        this.orderList = orderList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order_history, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        OrderDTO order = orderList.get(position);

        // 1. Set ID
        holder.tvOrderId.setText("Mã đơn hàng: #" + order.getId());

        // 2. Set Price (Format currency)
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        String formattedPrice = currencyFormat.format(order.getTotalAmount());
        holder.tvTotalAmount.setText("Tổng tiền: " + formattedPrice);

        // 3. Set Date
        if (order.getCreatedAt() != null) {
            String dateStr = order.getCreatedAt().toString();
            holder.tvOrderDate.setText("Ngày đặt: " + (dateStr.length() >= 10 ? dateStr.substring(0, 10) : dateStr));
        }

        // 4. Set Status & Color
        String status = order.getStatus();
        updateStatusView(holder.tvStatus, status);

        // 5. Load Image
        if (order.getOrderDetails() != null && !order.getOrderDetails().isEmpty()) {
            OrderDetailDTO firstItem = order.getOrderDetails().get(0);
            if (firstItem.getProduct() != null) {
                Glide.with(context)
                        .load(firstItem.getProduct().getImageUrl())
                        .placeholder(R.drawable.ic_launcher_background)
                        .into(holder.imgProduct);
            }
        }
    }

    private void updateStatusView(TextView tv, String status) {
        if (status == null) return;

        // Set text đúng theo status từ database
        tv.setText(status);

        switch (status) {
            case "Đơn hàng mới":
                tv.setBackgroundResource(R.drawable.bg_status_orange);
                tv.setTextColor(Color.parseColor("#9A3412"));
                break;

            case "Chờ xác nhận":
                tv.setBackgroundResource(R.drawable.bg_status_orange);
                tv.setTextColor(Color.parseColor("#9A3412"));
                break;

            case "Chờ lấy hàng":
                tv.setBackgroundResource(R.drawable.bg_status_orange);
                tv.setTextColor(Color.parseColor("#9A3412"));
                break;

            case "Đang giao":
                tv.setBackgroundResource(R.drawable.bg_status_orange);
                tv.setTextColor(Color.parseColor("#9A3412"));
                break;

            case "Giao thành công":
                tv.setBackgroundResource(R.drawable.bg_status_green);
                tv.setTextColor(Color.parseColor("#166534"));
                break;

            case "Giao thất bại":
                tv.setBackgroundResource(R.drawable.bg_status_gray);
                tv.setTextColor(Color.parseColor("#374151"));
                break;

            case "Đã hủy":
                tv.setBackgroundResource(R.drawable.bg_status_gray);
                tv.setTextColor(Color.parseColor("#374151"));
                break;

            default:
                tv.setBackgroundResource(R.drawable.bg_status_gray);
                tv.setTextColor(Color.BLACK);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return orderList == null ? 0 : orderList.size();
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvTotalAmount, tvOrderDate, tvStatus;
        ImageView imgProduct, btnDetail;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvTotalAmount = itemView.findViewById(R.id.tvTotalAmount);
            tvOrderDate = itemView.findViewById(R.id.tvOrderDate);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            btnDetail = itemView.findViewById(R.id.btnDetail);
        }
    }
}