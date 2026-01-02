package com.vibecoding.flowerstore.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.vibecoding.flowerstore.Model.OrderDTO;
import com.vibecoding.flowerstore.R;

import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;

public class AdminOrderAdapter extends RecyclerView.Adapter<AdminOrderAdapter.ViewHolder> {

    private Context context;
    private List<OrderDTO> orderList;
    private OnOrderClickListener listener;

    // Interface để xử lý sự kiện click
    public interface OnOrderClickListener {
        void onOrderClick(OrderDTO order);
    }

    // Cập nhật Constructor để nhận listener
    public AdminOrderAdapter(Context context, List<OrderDTO> orderList, OnOrderClickListener listener) {
        this.context = context;
        this.orderList = orderList;
        this.listener = listener;
    }

    public void setOrderList(List<OrderDTO> orderList) {
        this.orderList = orderList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order_admin, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OrderDTO order = orderList.get(position);
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

        // Set ID
        holder.tvOrderId.setText("#DH" + order.getId());

        // Set Time
        holder.tvTime.setText(formatDate(order.getCreatedAt()));

        // Set Customer Info
        if (order.getUser() != null) {
            String info = order.getUser().getFullName();
            if (order.getShippingPhone() != null && !order.getShippingPhone().isEmpty()) {
                info += " • " + order.getShippingPhone();
            } else if (order.getUser().getPhoneNumber() != null) {
                info += " • " + order.getUser().getPhoneNumber();
            }
            holder.tvCustomerInfo.setText(info);
        } else {
            holder.tvCustomerInfo.setText("Khách vãng lai");
        }

        // Set Status Badge
        String status = order.getStatus();
        holder.tvStatusBadge.setText(status);
        setStatusColor(holder.tvStatusBadge, status);

        // Set Product Name
        if (order.getOrderDetails() != null && !order.getOrderDetails().isEmpty()) {
            String productName = order.getOrderDetails().get(0).getProduct().getName();
            int quantity = order.getOrderDetails().get(0).getQuantity();
            if (order.getOrderDetails().size() > 1) {
                holder.tvProductName.setText(quantity + "x " + productName + " và " + (order.getOrderDetails().size() - 1) + " sản phẩm khác");
            } else {
                holder.tvProductName.setText(quantity + "x " + productName);
            }
        } else {
            holder.tvProductName.setText("Không có sản phẩm");
        }

        // Set Total Price
        holder.tvPrice.setText(currencyFormat.format(order.getTotalAmount()));

        // Xử lý sự kiện click vào item
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onOrderClick(order);
            }
        });
    }

    @Override
    public int getItemCount() {
        return orderList == null ? 0 : orderList.size();
    }

    private String formatDate(String dateString) {
        if (dateString == null) return "";
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                LocalDateTime dateTime = LocalDateTime.parse(dateString);
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");
                return dateTime.format(formatter);
            }
            return dateString;
        } catch (DateTimeParseException e) {
            return dateString;
        }
    }

    private void setStatusColor(TextView tv, String status) {
        if (status == null) return;
        
        tv.setBackgroundResource(R.drawable.bg_status_pending); 
        
        switch (status) {
            case "Đơn hàng mới":
            case "Chờ xác nhận":
                tv.setTextColor(Color.parseColor("#FF9800")); // Orange
                tv.setBackgroundResource(R.drawable.bg_status_pending);
                break;
            case "Chờ lấy hàng":
            case "Đang giao":
                tv.setTextColor(Color.parseColor("#2196F3")); // Blue
                break;
            case "Giao thành công":
                tv.setTextColor(Color.parseColor("#4CAF50")); // Green
                break;
            case "Đã huỷ":
            case "Giao thất bại":
                tv.setTextColor(Color.parseColor("#F44336")); // Red
                break;
            default:
                tv.setTextColor(Color.GRAY);
                break;
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvTime, tvCustomerInfo, tvStatusBadge, tvProductName, tvPrice;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvCustomerInfo = itemView.findViewById(R.id.tvCustomerInfo);
            tvStatusBadge = itemView.findViewById(R.id.tvStatusBadge);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
        }
    }
}
