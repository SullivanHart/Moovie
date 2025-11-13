package com.moovie;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private EditText emailInput, passwordInput;
    private Button loginButton, signupButton;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.loginButton);
        signupButton = findViewById(R.id.signupButton);

        loginButton.setOnClickListener(v -> attemptLogin());
        signupButton.setOnClickListener(v ->
                startActivity(new Intent(this, SignupActivity.class)));

        // Auto-login
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
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
                        navigateToMain();
                        return;
                    }

                    Exception e = task.getException();
                    Log.e(TAG, "Login failed", e);

                    // Always generic error for Firebase email enumeration protection
                    highlightError(emailInput);
                    highlightError(passwordInput);
                    shakeField(emailInput);
                    shakeField(passwordInput);
                    showError("Invalid email or password");
                });
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
