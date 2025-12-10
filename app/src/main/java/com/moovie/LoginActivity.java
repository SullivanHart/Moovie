package com.moovie;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private static final String PREFS_NAME = "login_prefs";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PASSWORD = "password";

    private EditText emailInput, passwordInput;
    private Button loginButton;
    private CheckBox rememberMeCheckbox;
    private TextView signupText, resetPasswordText;

    private FirebaseAuth mAuth;

    /**
     * Called when the activity is starting.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down then this Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle).
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.loginButton);
        rememberMeCheckbox = findViewById(R.id.rememberMeCheckbox);
        signupText = findViewById(R.id.signupText);
        resetPasswordText = findViewById(R.id.resetPasswordText);

        // Load saved credentials if "remember me" was checked
        boolean hasSavedCredentials = loadSavedCredentials();

        loginButton.setOnClickListener(v -> attemptLogin());
        signupText.setOnClickListener(v -> startActivity(new Intent(this, SignupActivity.class)));
        resetPasswordText.setOnClickListener(v -> startActivity(new Intent(this, ResetPasswordActivity.class)));

        // Auto-login only if user is authenticated AND "remember me" is on
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null && hasSavedCredentials) {
            navigateToMain();
        }
    }

    private void attemptLogin() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString();

        if (email.isEmpty() || password.isEmpty()) {
            showError("Please enter email and password");
            return;
        }

        clearErrorStyles();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Login successful: " + email);

                        if (rememberMeCheckbox.isChecked()) {
                            saveCredentials(email, password);
                        } else {
                            clearSavedCredentials();
                        }

                        navigateToMain();
                        return;
                    }

                    Exception e = task.getException();
                    Log.e(TAG, "Login failed", e);

                    highlightError(emailInput);
                    highlightError(passwordInput);
                    shakeField(emailInput);
                    shakeField(passwordInput);
                    showError("Invalid email or password");
                });
    }

    private void saveCredentials(String email, String password) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putString(KEY_EMAIL, email)
                .putString(KEY_PASSWORD, password)
                .apply();
    }

    /**
     * Loads saved credentials if they exist. Returns true if credentials exist, false otherwise.
     */
    private boolean loadSavedCredentials() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String savedEmail = prefs.getString(KEY_EMAIL, null);
        String savedPassword = prefs.getString(KEY_PASSWORD, null);

        boolean hasCredentials = savedEmail != null && savedPassword != null;

        if (hasCredentials) {
            emailInput.setText(savedEmail);
            passwordInput.setText(savedPassword);
            rememberMeCheckbox.setChecked(true);
        } else {
            rememberMeCheckbox.setChecked(false);
        }

        return hasCredentials;
    }

    private void clearSavedCredentials() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().clear().apply();
    }

    private void navigateToMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private void showError(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        Log.w(TAG, msg);
    }

    private void highlightError(EditText field) {
        field.setBackgroundTintList(getColorStateList(android.R.color.holo_red_light));
    }

    private void clearErrorStyles() {
        emailInput.setBackgroundTintList(getColorStateList(android.R.color.darker_gray));
        passwordInput.setBackgroundTintList(getColorStateList(android.R.color.darker_gray));
    }

    private void shakeField(EditText field) {
        field.animate()
                .translationX(20)
                .setDuration(50)
                .withEndAction(() ->
                        field.animate()
                                .translationX(0)
                                .setDuration(50)
                );
    }
}
