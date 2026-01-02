package com.vibecoding.flowerstore.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.vibecoding.flowerstore.Model.Review;
import com.vibecoding.flowerstore.R;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class FeedbackAdapter extends RecyclerView.Adapter<FeedbackAdapter.FeedbackViewHolder> {

    private List<Review> reviewList;
    private Context context;

    // BASE URL của Server chứa ảnh (Nếu ảnh là đường dẫn tương đối)
    // Bạn thay link này bằng link gốc server của bạn
    private static final String BASE_IMAGE_URL = "https://holetinnghia-vibe-coding-store-api.hf.space/";

    public FeedbackAdapter(List<Review> reviewList, Context context) {
        this.reviewList = reviewList;
        this.context = context;
    }

    @NonNull
    @Override
    public FeedbackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_feedback, parent, false);
        return new FeedbackViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FeedbackViewHolder holder, int position) {
        Review review = reviewList.get(position);

        // 1. Rating
        holder.ratingBar.setRating(review.getRating());

        // 2. Nội dung comment
        if (review.getComment() == null || review.getComment().trim().isEmpty()) {
            holder.tvContent.setVisibility(View.GONE); // Ẩn nếu ko có comment
        } else {
            holder.tvContent.setVisibility(View.VISIBLE);
            holder.tvContent.setText(review.getComment());
        }

        // 3. Xử lý Ngày tháng (Cắt chuỗi cho an toàn)
        // JSON: "2025-12-02T15:53:35.000+00:00"
        String rawDate = review.getReviewDate();
        if (rawDate != null && rawDate.length() >= 10) {
            try {
                // Lấy 10 ký tự đầu: "2025-12-02"
                String datePart = rawDate.substring(0, 10);
                // Chuyển thành 02/12/2025
                String[] parts = datePart.split("-");
                if (parts.length == 3) {
                    holder.tvDate.setText(parts[2] + "/" + parts[1] + "/" + parts[0]);
                } else {
                    holder.tvDate.setText(datePart);
                }
            } catch (Exception e) {
                holder.tvDate.setText(rawDate);
            }
        }

        // 4. Xử lý User & Avatar
        if (review.getUser() != null) {
            holder.tvUsername.setText(review.getUser().getFullName());

            String avatarUrl = review.getUser().getAvatar();

            // LOGIC QUAN TRỌNG: Kiểm tra link ảnh
            if (avatarUrl != null && !avatarUrl.isEmpty()) {
                if (!avatarUrl.startsWith("http")) {
                    // Nếu là link tương đối (avatars/user6.jpg) -> Nối thêm Base URL
                    avatarUrl = BASE_IMAGE_URL + avatarUrl;
                }

                Glide.with(context)
                        .load(avatarUrl)
                        .placeholder(R.drawable.avatar_placeholder) // Ảnh mặc định
                        .error(R.drawable.avatar_placeholder)
                        .into(holder.imgAvatar);
            } else {
                holder.imgAvatar.setImageResource(R.drawable.avatar_placeholder);
            }
        }
    }

    @Override
    public int getItemCount() {
        return reviewList != null ? reviewList.size() : 0;
    }

    public void updateData(List<Review> newList) {
        this.reviewList = newList;
        notifyDataSetChanged();
    }

    public static class FeedbackViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAvatar;
        TextView tvUsername, tvDate, tvContent;
        RatingBar ratingBar;

        public FeedbackViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.img_user_avatar);
            tvUsername = itemView.findViewById(R.id.tv_username);
            tvDate = itemView.findViewById(R.id.tv_feedback_date);
            tvContent = itemView.findViewById(R.id.tv_feedback_content);
            ratingBar = itemView.findViewById(R.id.rb_feedback_rating);
        }
    }
}