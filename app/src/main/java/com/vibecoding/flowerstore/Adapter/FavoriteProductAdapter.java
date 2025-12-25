package com.vibecoding.flowerstore.Adapter;

import android.content.Context;
import android.content.Intent; // Import Intent
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.vibecoding.flowerstore.Activity.ProductDetailActivity; // Import trang chi tiết
import com.vibecoding.flowerstore.Model.DataStore;
import com.vibecoding.flowerstore.Model.Product;
import com.vibecoding.flowerstore.R;
import com.vibecoding.flowerstore.Service.ApiService;
import com.vibecoding.flowerstore.Service.RetrofitClient;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FavoriteProductAdapter extends RecyclerView.Adapter<FavoriteProductAdapter.ViewHolder> {

    private List<Product> productList;
    private Context context;

    public FavoriteProductAdapter(List<Product> productList, Context context) {
        this.productList = productList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_favorite_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = productList.get(position);

        // 1. Hiển thị thông tin
        holder.tvName.setText(product.getName());
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        holder.tvPrice.setText(currencyFormatter.format(product.getPrice()));

        if (product.getImage() != null && !product.getImage().isEmpty()) {
            Glide.with(context)
                    .load(product.getImage())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .override(300, 300)
                    .placeholder(R.drawable.placeholder_product)
                    .error(R.drawable.placeholder_product)
                    .into(holder.imgProduct);
        } else {
            holder.imgProduct.setImageResource(R.drawable.banner1);
        }

        // 2. Sự kiện thêm vào giỏ hàng
        holder.btnAddCart.setOnClickListener(v -> {
            Toast.makeText(context, "Đã thêm vào giỏ", Toast.LENGTH_SHORT).show();
            // TODO: Gọi API thêm vào giỏ nếu cần
        });

        // 3. Sự kiện Xóa yêu thích (Optimistic UI)
        holder.btnFavorite.setOnClickListener(v -> {
            int currentPos = holder.getAdapterPosition();
            if (currentPos != RecyclerView.NO_POSITION) {
                deleteItemOptimistic(currentPos, product.getId());
            }
        });

        // --- 4. SỰ KIỆN CLICK VÀO SẢN PHẨM -> MỞ CHI TIẾT (MỚI THÊM) ---
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ProductDetailActivity.class);
            // Truyền ID để bên kia gọi API hoặc tìm trong cache
            intent.putExtra("product_id", product.getId());
            context.startActivity(intent);
        });
    }

    // Hàm xóa nhanh (như cũ)
    private void deleteItemOptimistic(int position, int productId) {
        Product removedProduct = productList.get(position);

        // Xóa UI
        productList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, productList.size());

        // Xóa Cache
        if (DataStore.cachedFavorites != null) {
            try {
                DataStore.cachedFavorites.removeIf(p -> p.getId() == productId);
            } catch (Exception e) {
                for (int i = 0; i < DataStore.cachedFavorites.size(); i++) {
                    if (DataStore.cachedFavorites.get(i).getId() == productId) {
                        DataStore.cachedFavorites.remove(i);
                        break;
                    }
                }
            }
        }

        // Gọi API ngầm
        ApiService apiService = RetrofitClient.getClient(context).create(ApiService.class);
        apiService.removeFromWishlist(productId).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (!response.isSuccessful()) {
                    // Lỗi -> Hoàn tác
                    Toast.makeText(context, "Lỗi server, khôi phục lại!", Toast.LENGTH_SHORT).show();
                    productList.add(position, removedProduct);
                    notifyItemInserted(position);
                    if(DataStore.cachedFavorites != null) DataStore.cachedFavorites.add(removedProduct);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                // Lỗi mạng -> Hoàn tác
                Toast.makeText(context, "Lỗi mạng!", Toast.LENGTH_SHORT).show();
                productList.add(position, removedProduct);
                notifyItemInserted(position);
                if(DataStore.cachedFavorites != null) DataStore.cachedFavorites.add(removedProduct);
            }
        });
    }

    @Override
    public int getItemCount() {
        return productList != null ? productList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView tvName, tvPrice;
        ImageButton btnFavorite, btnAddCart;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.img_product);
            tvName = itemView.findViewById(R.id.tv_product_name);
            tvPrice = itemView.findViewById(R.id.tv_product_price);
            btnFavorite = itemView.findViewById(R.id.btn_favorite);
            btnAddCart = itemView.findViewById(R.id.btn_add_to_cart);
        }
    }
}