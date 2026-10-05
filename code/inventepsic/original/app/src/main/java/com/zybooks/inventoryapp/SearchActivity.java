package com.zybooks.inventoryapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

//lets user filter search pulling from the list
public class SearchActivity extends AppCompatActivity {

    private InventoryDbHelper dbHelper;
    private List<InventoryItem> allItems;
    private InventoryAdapter adapter;
    private List<InventoryItem> filteredItems;

    private RecyclerView rvSearchResults;
    private View tvSearchHint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        dbHelper = new InventoryDbHelper(this);
        allItems = dbHelper.getAllInventoryItems();
        filteredItems = new ArrayList<>();

        rvSearchResults = findViewById(R.id.rvSearchResults);
        tvSearchHint = findViewById(R.id.tvSearchHint);

        adapter = new InventoryAdapter(filteredItems, new InventoryAdapter.OnItemActionListener() {
            @Override
            public void onEditClicked(InventoryItem item) {
            }

            @Override
            public void onDeleteClicked(InventoryItem item) {
                dbHelper.deleteInventoryItem(item.getId());
                allItems = dbHelper.getAllInventoryItems();
                filterResults(getCurrentQuery());
            }
        });

        rvSearchResults.setLayoutManager(new LinearLayoutManager(this));
        rvSearchResults.setAdapter(adapter);

        setUpSearchInput();
        setUpBottomNav();
    }

    private void setUpSearchInput() {
        TextInputEditText etSearch = findViewById(R.id.etSearch);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterResults(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private String currentQuery = "";

    private String getCurrentQuery() {
        return currentQuery;
    }

    //Filters the in-memory item list by SKU or description
    private void filterResults(String query) {
        currentQuery = query;
        filteredItems.clear();

        if (query.trim().isEmpty()) {
            tvSearchHint.setVisibility(View.VISIBLE);
            rvSearchResults.setVisibility(View.GONE);
            adapter.notifyDataSetChanged();
            return;
        }

        String lowerCaseQuery = query.toLowerCase(Locale.getDefault());
        for (InventoryItem item : allItems) {
            boolean matchesSku = item.getSku().toLowerCase(Locale.getDefault()).contains(lowerCaseQuery);
            boolean matchesDescription = item.getDescription().toLowerCase(Locale.getDefault()).contains(lowerCaseQuery);
            if (matchesSku || matchesDescription) {
                filteredItems.add(item);
            }
        }

        tvSearchHint.setVisibility(View.GONE);
        rvSearchResults.setVisibility(View.VISIBLE);
        adapter.notifyDataSetChanged();
    }

    private void setUpBottomNav() {
        BottomNavigationView navBar = findViewById(R.id.bottomNav);
        navBar.setSelectedItemId(R.id.nav_search);

        navBar.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_search) {
                return true;
            } else if (itemId == R.id.nav_database) {
                startActivity(new Intent(this, DatabaseActivity.class));
                return true;
            } else if (itemId == R.id.nav_notifications) {
                startActivity(new Intent(this, NotificationsActivity.class));
                return true;
            } else if (itemId == R.id.nav_logout) {
                Intent intent = new Intent(this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                return true;
            }
            return false;
        });
    }
}