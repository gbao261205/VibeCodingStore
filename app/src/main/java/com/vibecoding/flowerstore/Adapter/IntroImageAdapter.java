package com.vibecoding.flowerstore.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.vibecoding.flowerstore.R;

import java.util.List;

public class IntroImageAdapter extends RecyclerView.Adapter<IntroImageAdapter.IntroViewHolder> {

    private final List<Integer> imageList;

    public IntroImageAdapter(List<Integer> imageList) {
        this.imageList = imageList;
    }

    @NonNull
    @Override
    public IntroViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // --- ĐÂY LÀ PHẦN SỬA LỖI QUAN TRỌNG ---
        // Sử dụng LayoutInflater để tạo view từ một file layout XML.
        // Phương thức này đảm bảo rằng LayoutParams được tạo ra chính xác từ `parent` (chính là ViewPager2).
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_slide, parent, false);
        return new IntroViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IntroViewHolder holder, int position) {
        holder.bind(imageList.get(position));
    }

    @Override
    public int getItemCount() {
        return imageList.size();
    }

    // ViewHolder để giữ và quản lý View cho mỗi item
    public static class IntroViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageView;

        public IntroViewHolder(@NonNull View itemView) {
            super(itemView);
            // Ánh xạ ImageView từ layout item_intro_image.xml
            imageView = itemView.findViewById(R.id.intro_slide_image);
        }

        public void bind(int imageResId) {
            imageView.setImageResource(imageResId);
        }
    }
}
