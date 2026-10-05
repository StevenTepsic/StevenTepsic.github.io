package com.zybooks.inventoryapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

//manages sms permissions
public class NotificationsActivity extends AppCompatActivity {

    private static final String TAG = "NotificationsActivity";
    private static final String SMS_DESTINATION_NUMBER = "5554";

    private InventoryDbHelper dbHelper;

    private MaterialCardView cardSmsPermission;
    private MaterialCardView cardSmsDenied;
    private MaterialButton btnEnableSms;
    private MaterialButton btnSkipSms;
    private View layoutNoAlerts;
    private RecyclerView rvAlerts;

    private final ActivityResultLauncher<String> smsPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    onPermissionGranted();
                } else {
                    onPermissionDenied();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        dbHelper = new InventoryDbHelper(this);

        cardSmsPermission = findViewById(R.id.cardSmsPermission);
        cardSmsDenied = findViewById(R.id.cardSmsDenied);
        btnEnableSms = findViewById(R.id.btnEnableSms);
        btnSkipSms = findViewById(R.id.btnSkipSms);
        layoutNoAlerts = findViewById(R.id.layoutNoAlerts);
        rvAlerts = findViewById(R.id.rvAlerts);

        btnEnableSms.setOnClickListener(v -> requestSmsPermission());
        btnSkipSms.setOnClickListener(v -> cardSmsPermission.setVisibility(View.GONE));

        setUpBottomNav();
        checkExistingPermissionState();
        loadAlerts();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAlerts();
    }

    private void checkExistingPermissionState() {
        boolean alreadyGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED;

        if (alreadyGranted) {
            cardSmsPermission.setVisibility(View.GONE);
            cardSmsDenied.setVisibility(View.GONE);
        }
    }

    private void requestSmsPermission() {
        smsPermissionLauncher.launch(Manifest.permission.SEND_SMS);
    }

    private void onPermissionGranted() {
        cardSmsPermission.setVisibility(View.GONE);
        cardSmsDenied.setVisibility(View.GONE);
        Toast.makeText(this, "SMS notifications enabled", Toast.LENGTH_SHORT).show();

        List<InventoryItem> outOfStockItems = dbHelper.getOutOfStockItems();
        for (InventoryItem item : outOfStockItems) {
            sendOutOfStockSms(this, item);
        }
        if (!outOfStockItems.isEmpty()) {
            Toast.makeText(this, "Stock alerts sent via SMS", Toast.LENGTH_SHORT).show();
        }
    }

    private void onPermissionDenied() {
        cardSmsPermission.setVisibility(View.GONE);
        cardSmsDenied.setVisibility(View.VISIBLE);
    }

    private void loadAlerts() {
        List<InventoryItem> outOfStockItems = dbHelper.getOutOfStockItems();

        if (outOfStockItems.isEmpty()) {
            layoutNoAlerts.setVisibility(View.VISIBLE);
            rvAlerts.setVisibility(View.GONE);
        } else {
            layoutNoAlerts.setVisibility(View.GONE);
            rvAlerts.setVisibility(View.VISIBLE);
            AlertAdapter adapter = new AlertAdapter(outOfStockItems);
            rvAlerts.setLayoutManager(new LinearLayoutManager(this));
            rvAlerts.setAdapter(adapter);
        }
    }

    //send out of stock notifications
    public static void sendOutOfStockSms(Context context, InventoryItem item) {
        boolean hasPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED;

        if (!hasPermission) {
            Log.d(TAG, "SMS permission not granted; skipping alert for " + item.getSku());
            return;
        }

        SmsManager smsManager = SmsManager.getDefault();
        String message = "Low stock alert: " + item.getDescription() +
                " (SKU: " + item.getSku() + ") is out of stock at " + item.getLocation();

        smsManager.sendTextMessage(SMS_DESTINATION_NUMBER, null, message, null, null);
        Log.d(TAG, "Sent SMS for " + item.getSku());
    }

    private void setUpBottomNav() {
        BottomNavigationView navBar = findViewById(R.id.bottomNav);
        navBar.setSelectedItemId(R.id.nav_notifications);

        navBar.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_notifications) {
                return true;
            } else if (itemId == R.id.nav_database) {
                startActivity(new Intent(this, DatabaseActivity.class));
                return true;
            } else if (itemId == R.id.nav_search) {
                startActivity(new Intent(this, SearchActivity.class));
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