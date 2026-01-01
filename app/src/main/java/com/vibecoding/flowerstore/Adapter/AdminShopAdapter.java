package com.vibecoding.flowerstore.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.vibecoding.flowerstore.Model.ShopDTO;
import com.vibecoding.flowerstore.R;

import java.util.List;

public class AdminShopAdapter extends RecyclerView.Adapter<AdminShopAdapter.ViewHolder> {

    private Context context;
    private List<ShopDTO> shopList;

    public AdminShopAdapter(Context context, List<ShopDTO> shopList) {
        this.context = context;
        this.shopList = shopList;
    }

    public void setShopList(List<ShopDTO> shopList) {
        this.shopList = shopList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_shop_admin, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ShopDTO shop = shopList.get(position);

        holder.tvShopName.setText(shop.getName());
        
        // Response ShopDTO hiện tại chỉ có id, name, active
        // Không có address, user, imageUrl -> Ẩn hoặc set mặc định
        
        holder.tvShopAddress.setVisibility(View.GONE);
        holder.tvShopOwner.setVisibility(View.GONE);

        // Trạng thái
        if (shop.isActive()) {
            holder.tvStatus.setText("Đang hoạt động");
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_green);
            holder.tvStatus.setTextColor(Color.parseColor("#166534"));
        } else {
            holder.tvStatus.setText("Ngừng hoạt động"); 
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_gray);
            holder.tvStatus.setTextColor(Color.parseColor("#374151"));
        }

        // Logo mặc định vì không có imageUrl
        holder.imgShopLogo.setImageResource(R.drawable.ic_store);
    }

    @Override
    public int getItemCount() {
        return shopList == null ? 0 : shopList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgShopLogo, btnMore;
        TextView tvShopName, tvShopAddress, tvShopOwner, tvStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgShopLogo = itemView.findViewById(R.id.imgShopLogo);
            tvShopName = itemView.findViewById(R.id.tvShopName);
            tvShopAddress = itemView.findViewById(R.id.tvShopAddress);
            tvShopOwner = itemView.findViewById(R.id.tvShopOwner);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnMore = itemView.findViewById(R.id.btnMore);
        }
    }
}
