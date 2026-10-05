package com.zybooks.inventoryapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.button.MaterialButton;

//Login Process
public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etUsername;
    private TextInputEditText etPassword;
    private MaterialButton btnLogin;
    private MaterialButton btnCreateAccount;

    private InventoryDbHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        dbHelper = new InventoryDbHelper(this);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnCreateAccount = findViewById(R.id.btnCreateAccount);

        btnLogin.setOnClickListener(v -> attemptLogin());
        btnCreateAccount.setOnClickListener(v -> attemptCreateAccount());
    }

    //validate input and checks against table
    private void attemptLogin() {
        String username = getTrimmedText(etUsername);
        String password = getTrimmedText(etPassword);

        if (!inputsAreValid(username, password)) {
            return;
        }

        boolean credentialsValid = dbHelper.checkCredentials(username, password);

        if (credentialsValid) {
            Toast.makeText(this, "Welcome back, " + username, Toast.LENGTH_SHORT).show();
            navigateToDatabaseScreen();
        } else {
            Toast.makeText(this, "Incorrect username or password", Toast.LENGTH_SHORT).show();
        }
    }

    //validates unique username before creation
    private void attemptCreateAccount() {
        String username = getTrimmedText(etUsername);
        String password = getTrimmedText(etPassword);

        if (!inputsAreValid(username, password)) {
            return;
        }

        if (dbHelper.usernameExists(username)) {
            Toast.makeText(this, "That username is already taken", Toast.LENGTH_SHORT).show();
            return;
        }

        long newUserId = dbHelper.createUser(username, password);

        if (newUserId != -1) {
            Toast.makeText(this, "Account created. Welcome, " + username, Toast.LENGTH_SHORT).show();
            navigateToDatabaseScreen();
        } else {
            Toast.makeText(this, "Account creation failed. Please try again", Toast.LENGTH_SHORT).show();
        }
    }

    //validation
    private boolean inputsAreValid(String username, String password) {
        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Username and password are required", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private String getTrimmedText(TextInputEditText editText) {
        return editText.getText() != null ? editText.getText().toString().trim() : "";
    }

    private void navigateToDatabaseScreen() {
        Intent intent = new Intent(LoginActivity.this, DatabaseActivity.class);
        startActivity(intent);
        finish(); // prevents the user from navigating back to the login screen
    }
}