package com.zybooks.inventoryapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

// binds out of stock items to notification system
public class AlertAdapter extends RecyclerView.Adapter<AlertAdapter.AlertViewHolder> {

    private final List<InventoryItem> alertList;

    public AlertAdapter(List<InventoryItem> alertList) {
        this.alertList = alertList;
    }

    @NonNull
    @Override
    public AlertViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_alert_row, parent, false);
        return new AlertViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlertViewHolder holder, int position) {
        InventoryItem item = alertList.get(position);
        holder.tvSku.setText(item.getSku());
        holder.tvDescription.setText(item.getDescription());
        holder.tvTimestamp.setText("Out of stock at " + item.getLocation());
    }

    @Override
    public int getItemCount() {
        return alertList.size();
    }

    static class AlertViewHolder extends RecyclerView.ViewHolder {
        TextView tvSku;
        TextView tvDescription;
        TextView tvTimestamp;

        AlertViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSku = itemView.findViewById(R.id.tvAlertSku);
            tvDescription = itemView.findViewById(R.id.tvAlertDescription);
            tvTimestamp = itemView.findViewById(R.id.tvAlertTimestamp);
        }
    }
}