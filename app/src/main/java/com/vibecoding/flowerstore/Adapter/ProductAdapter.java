package com.vibecoding.flowerstore.Adapter;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.vibecoding.flowerstore.Model.Product;
import com.vibecoding.flowerstore.R;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private List<Product> productList;
    private Context context;

    public ProductAdapter(List<Product> productList, Context context) {
        this.productList = productList;
        this.context = context;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Sử dụng layout item_product_card.xml của bạn
        View view = LayoutInflater.from(context).inflate(R.layout.item_product_card, parent, false);
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

    // Phương thức để cập nhật dữ liệu từ MainActivity
    public void updateData(List<Product> newProducts) {
        this.productList.clear();
        this.productList.addAll(newProducts);
        notifyDataSetChanged(); // Báo cho RecyclerView vẽ lại giao diện
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage;
        TextView productName;
        TextView productDiscountPrice;
        TextView productOriginalPrice;
        MaterialButton viewDetailsButton;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            // Ánh xạ các view từ file item_product_card.xml
            productImage = itemView.findViewById(R.id.product_image);
            productName = itemView.findViewById(R.id.product_name);
            productDiscountPrice = itemView.findViewById(R.id.product_discount_price);
            productOriginalPrice = itemView.findViewById(R.id.product_original_price);
            viewDetailsButton = itemView.findViewById(R.id.view_details_button);
        }

        public void bind(Product product) {
            productName.setText(product.getName());

            // Format giá tiền theo định dạng Việt Nam (ví dụ: 150.000 ₫)
            NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            productDiscountPrice.setText(currencyFormatter.format(product.getPrice()));

            // Vì API không có giá gốc, chúng ta sẽ ẩn TextView này đi
            productOriginalPrice.setVisibility(View.GONE);

            // Nếu trong tương lai API có giá gốc, bạn có thể dùng code sau:
            // productOriginalPrice.setText(currencyFormatter.format(product.getOriginalPrice()));
            // productOriginalPrice.setPaintFlags(productOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG); // Gạch ngang giá
            // productOriginalPrice.setVisibility(View.VISIBLE);


            // Sử dụng Glide để tải hình ảnh từ URL (bạn đã có thư viện này)
            Glide.with(itemView.getContext())
                    .load(product.getImageUrl())
                    .placeholder(R.drawable.ic_launcher_background) // Thay bằng ảnh placeholder của bạn
                    .error(R.drawable.ic_launcher_background)       // Thay bằng ảnh báo lỗi của bạn
                    .into(productImage);

            // Bắt sự kiện click cho nút "Xem Chi Tiết"
            viewDetailsButton.setOnClickListener(v -> {
                // Xử lý sự kiện khi người dùng nhấn nút, ví dụ: chuyển sang màn hình chi tiết sản phẩm
                Toast.makeText(itemView.getContext(), "Xem chi tiết: " + product.getName(), Toast.LENGTH_SHORT).show();
            });
        }
    }
}
