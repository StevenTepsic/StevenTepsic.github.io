package com.zybooks.inventoryapp;

import static org.junit.Assert.*;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;


// Automated tests for authentication and database operations.
// These run on an emulator or device, because SQLite needs a real Android Context.
@RunWith(AndroidJUnit4.class)
public class InventoryDbHelperTest {

    private static final String TEST_DB = "test_inventepsic.db";

    private Context context;
    private InventoryDbHelper db;

    @Before
    public void setUp() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        context.deleteDatabase(TEST_DB);                 // start every test with an empty database
        db = new InventoryDbHelper(context, TEST_DB);    // second constructor, see Step 2
    }

    @After
    public void tearDown() {
        db.close();
        context.deleteDatabase(TEST_DB);
    }

    // ---------- Authentication ----------

    @Test
    public void createUser_thenLoginWithCorrectPassword_succeeds() {
        assertTrue(db.createUser("steven", "Passw0rd!") != -1);
        assertTrue(db.checkCredentials("steven", "Passw0rd!"));
    }

    @Test
    public void login_withWrongPassword_fails() {
        db.createUser("steven", "Passw0rd!");
        assertFalse(db.checkCredentials("steven", "wrong"));
    }

    @Test
    public void login_isCaseSensitiveOnPassword() {
        db.createUser("steven", "Passw0rd!");
        assertFalse(db.checkCredentials("steven", "passw0rd!"));
    }

    @Test
    public void login_withUnknownUser_fails() {
        assertFalse(db.checkCredentials("nobody", "Passw0rd!"));
    }

    @Test
    public void login_worksRepeatedly() {
        // The stored hash must be stable: hashing the same password with the same salt
        // has to give the same answer every time.
        db.createUser("steven", "Passw0rd!");
        assertTrue(db.checkCredentials("steven", "Passw0rd!"));
        assertTrue(db.checkCredentials("steven", "Passw0rd!"));
    }

    @Test
    public void usernameExists_reflectsCreatedUsers() {
        assertFalse(db.usernameExists("steven"));
        db.createUser("steven", "Passw0rd!");
        assertTrue(db.usernameExists("steven"));
    }

    @Test
    public void createUser_duplicateUsername_isRejected() {
        assertTrue(db.createUser("steven", "Passw0rd!") != -1);
        assertEquals(-1, db.createUser("steven", "different"));
    }

    @Test
    public void login_sqlInjectionAttempt_fails() {
        db.createUser("steven", "Passw0rd!");
        assertFalse(db.checkCredentials("steven' --", "anything"));
        assertFalse(db.checkCredentials("' OR '1'='1", "' OR '1'='1"));
    }

    // ---------- Password storage (Outcome 5) ----------
    // These read the raw users table. On the original plaintext version they would fail.

    @Test
    public void storedUserRow_neverContainsThePlaintextPassword() {
        String password = "Passw0rd!";
        db.createUser("steven", password);

        for (String value : storedValuesFor("steven")) {
            assertNotEquals("Plaintext password found in the users table", password, value);
            assertFalse("Plaintext password is embedded in a stored value", value.contains(password));
        }
    }

    @Test
    public void passwordHash_andSalt_areStored() {
        db.createUser("steven", "Passw0rd!");
        SQLiteDatabase raw = db.getReadableDatabase();
        Cursor c = raw.query(DatabaseContract.UserEntry.TABLE_NAME,
                new String[]{ DatabaseContract.UserEntry.COLUMN_PASSWORD_HASH,
                        DatabaseContract.UserEntry.COLUMN_SALT },
                DatabaseContract.UserEntry.COLUMN_USERNAME + " = ?",
                new String[]{ "steven" }, null, null, null);
        try {
            assertTrue(c.moveToFirst());
            String hash = c.getString(0);
            String salt = c.getString(1);
            assertNotNull(hash);
            assertNotNull(salt);
            assertFalse(hash.isEmpty());
            assertEquals("Salt is 16 random bytes, Base64 encoded", 24, salt.length());
        } finally {
            c.close();
        }
    }

    @Test
    public void samePassword_twoUsers_getDifferentSaltsAndHashes() {
        db.createUser("alice", "Passw0rd!");
        db.createUser("bob", "Passw0rd!");

        // With a random salt per user, identical passwords must not look identical in the table.
        assertNotEquals(storedValuesFor("alice"), storedValuesFor("bob"));
    }

    // Reads every column of a user's row except _id and username.
    private List<String> storedValuesFor(String username) {
        List<String> values = new java.util.ArrayList<>();
        SQLiteDatabase raw = db.getReadableDatabase();
        Cursor c = raw.rawQuery("SELECT * FROM users WHERE username = ?", new String[]{ username });
        try {
            assertTrue("User row not found", c.moveToFirst());
            for (int i = 0; i < c.getColumnCount(); i++) {
                String name = c.getColumnName(i);
                if (name.equals("_id") || name.equals("username")) continue;
                values.add(String.valueOf(c.getString(i)));
            }
        } finally {
            c.close();
        }
        return values;
    }

    // ---------- Inventory CRUD ----------

    @Test
    public void addInventoryItem_savesAndReadsBack() {
        long id = db.addInventoryItem("SKU-001", "Widget", 5, "Aisle 3", "012345678905");
        assertTrue(id > 0);

        List<InventoryItem> items = db.getAllInventoryItems();
        assertEquals(1, items.size());
        assertEquals("SKU-001", items.get(0).getSku());
        assertEquals("Widget", items.get(0).getDescription());
        assertEquals(5, items.get(0).getQuantity());
        assertEquals("Aisle 3", items.get(0).getLocation());
        assertEquals("012345678905", items.get(0).getUPC());
    }

    @Test
    public void getAllInventoryItems_isSortedBySku() {
        db.addInventoryItem("C-300", "Third", 1, "x", "3");
        db.addInventoryItem("A-100", "First", 1, "x", "1");
        db.addInventoryItem("B-200", "Second", 1, "x", "2");

        List<InventoryItem> items = db.getAllInventoryItems();
        assertEquals("A-100", items.get(0).getSku());
        assertEquals("B-200", items.get(1).getSku());
        assertEquals("C-300", items.get(2).getSku());
    }

    @Test
    public void updateItemQuantity_changesOnlyThatItem() {
        long a = db.addInventoryItem("A", "Alpha", 5, "x", "1");
        long b = db.addInventoryItem("B", "Beta", 5, "x", "2");

        assertEquals(1, db.updateItemQuantity(a, 9));

        for (InventoryItem item : db.getAllInventoryItems()) {
            if (item.getId() == a) assertEquals(9, item.getQuantity());
            if (item.getId() == b) assertEquals(5, item.getQuantity());
        }
    }

    @Test
    public void updateInventoryItem_changesAllFields_includingUpc() {
        long id = db.addInventoryItem("A", "Alpha", 5, "x", "111");
        assertEquals(1, db.updateInventoryItem(id, "A2", "Alpha 2", 7, "y", "222"));

        InventoryItem item = db.getAllInventoryItems().get(0);
        assertEquals("A2", item.getSku());
        assertEquals("Alpha 2", item.getDescription());
        assertEquals(7, item.getQuantity());
        assertEquals("y", item.getLocation());
        assertEquals("222", item.getUPC());
    }

    @Test
    public void deleteInventoryItem_removesRow() {
        long id = db.addInventoryItem("A", "Alpha", 5, "x", "1");
        assertEquals(1, db.deleteInventoryItem(id));
        assertTrue(db.getAllInventoryItems().isEmpty());
    }

    @Test
    public void deleteInventoryItem_missingId_deletesNothing() {
        db.addInventoryItem("A", "Alpha", 5, "x", "1");
        assertEquals(0, db.deleteInventoryItem(9999));
        assertEquals(1, db.getAllInventoryItems().size());
    }

    @Test
    public void getOutOfStockItems_returnsOnlyZeroQuantity() {
        db.addInventoryItem("A", "In stock", 3, "x", "1");
        db.addInventoryItem("B", "Empty", 0, "x", "2");

        List<InventoryItem> out = db.getOutOfStockItems();
        assertEquals(1, out.size());
        assertEquals("B", out.get(0).getSku());
        assertEquals("2", out.get(0).getUPC());
    }

    // ---------- UPC ----------

    @Test
    public void addInventoryItem_withoutUpc_isRejected() {
        // The UPC column is NOT NULL, so a null UPC must fail instead of saving a bad row.
        assertEquals(-1, db.addInventoryItem("A", "Alpha", 5, "x", null));
        assertTrue(db.getAllInventoryItems().isEmpty());
    }
}

