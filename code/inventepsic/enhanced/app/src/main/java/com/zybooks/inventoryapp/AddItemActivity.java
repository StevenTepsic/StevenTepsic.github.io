package com.zybooks.inventoryapp;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;
import android.Manifest;
import android.content.pm.PackageManager;


import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;


import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

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

//collects input for new items
public class AddItemActivity extends AppCompatActivity {

    private TextInputEditText etSku;
    private TextInputEditText etDescription;
    private TextInputEditText etQuantity;
    private TextInputEditText etLocation;

    private TextInputEditText etUPC;

    private MaterialButton btnScan;
    private PreviewView previewView;
    private static final String TAG = "AddItemActivity";

    private ProcessCameraProvider cameraProvider;
    private ExecutorService cameraExecutor;
    private boolean barcodeHandled = false;


    private InventoryDbHelper dbHelper;

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
        setContentView(R.layout.activity_add_item);

        dbHelper = new InventoryDbHelper(this);

        etSku = findViewById(R.id.etSku);
        etDescription = findViewById(R.id.etDescription);
        etQuantity = findViewById(R.id.etQuantity);
        etLocation = findViewById(R.id.etLocation);
        etUPC = findViewById(R.id.etUPC);

        MaterialButton btnSave = findViewById(R.id.btnSaveItem);
        MaterialButton btnCancel = findViewById(R.id.btnCancelAdd);
        btnScan = findViewById(R.id.btnScanUPC);
        previewView = findViewById(R.id.previewView);
        MaterialToolbar toolbar = findViewById(R.id.toolbarAddItem);

        btnSave.setOnClickListener(v -> saveItem());
        btnCancel.setOnClickListener(v -> finish());
        toolbar.setNavigationOnClickListener(v -> finish());
        btnScan.setOnClickListener(v -> requestCameraPermission());

        checkExistingPermissionState();
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
                                etUPC.setText(rawValue);
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


    private void saveItem() {
        String sku = getTrimmedText(etSku);
        String description = getTrimmedText(etDescription);
        String quantityText = getTrimmedText(etQuantity);
        String location = getTrimmedText(etLocation);
        String UPC = getTrimmedText(etUPC);

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

        long newRowId = dbHelper.addInventoryItem(sku, description, quantity, location, UPC);

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