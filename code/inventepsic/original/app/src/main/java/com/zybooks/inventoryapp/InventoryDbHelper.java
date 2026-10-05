package com.zybooks.inventoryapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

//SQLite CRUD
public class InventoryDbHelper extends SQLiteOpenHelper {

    private static final String TAG = "InventoryDbHelper";

    // increase db version on change
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "InvenTepsic.db";

    // SQL create inventory table
    private static final String SQL_CREATE_INVENTORY_TABLE =
            "CREATE TABLE " + DatabaseContract.InventoryEntry.TABLE_NAME + " (" +
                    DatabaseContract.InventoryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    DatabaseContract.InventoryEntry.COLUMN_SKU + " TEXT NOT NULL, " +
                    DatabaseContract.InventoryEntry.COLUMN_DESCRIPTION + " TEXT NOT NULL, " +
                    DatabaseContract.InventoryEntry.COLUMN_QUANTITY + " INTEGER NOT NULL, " +
                    DatabaseContract.InventoryEntry.COLUMN_LOCATION + " TEXT)";

    // SQL create users
    private static final String SQL_CREATE_USERS_TABLE =
            "CREATE TABLE " + DatabaseContract.UserEntry.TABLE_NAME + " (" +
                    DatabaseContract.UserEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    DatabaseContract.UserEntry.COLUMN_USERNAME + " TEXT UNIQUE NOT NULL, " +
                    DatabaseContract.UserEntry.COLUMN_PASSWORD + " TEXT NOT NULL)";

    private static final String SQL_DELETE_INVENTORY_TABLE =
            "DROP TABLE IF EXISTS " + DatabaseContract.InventoryEntry.TABLE_NAME;

    private static final String SQL_DELETE_USERS_TABLE =
            "DROP TABLE IF EXISTS " + DatabaseContract.UserEntry.TABLE_NAME;

