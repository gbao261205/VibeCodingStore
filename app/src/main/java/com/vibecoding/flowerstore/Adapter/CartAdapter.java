package com.vibecoding.flowerstore.Adapter;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.vibecoding.flowerstore.Model.CartItem;
import com.vibecoding.flowerstore.Model.Product;
import com.vibecoding.flowerstore.R;

import java.text.NumberFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private List<CartItem> cartItems;
    private Context context;
    private CartListener listener;
    private boolean isDeleteMode = false;
    private Set<Integer> selectedItems = new HashSet<>();
    private static final int MAX_QUANTITY = 20;

    public interface CartListener {
        void onQuantityChanged(int productId, int newQuantity);
        void onRemoveItem(int productId);
        void onSelectionChanged(int selectedCount);
        void onTotalPriceUpdated(); // Callback để báo Activity cập nhật tổng tiền
    }

    public CartAdapter(List<CartItem> cartItems, Context context, CartListener listener) {
        this.cartItems = cartItems;
        this.context = context;
        this.listener = listener;
    }

    public void setDeleteMode(boolean deleteMode) {
        isDeleteMode = deleteMode;
        if (!deleteMode) {
            selectedItems.clear();
        }
        notifyDataSetChanged();
    }

    public boolean isDeleteMode() {
        return isDeleteMode;
    }

    public Set<Integer> getSelectedItems() {
        return selectedItems;
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
        Product product = item.getProduct();

        holder.productName.setText(product.getName());
        
        // Remove text change listener to avoid infinite loop when setting text
        if (holder.quantityInput.getTag() instanceof TextWatcher) {
            holder.quantityInput.removeTextChangedListener((TextWatcher) holder.quantityInput.getTag());
        }
        holder.quantityInput.setText(String.valueOf(item.getQuantity()));

        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

        double discountedPrice = product.getDiscountedPrice();
        double originalPrice = product.getPrice();

        if (discountedPrice > 0 && discountedPrice < originalPrice) {
            holder.productPrice.setText(currencyFormat.format(discountedPrice));
        } else {
            holder.productPrice.setText(currencyFormat.format(originalPrice));
        }

        Glide.with(context)
                .load(product.getImage())
                .placeholder(R.drawable.placeholder_product)
                .into(holder.productImage);

        // Handle delete mode visibility
        if (isDeleteMode) {
            holder.itemCheckbox.setVisibility(View.VISIBLE);
            holder.itemCheckbox.setChecked(selectedItems.contains(product.getId()));
            
            // In delete mode, disable quantity controls
            holder.increaseButton.setVisibility(View.INVISIBLE);
            holder.decreaseButton.setVisibility(View.INVISIBLE);
            holder.quantityInput.setVisibility(View.INVISIBLE);
        } else {
            holder.itemCheckbox.setVisibility(View.GONE);
            holder.increaseButton.setVisibility(View.VISIBLE);
            holder.decreaseButton.setVisibility(View.VISIBLE);
            holder.quantityInput.setVisibility(View.VISIBLE);
        }

        holder.itemCheckbox.setOnClickListener(v -> {
            if (holder.itemCheckbox.isChecked()) {
                selectedItems.add(product.getId());
            } else {
                selectedItems.remove(product.getId());
            }
            listener.onSelectionChanged(selectedItems.size());
        });

        holder.increaseButton.setOnClickListener(v -> {
            int newQuantity = item.getQuantity() + 1;
            if (newQuantity > MAX_QUANTITY) {
                newQuantity = MAX_QUANTITY;
                Toast.makeText(context, "Số lượng tối đa là " + MAX_QUANTITY, Toast.LENGTH_SHORT).show();
            }
            updateQuantity(item, newQuantity, holder);
        });

        holder.decreaseButton.setOnClickListener(v -> {
            int newQuantity = item.getQuantity() - 1;
            if (newQuantity > 0) {
                updateQuantity(item, newQuantity, holder);
            } else {
                listener.onRemoveItem(product.getId());
            }
        });
        
        // TextWatcher for direct input
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String input = s.toString();
                if (input.isEmpty()) return;

                try {
                    int newQuantity = Integer.parseInt(input);
                    if (newQuantity != item.getQuantity()) {
                        if (newQuantity > MAX_QUANTITY) {
                            newQuantity = MAX_QUANTITY;
                            holder.quantityInput.setText(String.valueOf(newQuantity));
                            holder.quantityInput.setSelection(holder.quantityInput.getText().length());
                            Toast.makeText(context, "Số lượng tối đa là " + MAX_QUANTITY, Toast.LENGTH_SHORT).show();
                        } else if (newQuantity < 1) {
                            newQuantity = 1;
                             holder.quantityInput.setText(String.valueOf(newQuantity));
                             holder.quantityInput.setSelection(holder.quantityInput.getText().length());
                        }
                        
                        // Update model silently first
                        item.setQuantity(newQuantity);
                        // Notify listener to call API (debounced in Activity)
                        listener.onQuantityChanged(item.getProduct().getId(), newQuantity);
                        // Notify to update total price locally immediately
                        listener.onTotalPriceUpdated();
                    }
                } catch (NumberFormatException e) {
                    // Ignore invalid input
                }
            }
        };
        holder.quantityInput.addTextChangedListener(textWatcher);
        holder.quantityInput.setTag(textWatcher);

        // Handle "Done" action on keyboard
        holder.quantityInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                holder.quantityInput.clearFocus();
                return true;
            }
            return false;
        });
        
        holder.quantityInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String input = holder.quantityInput.getText().toString();
                if (input.isEmpty() || input.equals("0")) {
                     holder.quantityInput.setText("1");
                     updateQuantity(item, 1, holder);
                }
            }
        });

        holder.deleteButton.setOnClickListener(v -> {
            listener.onRemoveItem(product.getId());
        });
    }
    
    private void updateQuantity(CartItem item, int newQuantity, CartViewHolder holder) {
        if (item.getQuantity() == newQuantity) return;
        
        item.setQuantity(newQuantity);
        
        // Temporarily remove watcher to avoid triggering it
        if (holder.quantityInput.getTag() instanceof TextWatcher) {
            holder.quantityInput.removeTextChangedListener((TextWatcher) holder.quantityInput.getTag());
        }
        
        holder.quantityInput.setText(String.valueOf(newQuantity));
        
        // Re-add watcher
        if (holder.quantityInput.getTag() instanceof TextWatcher) {
            holder.quantityInput.addTextChangedListener((TextWatcher) holder.quantityInput.getTag());
        }

        listener.onQuantityChanged(item.getProduct().getId(), newQuantity);
        listener.onTotalPriceUpdated();
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
        TextView productName, productPrice;
        EditText quantityInput; // Changed to EditText
        ImageButton increaseButton, decreaseButton, deleteButton;
        CheckBox itemCheckbox;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.product_image);
            productName = itemView.findViewById(R.id.product_name);
            productPrice = itemView.findViewById(R.id.product_price);
            quantityInput = itemView.findViewById(R.id.quantity_input); // Bind new ID
            increaseButton = itemView.findViewById(R.id.increase_button);
            decreaseButton = itemView.findViewById(R.id.decrease_button);
            deleteButton = itemView.findViewById(R.id.delete_button);
            itemCheckbox = itemView.findViewById(R.id.item_checkbox);
        }
    }
}
