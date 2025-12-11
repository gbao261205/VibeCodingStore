package com.vibecoding.flowerstore.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.vibecoding.flowerstore.Model.CartItem;
import com.vibecoding.flowerstore.R;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private List<CartItem> cartItems;
    private Context context;
    private CartListener listener;

    // Interface to communicate with the Activity
    public interface CartListener {
        void onQuantityChanged(int productId, int newQuantity);
        void onRemoveItem(int productId);
    }

    public CartAdapter(List<CartItem> cartItems, Context context, CartListener listener) {
        this.cartItems = cartItems;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = cartItems.get(position);

        holder.productName.setText(item.getProduct().getName());
        holder.quantityText.setText(String.valueOf(item.getQuantity()));

        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        holder.productPrice.setText(currencyFormat.format(item.getProduct().getDiscountedPrice()));

        Glide.with(context)
                .load(item.getProduct().getImage()) // Corrected from getImageUrl()
                .placeholder(R.drawable.placeholder_product)
                .into(holder.productImage);

        // --- Event Listeners ---
        holder.increaseButton.setOnClickListener(v -> {
            int newQuantity = item.getQuantity() + 1;
            listener.onQuantityChanged(item.getProduct().getId(), newQuantity);
        });

        holder.decreaseButton.setOnClickListener(v -> {
            int newQuantity = item.getQuantity() - 1;
            if (newQuantity > 0) {
                listener.onQuantityChanged(item.getProduct().getId(), newQuantity);
            } else {
                // If quantity becomes 0, remove the item
                listener.onRemoveItem(item.getProduct().getId());
            }
        });
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    public void updateData(List<CartItem> newItems) {
        this.cartItems = newItems;
        notifyDataSetChanged();
    }

    public static class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage;
        TextView productName, productPrice, quantityText;
        Button increaseButton, decreaseButton;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.product_image);
            productName = itemView.findViewById(R.id.product_name);
            productPrice = itemView.findViewById(R.id.product_price);
            quantityText = itemView.findViewById(R.id.quantity_text);
            increaseButton = itemView.findViewById(R.id.increase_button);
            decreaseButton = itemView.findViewById(R.id.decrease_button);
        }
    }
}
