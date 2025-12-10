package com.moovie;

import android.app.AlertDialog;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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
import com.google.firebase.firestore.QuerySnapshot;
import com.moovie.model.MovieListItem;
import com.moovie.model.TMDBResponse;
import com.moovie.network.TMDBService;
import com.moovie.util.FirebaseUtil;
import com.moovie.util.TMDBApiClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Fragment to display the user's profile and statistics.
 */
public class ProfileFragment extends Fragment {

    private TextView nameTextView, emailTextView, bioTextView;
    private Button editButton;
    private ImageButton buttonSignOut;
    private Button buttonDeleteAccount;

    // Stats TextViews
    private TextView statsMoviesWatched;
    private TextView statsMoviesRanked;
    private TextView statsWantToWatch;
    private TextView statsTopGenre;
    private TextView statsPercentageSeen;
    private TextView statsGenreBreakdown;

    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;

    /**
     * Default constructor for ProfileFragment.
     */
    public ProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Called to have the fragment instantiate its user interface view.
     *
     * @param inflater           The LayoutInflater object that can be used to inflate any views in the fragment.
     * @param container          If non-null, this is the parent view that the fragment's UI should be attached to.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state as given here.
     * @return Return the View for the fragment's UI, or null.
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();

        // Find views
        MaterialToolbar toolbar = view.findViewById(R.id.topAppBar);
        nameTextView = view.findViewById(R.id.text_name);
        emailTextView = view.findViewById(R.id.text_email);
        bioTextView = view.findViewById(R.id.text_bio);
        editButton = view.findViewById(R.id.button_edit_profile);
        buttonSignOut = view.findViewById(R.id.button_sign_out);
        buttonDeleteAccount = view.findViewById(R.id.button_delete_account);

        // Stats views
        statsMoviesWatched = view.findViewById(R.id.stats_movies_watched);
        statsMoviesRanked = view.findViewById(R.id.stats_movies_ranked);
        statsWantToWatch = view.findViewById(R.id.stats_want_to_watch);
        statsTopGenre = view.findViewById(R.id.stats_top_genre);
        statsPercentageSeen = view.findViewById(R.id.stats_percentage_seen);
        statsGenreBreakdown = view.findViewById(R.id.stats_genre_breakdown);

        // Set toolbar title
        toolbar.setTitle("Profile");

