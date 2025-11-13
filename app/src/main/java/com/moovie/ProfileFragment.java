package com.moovie;

import android.app.AlertDialog;
import android.content.Intent;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.moovie.util.FirebaseUtil;

public class ProfileFragment extends Fragment {

    private TextView nameTextView, emailTextView, bioTextView;
    private ImageView profileImageView;
    private Button editButton;
    private Button buttonSignOut;

    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();

        // Find views
        MaterialToolbar toolbar = view.findViewById(R.id.topAppBar);
        profileImageView = view.findViewById(R.id.image_profile);
        nameTextView = view.findViewById(R.id.text_name);
        emailTextView = view.findViewById(R.id.text_email);
        bioTextView = view.findViewById(R.id.text_bio);
        editButton = view.findViewById(R.id.button_edit_profile);
        buttonSignOut = view.findViewById(R.id.button_sign_out);


        // Set toolbar title
        toolbar.setTitle("Profile");

        // Handle back navigation
        toolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());

        // Load user info
        loadUserProfile();

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            editButton.setVisibility(View.VISIBLE);
        } else {
            editButton.setVisibility(View.GONE);
        }

        editButton.setOnClickListener(v -> showEditProfileDialog());
        buttonSignOut.setOnClickListener(v -> signOut());

        return view;
    }

    private void loadUserProfile() {
        FirebaseUser user = mAuth.getCurrentUser();

        if (user == null) {
            Toast.makeText(getContext(), "No user signed in", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = user.getUid();
        String email = user.getEmail();
        String displayName = user.getDisplayName();
        String photoUrl = (user.getPhotoUrl() != null) ? user.getPhotoUrl().toString() : null;

        emailTextView.setText(email != null ? email : "No email");
        nameTextView.setText(displayName != null ? displayName : "User");

        if (photoUrl != null && getContext() != null) {
            Glide.with(getContext()).load(photoUrl).into(profileImageView);
        }

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
        if (getContext() == null) return;

        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_edit_profile, null);
        EditText editUsername = dialogView.findViewById(R.id.edit_username);
        EditText editBio = dialogView.findViewById(R.id.edit_bio);

        editUsername.setText(nameTextView.getText().toString());
        editBio.setText(bioTextView.getText().toString());

        new AlertDialog.Builder(getContext())
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
                                    Toast.makeText(getContext(), "Profile updated", Toast.LENGTH_SHORT).show();
                                    nameTextView.setText(newUsername);
                                    bioTextView.setText(newBio);
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(getContext(), "Failed to update profile", Toast.LENGTH_SHORT).show());
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void signOut() {
        mAuth.signOut();
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

}
