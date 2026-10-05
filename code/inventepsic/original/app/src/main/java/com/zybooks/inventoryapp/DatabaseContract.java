package com.zybooks.inventoryapp;

import android.provider.BaseColumns;

//table and column names for the inventory database
public final class DatabaseContract {

    private DatabaseContract() {}

    // inner class representing the inventory table
    public static final class InventoryEntry implements BaseColumns {
        public static final String TABLE_NAME = "inventory";
        public static final String COLUMN_SKU = "sku";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_QUANTITY = "quantity";
        public static final String COLUMN_LOCATION = "location";
    }

    //login info
    public static final class UserEntry implements BaseColumns {
        public static final String TABLE_NAME = "users";
        public static final String COLUMN_USERNAME = "username";
        public static final String COLUMN_PASSWORD = "password";
    }
}