        // Handle back navigation
        toolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());

        // Load user info
        loadUserProfile();
        loadUserStats();

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            editButton.setVisibility(View.VISIBLE);
        } else {
            editButton.setVisibility(View.GONE);
        }

        editButton.setOnClickListener(v -> showEditProfileDialog());
        buttonSignOut.setOnClickListener(v -> signOut());
        buttonDeleteAccount.setOnClickListener(v -> deleteAccount());

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

        emailTextView.setText(email != null ? email : "No email");
        nameTextView.setText(displayName != null ? displayName : "User");

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

    private void loadUserStats() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        String uid = user.getUid();

        // Load watched movies count
        mFirestore.collection("users").document(uid)
                .collection("watched")
                .get()
                .addOnSuccessListener(watchedSnapshot -> {
                    int totalWatched = watchedSnapshot.size();
                    statsMoviesWatched.setText(String.valueOf(totalWatched));

                    Log.d("ProfileFragment", "Total watched movies: " + totalWatched);

                    // Count ranked movies and find top genre
                    int rankedCount = 0;
                    Map<String, Integer> genreCounts = new HashMap<>();

                    for (com.google.firebase.firestore.DocumentSnapshot doc : watchedSnapshot.getDocuments()) {
                        MovieListItem item = doc.toObject(MovieListItem.class);

                        Log.d("ProfileFragment", "Movie: " + (item != null ? item.getTitle() : "null"));

                        if (item != null) {
                            // Count ranked movies (must have ranked=true)
                            if (item.isRanked() && item.getRankIndex() >= 0) {
                                rankedCount++;
                            }

                            // Track genres
                            String genre = item.getGenre();
                            Log.d("ProfileFragment", "  Genre: " + genre);

                            if (genre != null && !genre.isEmpty()) {
                                genreCounts.put(genre, genreCounts.getOrDefault(genre, 0) + 1);
                                Log.d("ProfileFragment", "  Added genre: " + genre + " (count now: " + genreCounts.get(genre) + ")");
                            } else {
                                Log.d("ProfileFragment", "  Genre is null or empty!");
                            }
                        }
                    }

                    Log.d("ProfileFragment", "Ranked count: " + rankedCount);
                    Log.d("ProfileFragment", "Genre map: " + genreCounts.toString());

                    statsMoviesRanked.setText(String.valueOf(rankedCount));

                    // Find most common genre
                    if (!genreCounts.isEmpty()) {
                        String topGenre = findTopGenre(genreCounts);
                        Log.d("ProfileFragment", "Top genre: " + topGenre);
                        statsTopGenre.setText(topGenre);

                        // Show genre breakdown
                        String breakdown = createGenreBreakdown(genreCounts, totalWatched);
                        statsGenreBreakdown.setText(breakdown);
                    } else {
                        Log.d("ProfileFragment", "Genre counts is empty - showing N/A");
                        statsTopGenre.setText("N/A");
                        statsGenreBreakdown.setText("Add movies to see genre breakdown");
                    }

                    // Calculate percentage of all movies in database that user has seen
                    calculatePercentageSeen(totalWatched);
                })
                .addOnFailureListener(e -> {
                    statsMoviesWatched.setText("0");
                    statsMoviesRanked.setText("0");
                });

        // Load want to watch count
        mFirestore.collection("users").document(uid)
                .collection("wantToWatch")
                .get()
                .addOnSuccessListener(wantSnapshot -> {
                    statsWantToWatch.setText(String.valueOf(wantSnapshot.size()));
                })
                .addOnFailureListener(e -> {
                    statsWantToWatch.setText("0");
                });
    }

    private void calculatePercentageSeen(int watchedCount) {

        TMDBService service = TMDBApiClient.getClient().create(TMDBService.class);

        service.getTotalMovies(BuildConfig.TMDB_API_KEY).enqueue(new Callback<TMDBResponse>() {
            @Override
            public void onResponse(Call<TMDBResponse> call, Response<TMDBResponse> response) {
                Log.d("ProfileFragment", "Response:" + response );

                if (!response.isSuccessful() || response.body() == null) {
                    Log.d("ProfileFragment", "Response unsuccessful." );
                    statsPercentageSeen.setText("N/A");
                    return;
                }

                int totalMovies = response.body().getTotalResults();
                Log.d("ProfileFragment", "Total movies:" + totalMovies );


                if (totalMovies > 0) {
                    double percentage = (watchedCount * 100.0) / totalMovies;
                    statsPercentageSeen.setText(String.format("%.1f%%", percentage));
                } else {
                    statsPercentageSeen.setText("0%");
                }
            }

            @Override
            public void onFailure(Call<TMDBResponse> call, Throwable t) {
                statsPercentageSeen.setText("N/A");
            }
        });
    }


    private String createGenreBreakdown(Map<String, Integer> genreCounts, int total) {
        // Sort genres by count (descending)
        List<Map.Entry<String, Integer>> sortedGenres = new ArrayList<>(genreCounts.entrySet());
        sortedGenres.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        // Take top 3 genres
        StringBuilder breakdown = new StringBuilder();
        int count = 0;
        for (Map.Entry<String, Integer> entry : sortedGenres) {
            if (count >= 3) break;

            if (count > 0) breakdown.append("\n");

            String genre = entry.getKey();
            int genreCount = entry.getValue();
            double percentage = (genreCount * 100.0) / total;

            breakdown.append(String.format("%s: %.0f%%", genre, percentage));
            count++;
        }

        return breakdown.toString();
    }

    private String findTopGenre(Map<String, Integer> genreCounts) {
        String topGenre = "";
        int maxCount = 0;

        for (Map.Entry<String, Integer> entry : genreCounts.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                topGenre = entry.getKey();
            }
        }

        return topGenre.isEmpty() ? "N/A" : topGenre;
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

    private void deleteAccount() {
        new AlertDialog.Builder(getContext())
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to delete your account?\nThis action cannot be undone.")
                .setPositiveButton("Yes", (dialog, which) -> performAccountDeletion())
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    private void performAccountDeletion() {
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            String uid = user.getUid();

            // Step 1: Delete Firestore document
            mFirestore.collection("users").document(uid)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        // Step 2: Delete authentication user
                        user.delete()
                                .addOnSuccessListener(unused -> {
                                    Toast.makeText(getContext(),
                                            "Account fully deleted", Toast.LENGTH_SHORT).show();
                                    signOut();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(getContext(),
                                            "Failed to delete authentication: " + e.getMessage(),
                                            Toast.LENGTH_LONG).show();
                                });

                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(),
                                "Failed to delete account data: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    });

        } else {
            Toast.makeText(getContext(), "No user signed in", Toast.LENGTH_SHORT).show();
        }
    }

    private void signOut() {
        mAuth.signOut();
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }
}
