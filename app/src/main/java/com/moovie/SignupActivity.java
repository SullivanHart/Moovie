package com.moovie;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * Activity for user registration.
 */
public class SignupActivity extends AppCompatActivity {

    private static final String TAG = "SignupActivity";

    private EditText emailInput, passwordInput;
    private Button signupButton;
    private FirebaseAuth mAuth;

    /**
     * Called when the activity is starting.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down then this Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle).
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mAuth = FirebaseAuth.getInstance();

        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        signupButton = findViewById(R.id.signupButton);

        signupButton.setOnClickListener(v -> signup());
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
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            String uid = user.getUid();

                            Map<String, Object> profile = new HashMap<>();
                            profile.put("userId", uid);
                            profile.put("username", ""); // empty until user edits it
                            profile.put("bio", "");      // empty default
                            profile.put("createdAt", System.currentTimeMillis());

                            FirebaseFirestore.getInstance()
                                    .collection("users")
                                    .document(uid)
                                    .set(profile)
                                    .addOnSuccessListener(aVoid ->
                                            Log.d(TAG, "User profile created in Firestore"))
                                    .addOnFailureListener(e ->
                                            Log.e(TAG, "Failed to create Firestore profile", e));
                        }

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
}
