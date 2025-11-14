package com.moovie;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.moovie.adapter.MovieAdapter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RatingFragment extends Fragment implements MovieAdapter.OnMovieSelectedListener {

    private static final String TAG = "RatingFragment";

    private FirebaseFirestore mFirestore;
    private RecyclerView mRecycler;
    private TextView mEmptyView;
    private MovieAdapter mAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rating, container, false);

        mRecycler = view.findViewById(R.id.recycler_unrated_movies);
        mEmptyView = view.findViewById(R.id.view_empty_unrated);

        // Initialize Firestore
        mFirestore = FirebaseFirestore.getInstance();

        // Setup RecyclerView
        mRecycler.setLayoutManager(new LinearLayoutManager(getContext()));

        loadUnratedWatchedMovies();

        return view;
    }

    // In RatingFragment.java

    private void loadUnratedWatchedMovies() {
        // 1. Ensure the user is logged in
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Log.e(TAG, "Cannot load movies, user is not signed in.");
            mRecycler.setVisibility(View.GONE);
            mEmptyView.setVisibility(View.VISIBLE);
            mEmptyView.setText("Please sign in to see your unrated movies.");
            return;
        }
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // 2. Get the entire user document which contains the watchlist and rated_movies_sorted arrays
        mFirestore.collection("users").document(userId).get().addOnSuccessListener(userDoc -> {
            if (!userDoc.exists()) {
                Log.e(TAG, "User document does not exist.");
                mEmptyView.setVisibility(View.VISIBLE);
                return;
            }

            // 3. Extract the arrays of DocumentReferences from the user document
            // Make sure the field names "watchlist" and "rated_movies_sorted" match your Firestore exactly!
            List<com.google.firebase.firestore.DocumentReference> watchedRefs =
                    (List<com.google.firebase.firestore.DocumentReference>) userDoc.get("watchlist");

            List<com.google.firebase.firestore.DocumentReference> ratedRefs =
                    (List<com.google.firebase.firestore.DocumentReference>) userDoc.get("rated_movies_sorted");

            // Handle cases where lists might be null (user hasn't watched/rated anything yet)
            if (watchedRefs == null || watchedRefs.isEmpty()) {
                Log.d(TAG, "User has no movies in their watchlist.");
                mRecycler.setVisibility(View.GONE);
                mEmptyView.setVisibility(View.VISIBLE);
                return;
            }

            // 4. Get the String IDs from the DocumentReferences
            Set<String> watchedMovieIds = new HashSet<>();
            for (com.google.firebase.firestore.DocumentReference ref : watchedRefs) {
                watchedMovieIds.add(ref.getId());
            }

            if (ratedRefs != null) {
                Set<String> ratedMovieIds = new HashSet<>();
                for (com.google.firebase.firestore.DocumentReference ref : ratedRefs) {
                    ratedMovieIds.add(ref.getId());
                }
                // Find the difference: watched but not rated
                watchedMovieIds.removeAll(ratedMovieIds);
            }

            // 5. Create and run the query for the unrated movies
            if (watchedMovieIds.isEmpty()) {
                Log.d(TAG, "All watched movies have been rated.");
                mRecycler.setVisibility(View.GONE);
                mEmptyView.setVisibility(View.VISIBLE);
                mEmptyView.setText("You've rated all your watched movies!");
                return;
            }

            // Convert the Set to a List for the 'whereIn' query
            List<String> unratedIdsToShow = new ArrayList<>(watchedMovieIds);

            // Handle Firestore's 30-item limit for 'whereIn' queries
            if (unratedIdsToShow.size() > 30) {
                unratedIdsToShow = unratedIdsToShow.subList(0, 30);
            }

            Query unratedMoviesQuery = mFirestore.collection("movies")
                    .whereIn(com.google.firebase.firestore.FieldPath.documentId(), unratedIdsToShow);

            mAdapter = new MovieAdapter(unratedMoviesQuery, this) {
                @Override
                protected void onDataChanged() {
                    super.onDataChanged();
                    if (getItemCount() == 0) {
                        mRecycler.setVisibility(View.GONE);
                        mEmptyView.setVisibility(View.VISIBLE);
                    } else {
                        mRecycler.setVisibility(View.VISIBLE);
                        mEmptyView.setVisibility(View.GONE);
                    }
                }
            };

            mRecycler.setAdapter(mAdapter);
            if (mAdapter != null) {
                mAdapter.startListening();
            }

        }).addOnFailureListener(e -> {
            Log.e(TAG, "Failed to load user document", e);
            // This is where your PERMISSION_DENIED error was being thrown
            mEmptyView.setText("Error: " + e.getMessage());
            mEmptyView.setVisibility(View.VISIBLE);
        });
    }


    @Override
    public void onMovieSelected(DocumentSnapshot movie) {
        // When a movie is clicked, open the detail activity
        Intent intent = new Intent(getActivity(), MovieDetailActivity.class);
        intent.putExtra(MovieDetailActivity.KEY_MOVIE_ID, movie.getId());
        startActivity(intent);
    }

    @Override
    public void onStart() {
        super.onStart();
        // Start listening for Firestore updates
        if (mAdapter != null) {
            mAdapter.startListening();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        // Stop listening for Firestore updates
        if (mAdapter != null) {
            mAdapter.stopListening();
        }
    }
}
