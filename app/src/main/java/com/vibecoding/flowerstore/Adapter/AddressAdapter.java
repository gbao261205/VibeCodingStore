package com.vibecoding.flowerstore.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.vibecoding.flowerstore.Model.AddressDTO;
import com.vibecoding.flowerstore.R;

import java.util.List;

public class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.AddressViewHolder> {

    private Context context;
    private List<AddressDTO> addressList;

    // Interface để xử lý sự kiện click nút Sửa/Xóa (nếu cần xử lý ở Activity)
    public interface OnAddressActionListener {
        void onEdit(AddressDTO address);
        void onDelete(AddressDTO address);
    }

    private OnAddressActionListener listener;

    public AddressAdapter(Context context, List<AddressDTO> addressList, OnAddressActionListener listener) {
        this.context = context;
        this.addressList = addressList;
        this.listener = listener;
    }

    public void setAddressList(List<AddressDTO> addressList) {
        this.addressList = addressList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AddressViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_address, parent, false);
        return new AddressViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AddressViewHolder holder, int position) {
        AddressDTO address = addressList.get(position);

        holder.tvName.setText(address.getRecipientName());
        holder.tvPhone.setText(address.getPhoneNumber());
        holder.tvAddress.setText(address.getFullAddress());

        // Xử lý hiển thị tag "Mặc định"
        if (address.isDefault()) {
            holder.tvDefaultTag.setVisibility(View.VISIBLE);
        } else {
            holder.tvDefaultTag.setVisibility(View.GONE);
        }

        // Sự kiện click Sửa
        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEdit(address);
        });

        // Sự kiện click Xóa
        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(address);
        });
    }

    @Override
    public int getItemCount() {
        return addressList == null ? 0 : addressList.size();
    }

    public static class AddressViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPhone, tvAddress, tvDefaultTag, btnEdit, btnDelete;

        public AddressViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvPhone = itemView.findViewById(R.id.tvPhone); // Đảm bảo đã thêm ID này trong XML
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvDefaultTag = itemView.findViewById(R.id.tvDefaultTag);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}