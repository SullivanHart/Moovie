package com.moovie;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileActivity extends AppCompatActivity {

    private TextView nameTextView, emailTextView, bioTextView;
    private ImageView profileImageView;

    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Toolbar setup with back button
        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        toolbar.setNavigationOnClickListener(v -> {
            // Navigate back to MainActivity
            Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        nameTextView = findViewById(R.id.text_name);
        emailTextView = findViewById(R.id.text_email);
        bioTextView = findViewById(R.id.text_bio);
        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();

        loadUserProfile();
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
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            String bio = documentSnapshot.getString("bio");
                            String username = documentSnapshot.getString("username");
                            String image = documentSnapshot.getString("imageUrl");

                            if (username != null) nameTextView.setText(username);
                            if (bio != null) bioTextView.setText(bio);
                            if (image != null)
                                Glide.with(ProfileActivity.this).load(image).into(profileImageView);
                        } else {
                            bioTextView.setText("No profile info found");
                        }
                    }
                });
    }
}
