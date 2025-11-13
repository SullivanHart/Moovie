package com.moovie;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileActivity extends AppCompatActivity {

    private TextView nameTextView, emailTextView, bioTextView;
    private ImageView profileImageView;
    private Button editButton;

    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Toolbar setup with back button
        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        toolbar.setNavigationOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        // Initialize views
        nameTextView = findViewById(R.id.text_name);
        emailTextView = findViewById(R.id.text_email);
        bioTextView = findViewById(R.id.text_bio);
        editButton = findViewById(R.id.button_edit_profile);

        // Firebase init
        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();

        // Load user info
        loadUserProfile();

        // Show the edit button only for logged-in users
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            editButton.setVisibility(View.VISIBLE);
        } else {
            editButton.setVisibility(View.GONE);
        }

        editButton.setOnClickListener(v -> showEditProfileDialog());
    }

    private void loadUserProfile() {
        FirebaseUser user = mAuth.getCurrentUser();

        if (user == null) {
            Toast.makeText(this, "No user signed in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Basic info from Firebase Auth
        String uid = user.getUid();
        String email = user.getEmail();
        String displayName = user.getDisplayName();
        String photoUrl = (user.getPhotoUrl() != null) ? user.getPhotoUrl().toString() : null;

        emailTextView.setText(email != null ? email : "No email");
        nameTextView.setText(displayName != null ? displayName : "User");

        if (photoUrl != null) {
            Glide.with(this).load(photoUrl).into(profileImageView);
        }

        // Additional info from Firestore: /users/{uid}
        mFirestore.collection("users").document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String bio = documentSnapshot.getString("bio");
                        String username = documentSnapshot.getString("username");

                        if (username != null) nameTextView.setText(username);
                        if (bio != null) bioTextView.setText(bio);
                    } else {
                        bioTextView.setText("No profile info found");
                    }
                });
    }

    private void showEditProfileDialog() {
        // Inflate a custom layout for the dialog
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_profile, null);

        EditText editUsername = dialogView.findViewById(R.id.edit_username);
        EditText editBio = dialogView.findViewById(R.id.edit_bio);

        // Pre-fill with current values
        editUsername.setText(nameTextView.getText().toString());
        editBio.setText(bioTextView.getText().toString());

        // Build the AlertDialog
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Edit Profile")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newUsername = editUsername.getText().toString().trim();
                    String newBio = editBio.getText().toString().trim();

                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user != null) {
                        mFirestore.collection("users").document(user.getUid())
                                .update("username", newUsername, "bio", newBio)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show();
                                    nameTextView.setText(newUsername);
                                    bioTextView.setText(newBio);
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show()
                                );
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
