package com.vibecoding.flowerstore.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.vibecoding.flowerstore.Model.ProductDTO;
import com.vibecoding.flowerstore.R;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdminProductAdapter extends RecyclerView.Adapter<AdminProductAdapter.ViewHolder> {

    private Context context;
    private List<ProductDTO> productList;

    public AdminProductAdapter(Context context, List<ProductDTO> productList) {
        this.context = context;
        this.productList = productList;
    }

    public void setProductList(List<ProductDTO> productList) {
        this.productList = productList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product_admin, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ProductDTO product = productList.get(position);
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

        holder.tvProductName.setText(product.getName());

        if (product.getCategory() != null) {
            holder.tvProductCategory.setText(product.getCategory().getName());
        } else {
            holder.tvProductCategory.setText("Chưa phân loại");
        }

        // Giá và giảm giá
        if (product.getDiscountedPrice() != null && product.getDiscountedPrice().compareTo(product.getPrice()) < 0) {
            holder.tvProductPrice.setText(currencyFormat.format(product.getDiscountedPrice()));
            // Có thể hiển thị giá gốc bị gạch ngang nếu muốn, nhưng layout hiện tại chỉ có 1 textview giá
            // holder.tvOriginalPrice.setText(currencyFormat.format(product.getPrice()));
            // holder.tvOriginalPrice.setPaintFlags(holder.tvOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.tvProductPrice.setText(currencyFormat.format(product.getPrice()));
        }

        holder.tvProductStock.setText("Kho: " + product.getStock());

        // Trạng thái
        if (product.isActive()) {
            holder.tvStatus.setText("Hiện");
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_green);
            holder.tvStatus.setTextColor(Color.parseColor("#166534"));
        } else {
            holder.tvStatus.setText("Ẩn");
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_gray);
            holder.tvStatus.setTextColor(Color.parseColor("#374151"));
        }

        // Ảnh
        Glide.with(context)
                .load(product.getImageUrl())
                .placeholder(R.drawable.ic_local_florist) // Cần ảnh placeholder
                .error(R.drawable.ic_local_florist)
                .into(holder.imgProduct);
    }

    @Override
    public int getItemCount() {
        return productList == null ? 0 : productList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView tvProductName, tvProductCategory, tvProductPrice, tvProductStock, tvStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvProductCategory = itemView.findViewById(R.id.tvProductCategory);
            tvProductPrice = itemView.findViewById(R.id.tvProductPrice);
            tvProductStock = itemView.findViewById(R.id.tvProductStock);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }
}
