package com.vibecoding.flowerstore.Adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.vibecoding.flowerstore.Activity.ProductDetailActivity;
import com.vibecoding.flowerstore.Model.DataStore;
import com.vibecoding.flowerstore.Model.Product;
import com.vibecoding.flowerstore.R;
import com.vibecoding.flowerstore.Service.ApiService;
import com.vibecoding.flowerstore.Service.RetrofitClient;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private List<Product> productList;
    private final Context context;

    public ProductAdapter(List<Product> productList, Context context) {
        this.productList = productList;
        this.context = context;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        holder.bind(productList.get(position));
    }

    @Override
    public int getItemCount() {
        return productList != null ? productList.size() : 0;
    }

    public void updateData(List<Product> newProductList) {
        this.productList.clear();
        this.productList.addAll(newProductList);
        notifyDataSetChanged();
    }

    class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage;
        TextView productTitle, productPrice;
        ImageButton favoriteButton;

        ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.image_product);
            productTitle = itemView.findViewById(R.id.text_product_title);
            productPrice = itemView.findViewById(R.id.text_product_price);
            favoriteButton = itemView.findViewById(R.id.button_favorite);
        }

        void bind(Product product) {
            productTitle.setText(product.getName());
            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            productPrice.setText(formatter.format(product.getPrice()));

            Glide.with(context)
                    .load(product.getImage())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.placeholder_product)
                    .into(productImage);

            // --- KIỂM TRA TRẠNG THÁI TIM TỪ CACHE (QUAN TRỌNG) ---
            boolean isFavorite = checkIsFavorite(product.getId());
            if (isFavorite) {
                favoriteButton.setImageResource(R.drawable.ic_heart_filled); // Tim đỏ
            } else {
                favoriteButton.setImageResource(R.drawable.ic_heart_outline); // Tim trắng
            }

            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, ProductDetailActivity.class);
                intent.putExtra("product_id", product.getId());
                context.startActivity(intent);
            });

            // --- XỬ LÝ CLICK TIM (TOGGLE) ---
            favoriteButton.setOnClickListener(v -> handleToggleFavorite(product, favoriteButton));
        }

        private boolean checkIsFavorite(int id) {
            if (DataStore.cachedFavorites == null) return false;
            for (Product p : DataStore.cachedFavorites) {
                if (p.getId() == id) return true;
            }
            return false;
        }

        private void handleToggleFavorite(Product product, ImageButton btn) {
            SharedPreferences prefs = context.getSharedPreferences("MY_APP_PREFS", Context.MODE_PRIVATE);
            String token = prefs.getString("ACCESS_TOKEN", null);

            if (token == null) {
                Toast.makeText(context, "Vui lòng đăng nhập!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (DataStore.cachedFavorites == null) DataStore.cachedFavorites = new ArrayList<>();

            boolean currentlyLiked = checkIsFavorite(product.getId());

            if (currentlyLiked) {
                // TRƯỜNG HỢP 1: Đang thích -> Bấm để BỎ thích
                btn.setImageResource(R.drawable.ic_heart_outline); // Đổi icon ngay
                DataStore.cachedFavorites.removeIf(p -> p.getId() == product.getId()); // Xóa khỏi cache
                syncWithServer(product.getId(), false, btn); // Gọi API
            } else {
                // TRƯỜNG HỢP 2: Chưa thích -> Bấm để THÊM thích
                btn.setImageResource(R.drawable.ic_heart_filled); // Đổi icon ngay
                DataStore.cachedFavorites.add(0, product); // Thêm vào cache
                syncWithServer(product.getId(), true, btn); // Gọi API
            }
        }

        private void syncWithServer(int productId, boolean isAdding, ImageButton btn) {
            ApiService apiService = RetrofitClient.getClient(context).create(ApiService.class);

            // Giả sử API dùng chung addToWishlist cho cả thêm/xóa (Toggle)
            apiService.addToWishlist(productId).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (!response.isSuccessful()) {
                        // Nếu server lỗi -> Quay lại trạng thái cũ (Rollback)
                        rollback(productId, isAdding, btn);
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    rollback(productId, isAdding, btn);
                }
            });
        }

        private void rollback(int productId, boolean wasAdding, ImageButton btn) {
            // Hoàn tác nếu lỗi
            if (wasAdding) {
                DataStore.cachedFavorites.removeIf(p -> p.getId() == productId);
                btn.setImageResource(R.drawable.ic_heart_outline);
            } else {
                // Logic thêm lại hơi phức tạp vì cần object Product gốc,
                // ở đây ta tạm thời chỉ đổi icon lại
                btn.setImageResource(R.drawable.ic_heart_filled);
            }
            Toast.makeText(context, "Lỗi kết nối!", Toast.LENGTH_SHORT).show();
        }
    }
}