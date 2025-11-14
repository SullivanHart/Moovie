package com.moovie;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class ResetPasswordActivity extends AppCompatActivity {

    private static final String TAG = "ResetPasswordActivity";

    private EditText emailInput;
    private Button resetButton;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        mAuth = FirebaseAuth.getInstance();

        emailInput = findViewById(R.id.emailInput);
        resetButton = findViewById(R.id.resetButton);

        resetButton.setOnClickListener(v -> resetPassword());
    }

    private void resetPassword() {
        String email = emailInput.getText().toString().trim();

        if (email.isEmpty()) {
            highlightError(emailInput);
            shakeField(emailInput);
            showError("Please enter your email");
            return;
        }

        clearErrorStyles();

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this,
                                "Password reset email sent to " + email,
                                Toast.LENGTH_LONG).show();
                        Log.d(TAG, "Password reset email sent: " + email);
                        finish(); // go back to login
                    } else {
                        Exception e = task.getException();
                        Log.e(TAG, "Reset password failed", e);
                        highlightError(emailInput);
                        shakeField(emailInput);
                        showError("Failed to send reset email: " + (e != null ? e.getMessage() : "unknown"));
                    }
                });
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
