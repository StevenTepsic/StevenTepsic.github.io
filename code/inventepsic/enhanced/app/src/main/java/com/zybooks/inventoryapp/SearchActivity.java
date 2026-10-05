package com.zybooks.inventoryapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.util.Log;
import android.view.View;

import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.common.InputImage;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


//lets user filter search pulling from the list
public class SearchActivity extends AppCompatActivity {

    private InventoryDbHelper dbHelper;
    private List<InventoryItem> allItems;
    private InventoryAdapter adapter;
    private List<InventoryItem> filteredItems;

    private RecyclerView rvSearchResults;
    private View tvSearchHint;
    private MaterialButton btnScanSearch;
    private PreviewView previewView;
    private static final String TAG = "SearchActivity";

    private ProcessCameraProvider cameraProvider;
    private ExecutorService cameraExecutor;
    private boolean barcodeHandled = false;
    private TextInputEditText etSearch;

    private final ActivityResultLauncher<String> cameraPermissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    isGranted -> {
                        if (isGranted) {
                            onCameraPermissionGranted();
                        } else {
                            onCameraPermissionDenied();
                        }
                    });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        dbHelper = new InventoryDbHelper(this);
        allItems = dbHelper.getAllInventoryItems();
        filteredItems = new ArrayList<>();

        rvSearchResults = findViewById(R.id.rvSearchResults);
        tvSearchHint = findViewById(R.id.tvSearchHint);
        btnScanSearch = findViewById(R.id.btnScanSearch);
        previewView = findViewById(R.id.previewView);


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

        btnScanSearch.setOnClickListener(v -> requestCameraPermission());
        checkExistingPermissionState();

    }

    private void setUpSearchInput() {
        etSearch = findViewById(R.id.etSearch);
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

    private void checkExistingPermissionState() {
        boolean alreadyGranted = ContextCompat.checkSelfPermission(
                this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;

        if (alreadyGranted) {
            onCameraPermissionGranted();
        }
    }

    private void requestCameraPermission() {
        cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
    }

    private void onCameraPermissionGranted() {
        previewView.setVisibility(View.VISIBLE);
        barcodeHandled = false;
        cameraExecutor = Executors.newSingleThreadExecutor();

        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                bindCameraUseCases();
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Failed to start camera", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void onCameraPermissionDenied() {
        // no-op for now - the screen already works for manual entry
    }

    @OptIn(markerClass = ExperimentalGetImage.class)
    private void bindCameraUseCases() {
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        BarcodeScanner scanner = BarcodeScanning.getClient();
        imageAnalysis.setAnalyzer(cameraExecutor, imageProxy -> processImageProxy(scanner, imageProxy));

        cameraProvider.unbindAll();
        cameraProvider.bindToLifecycle(
                this, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageAnalysis);
    }

    @ExperimentalGetImage
    private void processImageProxy(BarcodeScanner scanner, ImageProxy imageProxy) {
        if (imageProxy.getImage() == null) {
            imageProxy.close();
            return;
        }

        InputImage image = InputImage.fromMediaImage(
                imageProxy.getImage(), imageProxy.getImageInfo().getRotationDegrees());

        scanner.process(image)
                .addOnSuccessListener(barcodes -> {
                    if (!barcodeHandled && !barcodes.isEmpty()) {
                        String rawValue = barcodes.get(0).getRawValue();
                        if (rawValue != null) {
                            barcodeHandled = true;
                            runOnUiThread(() -> {
                                onBarcodeScanned(rawValue);
                                stopScanning();
                            });
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Barcode scan failed", e))
                .addOnCompleteListener(task -> imageProxy.close());
    }

    private void stopScanning() {
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
        }
        if (cameraExecutor != null) {
            cameraExecutor.shutdown();
        }
        previewView.setVisibility(View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraExecutor != null) {
            cameraExecutor.shutdown();
        }
    }

    private void onBarcodeScanned(String upc) {
        etSearch.setText(upc);
        filterResults(upc);
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