    public InventoryDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_INVENTORY_TABLE);
        db.execSQL(SQL_CREATE_USERS_TABLE);
        Log.d(TAG, "Database tables created successfully");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // discard old data and recreate tables.
        db.execSQL(SQL_DELETE_INVENTORY_TABLE);
        db.execSQL(SQL_DELETE_USERS_TABLE);
        onCreate(db);
    }

    //checks user credentials
    public boolean checkCredentials(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();

        String[] columns = { DatabaseContract.UserEntry._ID };
        String selection = DatabaseContract.UserEntry.COLUMN_USERNAME + " = ? AND " +
                DatabaseContract.UserEntry.COLUMN_PASSWORD + " = ?";
        String[] selectionArgs = { username, password };

        Cursor cursor = db.query(
                DatabaseContract.UserEntry.TABLE_NAME,
                columns,
                selection,
                selectionArgs,
                null, null, null
        );

        boolean credentialsMatch = cursor.getCount() > 0;
        cursor.close();
        return credentialsMatch;
    }

    //check if username exists
    public boolean usernameExists(String username) {
        SQLiteDatabase db = this.getReadableDatabase();

        String[] columns = { DatabaseContract.UserEntry._ID };
        String selection = DatabaseContract.UserEntry.COLUMN_USERNAME + " = ?";
        String[] selectionArgs = { username };

        Cursor cursor = db.query(
                DatabaseContract.UserEntry.TABLE_NAME,
                columns,
                selection,
                selectionArgs,
                null, null, null
        );

        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    //insert new user record
    public long createUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DatabaseContract.UserEntry.COLUMN_USERNAME, username);
        values.put(DatabaseContract.UserEntry.COLUMN_PASSWORD, password);

        return db.insert(DatabaseContract.UserEntry.TABLE_NAME, null, values);
    }


    //Create inventory db
    public long addInventoryItem(String sku, String description, int quantity, String location) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DatabaseContract.InventoryEntry.COLUMN_SKU, sku);
        values.put(DatabaseContract.InventoryEntry.COLUMN_DESCRIPTION, description);
        values.put(DatabaseContract.InventoryEntry.COLUMN_QUANTITY, quantity);
        values.put(DatabaseContract.InventoryEntry.COLUMN_LOCATION, location);

        long newRowId = db.insert(DatabaseContract.InventoryEntry.TABLE_NAME, null, values);
        Log.d(TAG, "Inserted item with SKU " + sku + " at row " + newRowId);
        return newRowId;
    }

    //Read inventory db
    public List<InventoryItem> getAllInventoryItems() {
        List<InventoryItem> itemList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String[] columns = {
                DatabaseContract.InventoryEntry._ID,
                DatabaseContract.InventoryEntry.COLUMN_SKU,
                DatabaseContract.InventoryEntry.COLUMN_DESCRIPTION,
                DatabaseContract.InventoryEntry.COLUMN_QUANTITY,
                DatabaseContract.InventoryEntry.COLUMN_LOCATION
        };

        // Sort by SKU
        String sortOrder = DatabaseContract.InventoryEntry.COLUMN_SKU + " ASC";

        Cursor cursor = db.query(
                DatabaseContract.InventoryEntry.TABLE_NAME,
                columns,
                null, null, null, null,
                sortOrder
        );

        while (cursor.moveToNext()) {
            long id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseContract.InventoryEntry._ID));
            String sku = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.InventoryEntry.COLUMN_SKU));
            String description = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.InventoryEntry.COLUMN_DESCRIPTION));
            int quantity = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.InventoryEntry.COLUMN_QUANTITY));
            String location = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.InventoryEntry.COLUMN_LOCATION));

            itemList.add(new InventoryItem(id, sku, description, quantity, location));
        }
        cursor.close();
        return itemList;
    }

    //Update inventory
    public int updateItemQuantity(long itemId, int newQuantity) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DatabaseContract.InventoryEntry.COLUMN_QUANTITY, newQuantity);

        String selection = DatabaseContract.InventoryEntry._ID + " = ?";
        String[] selectionArgs = { String.valueOf(itemId) };

        return db.update(DatabaseContract.InventoryEntry.TABLE_NAME, values, selection, selectionArgs);
    }

    public int updateInventoryItem(long itemId, String sku, String description, int quantity, String location) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DatabaseContract.InventoryEntry.COLUMN_SKU, sku);
        values.put(DatabaseContract.InventoryEntry.COLUMN_DESCRIPTION, description);
        values.put(DatabaseContract.InventoryEntry.COLUMN_QUANTITY, quantity);
        values.put(DatabaseContract.InventoryEntry.COLUMN_LOCATION, location);

        String selection = DatabaseContract.InventoryEntry._ID + " = ?";
        String[] selectionArgs = { String.valueOf(itemId) };

        return db.update(DatabaseContract.InventoryEntry.TABLE_NAME, values, selection, selectionArgs);
    }

    //Delete Inventory
    public int deleteInventoryItem(long itemId) {
        SQLiteDatabase db = this.getWritableDatabase();

        String selection = DatabaseContract.InventoryEntry._ID + " = ?";
        String[] selectionArgs = { String.valueOf(itemId) };

        int rowsDeleted = db.delete(DatabaseContract.InventoryEntry.TABLE_NAME, selection, selectionArgs);
        Log.d(TAG, "Deleted " + rowsDeleted + " row(s) with ID " + itemId);
        return rowsDeleted;
    }

    //Returns items with zero quantity
    public List<InventoryItem> getOutOfStockItems() {
        List<InventoryItem> outOfStock = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String[] columns = {
                DatabaseContract.InventoryEntry._ID,
                DatabaseContract.InventoryEntry.COLUMN_SKU,
                DatabaseContract.InventoryEntry.COLUMN_DESCRIPTION,
                DatabaseContract.InventoryEntry.COLUMN_QUANTITY,
                DatabaseContract.InventoryEntry.COLUMN_LOCATION
        };
        String selection = DatabaseContract.InventoryEntry.COLUMN_QUANTITY + " = ?";
        String[] selectionArgs = { "0" };

        Cursor cursor = db.query(
                DatabaseContract.InventoryEntry.TABLE_NAME,
                columns, selection, selectionArgs,
                null, null, null
        );

        while (cursor.moveToNext()) {
            long id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseContract.InventoryEntry._ID));
            String sku = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.InventoryEntry.COLUMN_SKU));
            String description = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.InventoryEntry.COLUMN_DESCRIPTION));
            int quantity = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.InventoryEntry.COLUMN_QUANTITY));
            String location = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.InventoryEntry.COLUMN_LOCATION));

            outOfStock.add(new InventoryItem(id, sku, description, quantity, location));
        }
        cursor.close();
        return outOfStock;
    }
}