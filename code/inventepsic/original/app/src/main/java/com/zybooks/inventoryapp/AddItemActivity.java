package com.zybooks.inventoryapp;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

//collects input for new items
public class AddItemActivity extends AppCompatActivity {

    private TextInputEditText etSku;
    private TextInputEditText etDescription;
    private TextInputEditText etQuantity;
    private TextInputEditText etLocation;

    private InventoryDbHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        dbHelper = new InventoryDbHelper(this);

        etSku = findViewById(R.id.etSku);
        etDescription = findViewById(R.id.etDescription);
        etQuantity = findViewById(R.id.etQuantity);
        etLocation = findViewById(R.id.etLocation);

        MaterialButton btnSave = findViewById(R.id.btnSaveItem);
        MaterialButton btnCancel = findViewById(R.id.btnCancelAdd);
        MaterialToolbar toolbar = findViewById(R.id.toolbarAddItem);

        btnSave.setOnClickListener(v -> saveItem());
        btnCancel.setOnClickListener(v -> finish());
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void saveItem() {
        String sku = getTrimmedText(etSku);
        String description = getTrimmedText(etDescription);
        String quantityText = getTrimmedText(etQuantity);
        String location = getTrimmedText(etLocation);

        if (TextUtils.isEmpty(sku) || TextUtils.isEmpty(description) || TextUtils.isEmpty(quantityText)) {
            Toast.makeText(this, "SKU, description, and quantity are required", Toast.LENGTH_SHORT).show();
            return;
        }

        int quantity;
        try {
            quantity = Integer.parseInt(quantityText);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Quantity must be a number", Toast.LENGTH_SHORT).show();
            return;
        }

        if (quantity < 0) {
            Toast.makeText(this, "Quantity cannot be negative", Toast.LENGTH_SHORT).show();
            return;
        }

        long newRowId = dbHelper.addInventoryItem(sku, description, quantity, location);

        if (newRowId != -1) {
            Toast.makeText(this, "Item saved", Toast.LENGTH_SHORT).show();
            finish(); // returns to DatabaseActivity
        } else {
            Toast.makeText(this, "Failed to save item", Toast.LENGTH_SHORT).show();
        }
    }

    private String getTrimmedText(TextInputEditText editText) {
        return editText.getText() != null ? editText.getText().toString().trim() : "";
    }
}