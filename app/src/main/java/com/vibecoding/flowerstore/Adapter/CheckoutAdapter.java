package com.vibecoding.flowerstore.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.vibecoding.flowerstore.Model.CartItem;
import com.vibecoding.flowerstore.Model.Product;
import com.vibecoding.flowerstore.R;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class CheckoutAdapter extends RecyclerView.Adapter<CheckoutAdapter.CheckoutViewHolder> {

    private List<CartItem> cartItems;
    private Context context;

    public CheckoutAdapter(List<CartItem> cartItems, Context context) {
        this.cartItems = cartItems;
        this.context = context;
    }

    @NonNull
    @Override
    public CheckoutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Tái sử dụng layout item_cart nhưng sẽ ẩn các nút không cần thiết
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        return new CheckoutViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CheckoutViewHolder holder, int position) {
        CartItem item = cartItems.get(position);
        Product product = item.getProduct();

        holder.productName.setText(product.getName());
        
        // Sử dụng EditText để hiển thị số lượng (read-only)
        holder.quantityInput.setText("x" + item.getQuantity());
        holder.quantityInput.setEnabled(false); // Không cho sửa
        holder.quantityInput.setFocusable(false);
        holder.quantityInput.setBackground(null); // Xóa background nếu có

        double unitPrice = 0;
        if (item.getQuantity() > 0) {
            unitPrice = item.getSubtotal() / item.getQuantity();
        }
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        holder.productPrice.setText(currencyFormat.format(unitPrice));

        Glide.with(context)
                .load(product.getImage())
                .placeholder(R.drawable.placeholder_product)
                .into(holder.productImage);

        // Ẩn các nút điều khiển vì đây chỉ là màn hình xem lại đơn hàng
        holder.increaseButton.setVisibility(View.GONE);
        holder.decreaseButton.setVisibility(View.GONE);
        holder.deleteButton.setVisibility(View.GONE);
        
        // Ẩn checkbox nếu có
        if (holder.itemCheckbox != null) {
            holder.itemCheckbox.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    public static class CheckoutViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage;
        TextView productName, productPrice;
        EditText quantityInput;
        View increaseButton, decreaseButton, deleteButton, itemCheckbox;

        public CheckoutViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.product_image);
            productName = itemView.findViewById(R.id.product_name);
            productPrice = itemView.findViewById(R.id.product_price);
            quantityInput = itemView.findViewById(R.id.quantity_input); // Đã đổi ID từ quantity_text sang quantity_input
            increaseButton = itemView.findViewById(R.id.increase_button);
            decreaseButton = itemView.findViewById(R.id.decrease_button);
            deleteButton = itemView.findViewById(R.id.delete_button);
            itemCheckbox = itemView.findViewById(R.id.item_checkbox);
        }
    }
}
