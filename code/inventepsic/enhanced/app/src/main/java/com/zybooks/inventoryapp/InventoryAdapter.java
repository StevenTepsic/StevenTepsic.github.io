package com.zybooks.inventoryapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

//links inventory items to grid screen
public class InventoryAdapter extends RecyclerView.Adapter<InventoryAdapter.InventoryViewHolder> {

    public interface OnItemActionListener {
        void onEditClicked(InventoryItem item);
        void onDeleteClicked(InventoryItem item);
    }

    private final List<InventoryItem> itemList;
    private final OnItemActionListener listener;

    public InventoryAdapter(List<InventoryItem> itemList, OnItemActionListener listener) {
        this.itemList = itemList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public InventoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_inventory_row, parent, false);
        return new InventoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InventoryViewHolder holder, int position) {
        InventoryItem item = itemList.get(position);

        holder.tvSku.setText(item.getSku());
        holder.tvDescription.setText(item.getDescription());
        holder.tvQuantity.setText(String.valueOf(item.getQuantity()));
        holder.tvLocation.setText(item.getLocation());

        //flag zero quantity items
        int quantityColor = item.getQuantity() == 0
                ? holder.itemView.getContext().getColor(R.color.Error)
                : holder.itemView.getContext().getColor(R.color.Success);
        holder.tvQuantity.setTextColor(quantityColor);

        holder.btnEdit.setOnClickListener(v -> listener.onEditClicked(item));
        holder.btnDelete.setOnClickListener(v -> listener.onDeleteClicked(item));
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    //holds views to reduce amount of calls
    static class InventoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvSku;
        TextView tvDescription;
        TextView tvQuantity;
        TextView tvLocation;
        ImageButton btnEdit;
        ImageButton btnDelete;

        InventoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSku = itemView.findViewById(R.id.tvRowSku);
            tvDescription = itemView.findViewById(R.id.tvRowDescription);
            tvQuantity = itemView.findViewById(R.id.tvRowQuantity);
            tvLocation = itemView.findViewById(R.id.tvRowLocation);
            btnEdit = itemView.findViewById(R.id.btnEditRow);
            btnDelete = itemView.findViewById(R.id.btnDeleteRow);
        }
    }
}