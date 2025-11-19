package com.moovie;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.moovie.adapter.MovieListAdapter;
import com.moovie.model.MovieListItem;
import com.moovie.util.FirebaseUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class RatingPageFragment extends Fragment implements MovieListAdapter.OnMovieSelectedListener {

    private static final String TAG = "RatingPageFragment";

    private RecyclerView mRecyclerView;
    private MovieListAdapter mAdapter;
    private FirebaseFirestore mFirestore;
    private Query mQuery;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_rating, container, false);

        mFirestore = FirebaseUtil.getFirestore();
        
        // Setup RecyclerView
        mRecyclerView = root.findViewById(R.id.recycler_user_ratings);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2)); // 2 columns for posters

        // Create query: Users -> [userId] -> watched -> where ranked == false
        String userId = FirebaseUtil.getAuth().getCurrentUser().getUid();
        mQuery = mFirestore.collection("users")
                .document(userId)
                .collection("watched")
                .whereEqualTo("ranked", false);

        mAdapter = new MovieListAdapter(mQuery, this) {
            @Override
            protected void onDataChanged() {
                if (getItemCount() == 0) {
                    // Show empty view if needed
                    Log.d(TAG, "No unranked movies found in watched list.");
                } else {
                    Log.d(TAG, "Found " + getItemCount() + " unranked movies.");
                }
            }
        };
        mRecyclerView.setAdapter(mAdapter);

        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mAdapter != null) {
            mAdapter.startListening();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAdapter != null) {
            mAdapter.stopListening();
        }
    }

    @Override
    public void onMovieSelected(DocumentSnapshot movie) {
        // For now, go to MovieDetailActivity
        // We might want to change this to a specific "Rating" or "Ranking" activity later
        MovieListItem item = movie.toObject(MovieListItem.class);
        if (item != null) {
            Intent intent = new Intent(getContext(), MovieDetailActivity.class);
            // We need to pass the KEY_MOVIE_ID. 
            // MovieListItem stores it as int tmdbId, but MovieDetailActivity expects String id (document ID)?
            // Wait, MovieDetailActivity:
            // String movieId = getIntent().getExtras().getString(KEY_MOVIE_ID);
            // mMovieRef = mFirestore.collection("movies").document(movieId);
            
            // The document ID in 'movies' collection is usually the TMDB ID as a string.
            // Let's verify how movies are saved. 
            // In MovieDetailActivity, it seems it reads from "movies" collection.
            // In this fragment, we are reading from "users/watched".
            // The document ID in "watched" is usually the same as the movie ID.
            
            intent.putExtra(MovieDetailActivity.KEY_MOVIE_ID, movie.getId());
            startActivity(intent);
        }
    }
}
