package com.moovie;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class SignupActivity extends AppCompatActivity {

    private static final String TAG = "SignupActivity";

    private EditText emailInput, passwordInput;
    private Button signupButton, resetPasswordButton;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mAuth = FirebaseAuth.getInstance();

        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        signupButton = findViewById(R.id.signupButton);
        resetPasswordButton = findViewById(R.id.resetPasswordButton);

        signupButton.setOnClickListener(v -> signup());
        resetPasswordButton.setOnClickListener(v -> resetPassword());
    }

    private void signup() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Account created successfully", Toast.LENGTH_LONG).show();
                        finish(); // back to login
                    } else {
                        Toast.makeText(this, "Sign-up failed: " +
                                (task.getException() != null ? task.getException().getMessage() : "unknown"),
                                Toast.LENGTH_LONG).show();
                        Log.e(TAG, "Signup failed", task.getException());
                    }
                });
    }

    private void resetPassword() {
        String email = emailInput.getText().toString().trim();

        if (email.isEmpty()) {
            Toast.makeText(this, "Enter your email to reset password", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.sendPasswordResetEmail(email)
                .addOnSuccessListener(v ->
                        Toast.makeText(this, "Password reset email sent", Toast.LENGTH_LONG).show())
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to send reset email: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Reset password failed", e);
                });
    }
}
