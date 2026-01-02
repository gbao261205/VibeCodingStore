package com.vibecoding.flowerstore.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.vibecoding.flowerstore.Model.Category;
import com.vibecoding.flowerstore.R;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private List<Category> categoryList;
    private Context context;
    private OnCategoryClickListener listener; // 1. Khai báo listener

    // 2. Tạo Interface để MainActivity có thể implement
    public interface OnCategoryClickListener {
        void onCategoryClick(Category category);
    }

    // 3. Cập nhật Constructor nhận thêm listener
    public CategoryAdapter(List<Category> categoryList, Context context, OnCategoryClickListener listener) {
        this.categoryList = categoryList;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categoryList.get(position);

        // Gán dữ liệu
        holder.categoryName.setText(category.getName());

        // 4. Bắt sự kiện Click vào toàn bộ item
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCategoryClick(category); // Gọi hàm callback
            }
        });
    }

    @Override
    public int getItemCount() {
        return categoryList != null ? categoryList.size() : 0;
    }

    public void updateData(List<Category> newCategories) {
        // Cách cập nhật an toàn hơn để tránh lỗi null
        if (this.categoryList == null) {
            this.categoryList = newCategories;
        } else {
            this.categoryList.clear();
            this.categoryList.addAll(newCategories);
        }
        notifyDataSetChanged();
    }

    public static class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView categoryName;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryName = itemView.findViewById(R.id.text_category_name);
        }
    }
}