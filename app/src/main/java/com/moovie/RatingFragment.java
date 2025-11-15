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

    private void loadUnratedWatchedMovies() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Task to get all rated movie IDs
        Task<QuerySnapshot> ratedTask = mFirestore.collection("users").document(userId)
                .collection("ratings").get();

        // Task to get all watched movie IDs (assuming from a 'watchlist' collection)
        Task<QuerySnapshot> watchlistTask = mFirestore.collection("users").document(userId)
                .collection("watchlist").get();

        Tasks.whenAllSuccess(ratedTask, watchlistTask).addOnSuccessListener(results -> {
            // 1. Get IDs from results
            Set<String> ratedMovieIds = new HashSet<>();
            for (DocumentSnapshot doc : (QuerySnapshot) results.get(0)) {
                ratedMovieIds.add(doc.getId());
            }

            List<String> watchedMovieIds = new ArrayList<>();
            for (DocumentSnapshot doc : (QuerySnapshot) results.get(1)) {
                watchedMovieIds.add(doc.getId());
            }

            // 2. Find the difference: watched but not rated
            watchedMovieIds.removeAll(ratedMovieIds);

            // 3. Create a query for the unrated movies
            if (watchedMovieIds.isEmpty()) {
                mRecycler.setVisibility(View.GONE);
                mEmptyView.setVisibility(View.VISIBLE);
                return;
            }

            // Firestore 'whereIn' queries are limited to 10 items. We'll show the first 10.
            List<String> unratedIdsToShow = watchedMovieIds.subList(0, Math.min(10, watchedMovieIds.size()));

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
        }).addOnFailureListener(e -> Log.e(TAG, "Failed to load movies", e));
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
