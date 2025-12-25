package com.vibecoding.flowerstore.Adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.vibecoding.flowerstore.Activity.ProductDetailActivity;
import com.vibecoding.flowerstore.Model.DataStore; // Import DataStore để đồng bộ cache
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
        Product product = productList.get(position);
        holder.bind(product);
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

    // --- CLASS VIEWHOLDER ---
    class ProductViewHolder extends RecyclerView.ViewHolder {

        ImageView productImage;
        TextView productTitle;
        TextView productPrice;
        ImageButton favoriteButton;

        ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.image_product);
            productTitle = itemView.findViewById(R.id.text_product_title);
            productPrice = itemView.findViewById(R.id.text_product_price);
            favoriteButton = itemView.findViewById(R.id.button_favorite);
        }

        void bind(Product product) {
            // 1. Set thông tin cơ bản
            productTitle.setText(product.getName());
            NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            productPrice.setText(currencyFormatter.format(product.getPrice()));

            // 2. Load ảnh (Dùng Cache Strategy để load nhanh hơn)
            Glide.with(context)
                    .load(product.getImage())
                    .diskCacheStrategy(DiskCacheStrategy.ALL) // Lưu cache ảnh
                    .placeholder(R.drawable.placeholder_product)
                    .error(R.drawable.placeholder_product)
                    .into(productImage);

            // 3. --- LOGIC CHECK TRẠNG THÁI TIM (QUAN TRỌNG) ---
            // Kiểm tra xem sản phẩm này đã có trong Wishlist chưa để hiện tim đỏ
            boolean isFavorite = false;
            if (DataStore.cachedFavorites != null) {
                for (Product p : DataStore.cachedFavorites) {
                    if (p.getId() == product.getId()) {
                        isFavorite = true;
                        break;
                    }
                }
            }

            // Set icon dựa trên trạng thái cache
            if (isFavorite) {
                favoriteButton.setImageResource(R.drawable.ic_heart_filled); // Tim đỏ
                // Nếu muốn ngăn user bấm lại khi đã thích rồi thì uncomment dòng dưới:
                // favoriteButton.setEnabled(false);
            } else {
                favoriteButton.setImageResource(R.drawable.ic_heart_outline); // Tim rỗng
                favoriteButton.setEnabled(true);
            }

            // 4. Click vào item -> Mở chi tiết
            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, ProductDetailActivity.class);
                intent.putExtra("product_id", product.getId());
                context.startActivity(intent);
            });

            // 5. --- CLICK NÚT TIM (OPTIMISTIC UPDATE) ---
            favoriteButton.setOnClickListener(v -> {
                handleFavoriteClick(product, favoriteButton);
            });
        }
    }

    // --- HÀM XỬ LÝ CLICK TIM ---
    private void handleFavoriteClick(Product product, ImageButton btnFavorite) {
        // A. Kiểm tra đăng nhập
        SharedPreferences prefs = context.getSharedPreferences("MY_APP_PREFS", Context.MODE_PRIVATE);
        String token = prefs.getString("ACCESS_TOKEN", null);

        if (token == null) {
            Toast.makeText(context, "Vui lòng đăng nhập!", Toast.LENGTH_SHORT).show();
            return;
        }

        // B. HIỆU ỨNG NGAY LẬP TỨC (Optimistic UI)
        // 1. Đổi icon sang đỏ ngay
        btnFavorite.setImageResource(R.drawable.ic_heart_filled);
        // 2. Disable nút để tránh spam click trong lúc đang gọi API
        btnFavorite.setEnabled(false);

        // C. CẬP NHẬT CACHE CỤC BỘ NGAY (Để các trang khác thấy ngay)
        if (DataStore.cachedFavorites != null) {
            // Kiểm tra trùng lặp trước khi thêm
            boolean exists = false;
            for (Product p : DataStore.cachedFavorites) {
                if (p.getId() == product.getId()) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                // Thêm vào đầu danh sách
                DataStore.cachedFavorites.add(0, product);
            }
        } else {
            // Nếu cache chưa khởi tạo thì tạo mới luôn
            DataStore.cachedFavorites = new ArrayList<>();
            DataStore.cachedFavorites.add(product);
        }

        // D. GỌI API NGẦM (Silent Request)
        callAddToWishlistApi(product, btnFavorite);
    }

    // --- GỌI API ---
    private void callAddToWishlistApi(Product product, ImageButton btnFavorite) {
        ApiService apiService = RetrofitClient.getClient(context).create(ApiService.class);

        apiService.addToWishlist(product.getId()).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                // Mở lại nút (nếu muốn cho phép user thao tác tiếp, ví dụ để bỏ tim)
                // Ở đây mình giữ disable hoặc enable tùy logic app của bạn
                btnFavorite.setEnabled(true);

                if (response.isSuccessful()) {
                    // THÀNH CÔNG: Không cần làm gì cả vì UI & Cache đã update ở bước trên rồi!
                    Log.d("ProductAdapter", "Đã đồng bộ Server thành công: " + product.getName());
                } else {
                    // LỖI SERVER (VD: 401, 500): PHẢI HOÀN TÁC (Rollback)
                    revertUI(product, btnFavorite);
                    Toast.makeText(context, "Lỗi: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                // LỖI MẠNG: PHẢI HOÀN TÁC
                btnFavorite.setEnabled(true);
                revertUI(product, btnFavorite);
                Toast.makeText(context, "Lỗi kết nối mạng!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // --- HÀM HOÀN TÁC (ROLLBACK) NẾU LỖI ---
    private void revertUI(Product product, ImageButton btnFavorite) {
        // 1. Đổi lại icon rỗng
        btnFavorite.setImageResource(R.drawable.ic_heart_outline);

        // 2. Xóa khỏi cache cục bộ (Vì nãy lỡ thêm vào rồi)
        if (DataStore.cachedFavorites != null) {
            // Dùng removeIf (Java 8+) hoặc vòng lặp để xóa đúng ID
            try {
                DataStore.cachedFavorites.removeIf(p -> p.getId() == product.getId());
            } catch (Exception e) {
                for (int i = 0; i < DataStore.cachedFavorites.size(); i++) {
                    if (DataStore.cachedFavorites.get(i).getId() == product.getId()) {
                        DataStore.cachedFavorites.remove(i);
                        break;
                    }
                }
            }
        }
    }
}