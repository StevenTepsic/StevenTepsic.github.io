package com.zybooks.inventoryapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.List;

// displays grid and handle db functions/nav
public class DatabaseActivity extends AppCompatActivity implements InventoryAdapter.OnItemActionListener {

    private InventoryDbHelper dbHelper;
    private InventoryAdapter adapter;
    private List<InventoryItem> itemList;

    private RecyclerView rvInventory;
    private View layoutEmpty;
    private TextView tvTotalItems;
    private TextView tvLowStock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_database);

        dbHelper = new InventoryDbHelper(this);

        rvInventory = findViewById(R.id.rvInventory);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        tvTotalItems = findViewById(R.id.tvTotalItems);
        tvLowStock = findViewById(R.id.tvLowStock);

        setUpRecyclerView();
        setUpFab();
        setUpBottomNav();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // refresh when return to screen
        refreshInventoryList();
    }

    private void setUpRecyclerView() {
        itemList = dbHelper.getAllInventoryItems();
        adapter = new InventoryAdapter(itemList, this);
        rvInventory.setLayoutManager(new LinearLayoutManager(this));
        rvInventory.setAdapter(adapter);
    }

    private void setUpFab() {
        ExtendedFloatingActionButton fab = findViewById(R.id.fabAddItem);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(DatabaseActivity.this, AddItemActivity.class);
            startActivity(intent);
        });
    }

    private void setUpBottomNav() {
        BottomNavigationView navBar = findViewById(R.id.navBar);
        navBar.setSelectedItemId(R.id.nav_database);

        navBar.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_database) {
                return true; // already here
            } else if (itemId == R.id.nav_notifications) {
                startActivity(new Intent(this, NotificationsActivity.class));
                return true;
            } else if (itemId == R.id.nav_search) {
                startActivity(new Intent(this, SearchActivity.class));
                return true;
            } else if (itemId == R.id.nav_logout) {
                logOut();
                return true;
            }
            return false;
        });
    }

    private void logOut() {
        Intent intent = new Intent(DatabaseActivity.this, LoginActivity.class);
        // Clear the back stack
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    //Reloads the inventory list
    private void refreshInventoryList() {
        itemList.clear();
        itemList.addAll(dbHelper.getAllInventoryItems());
        adapter.notifyDataSetChanged();

        updateSummaryCounts();
        toggleEmptyState();
    }

    private void updateSummaryCounts() {
        int totalItems = itemList.size();
        int lowStockCount = 0;
        for (InventoryItem item : itemList) {
            if (item.getQuantity() == 0) {
                lowStockCount++;
            }
        }
        tvTotalItems.setText("Total Items: " + totalItems);
        tvLowStock.setText("Low Stock: " + lowStockCount);
    }

    private void toggleEmptyState() {
        if (itemList.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            rvInventory.setVisibility(View.GONE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            rvInventory.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onEditClicked(InventoryItem item) {
        showEditQuantityDialog(item);
    }

    @Override
    public void onDeleteClicked(InventoryItem item) {
        showDeleteConfirmationDialog(item);
    }

    //edit dialogue
    private void showEditQuantityDialog(InventoryItem item) {
        final android.widget.EditText input = new android.widget.EditText(this);
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        input.setText(String.valueOf(item.getQuantity()));

        new AlertDialog.Builder(this)
                .setTitle("Update Quantity")
                .setMessage(item.getSku() + " — " + item.getDescription())
                .setView(input)
                .setPositiveButton("Save", (dialog, which) -> {
                    String enteredValue = input.getText().toString().trim();
                    if (enteredValue.isEmpty()) {
                        Toast.makeText(this, "Quantity cannot be empty", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    int newQuantity = Integer.parseInt(enteredValue);
                    dbHelper.updateItemQuantity(item.getId(), newQuantity);

                    // If this edit just dropped the item to zero stock, fire an
                    // SMS alert immediately (no-ops silently if permission was
                    // never granted, so this is always safe to call).
                    if (newQuantity == 0) {
                        NotificationsActivity.sendOutOfStockSms(this, item);
                    }

                    refreshInventoryList();
                    Toast.makeText(this, "Quantity updated", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showDeleteConfirmationDialog(InventoryItem item) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Item")
                .setMessage("Remove " + item.getSku() + " from inventory?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    dbHelper.deleteInventoryItem(item.getId());
                    refreshInventoryList();
                    Toast.makeText(this, "Item deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}