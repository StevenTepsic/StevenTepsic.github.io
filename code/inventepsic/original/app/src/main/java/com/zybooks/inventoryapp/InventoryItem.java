package com.zybooks.inventoryapp;

//inventory item class
public class InventoryItem {

    private final long id;
    private final String sku;
    private final String description;
    private int quantity;
    private final String location;

    public InventoryItem(long id, String sku, String description, int quantity, String location) {
        this.id = id;
        this.sku = sku;
        this.description = description;
        this.quantity = quantity;
        this.location = location;
    }

    public long getId() {
        return id;
    }

    public String getSku() {
        return sku;
    }

    public String getDescription() {
        return description;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getLocation() {
        return location;
    }
}