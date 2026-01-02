package com.vibecoding.flowerstore.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.vibecoding.flowerstore.Model.UserDTO;
import com.vibecoding.flowerstore.R;

import java.util.List;

public class AdminUserAdapter extends RecyclerView.Adapter<AdminUserAdapter.ViewHolder> {

    private Context context;
    private List<UserDTO> userList;

    public AdminUserAdapter(Context context, List<UserDTO> userList) {
        this.context = context;
        this.userList = userList;
    }

    public void setUserList(List<UserDTO> userList) {
        this.userList = userList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user_admin, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserDTO user = userList.get(position);

        if (user.getFullName() != null && !user.getFullName().isEmpty()) {
            holder.tvUserName.setText(user.getFullName());
        } else {
            holder.tvUserName.setText(user.getUsername());
        }
        
        // Sửa lỗi hiển thị email
        String email = user.getEmail();
        if (email != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                holder.tvUserEmail.setText(Html.fromHtml(email, Html.FROM_HTML_MODE_LEGACY));
            } else {
                holder.tvUserEmail.setText(Html.fromHtml(email));
            }
        } else {
            holder.tvUserEmail.setText("");
        }

        // Role
        if (user.getRole() != null) {
            holder.tvRole.setText(user.getRole().getName());
        } else {
            holder.tvRole.setText("--");
        }

        // Status
        if (user.isActive()) {
            holder.tvStatus.setText("Active");
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_green);
            holder.tvStatus.setTextColor(Color.parseColor("#166534"));
        } else {
            holder.tvStatus.setText("Inactive");
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_gray);
            holder.tvStatus.setTextColor(Color.parseColor("#374151"));
        }

        // Avatar
        String avatarUrl = user.getAvatar(); 
        Glide.with(context)
                .load(avatarUrl)
                .placeholder(R.drawable.ic_person) // Hiển thị khi đang tải
                .error(R.drawable.ic_person)       // Hiển thị khi tải lỗi
                .fallback(R.drawable.ic_person)    // Hiển thị khi url là null
                .into(holder.imgAvatar);
    }

    @Override
    public int getItemCount() {
        return userList == null ? 0 : userList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAvatar, btnMore;
        TextView tvUserName, tvUserEmail, tvRole, tvStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvUserEmail = itemView.findViewById(R.id.tvUserEmail);
            tvRole = itemView.findViewById(R.id.tvRole);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnMore = itemView.findViewById(R.id.btnMore);
        }
    }
}
