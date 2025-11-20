package com.moovie;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.moovie.adapter.MovieListAdapter;
import com.moovie.adapter.RankedMovieAdapter;
import com.moovie.model.MovieListItem;
import com.moovie.util.FirebaseUtil;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class RatingPageFragment extends Fragment {

    private static final String TAG = "RatingPageFragment";

    private RecyclerView mRecyclerView;
    private MovieListAdapter mUnratedAdapter;
    private RankedMovieAdapter mRankedAdapter;
    private FirebaseFirestore mFirestore;
    private Query mUnratedQuery;
    private Query mRankedQuery;

    private MaterialToolbar mToolbar;
    private boolean isShowingUnrated = true;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_rating, container, false);

        mFirestore = FirebaseUtil.getFirestore();
        String userId = FirebaseUtil.getAuth().getCurrentUser().getUid();
        
        mRecyclerView = root.findViewById(R.id.recycler_user_ratings);
        mToolbar = root.findViewById(R.id.topAppBar);

        // Setup Toolbar
        mToolbar.inflateMenu(R.menu.menu_rating);
        mToolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_switch_view) {
                toggleView();
                return true;
            }
            return false;
        });

        // Query for Unrated Movies
        mUnratedQuery = mFirestore.collection("users")
                .document(userId)
                .collection("watched")
                .whereEqualTo("ranked", false);

        // Query for Ranked Movies
        mRankedQuery = mFirestore.collection("users")
                .document(userId)
                .collection("watched")
                .whereEqualTo("ranked", true)
                .orderBy("rankIndex", Query.Direction.ASCENDING);

        // Initialize Adapters
        mUnratedAdapter = new MovieListAdapter(mUnratedQuery, this::onUnratedMovieSelected) {
             @Override
            protected void onDataChanged() {
                if (isShowingUnrated && getItemCount() == 0) {
                    Log.d(TAG, "No unrated movies found.");
                }
            }
        };

        mRankedAdapter = new RankedMovieAdapter(mRankedQuery, this::onRankedMovieSelected) {
            @Override
            protected void onDataChanged() {
                 if (!isShowingUnrated && getItemCount() == 0) {
                    Log.d(TAG, "No ranked movies found.");
                }
            }
        };

        // Set default view (Unrated)
        showUnratedMovies();

        return root;
    }

    private void toggleView() {
        if (isShowingUnrated) {
            showRankedMovies();
        } else {
            showUnratedMovies();
        }
    }

    private void showUnratedMovies() {
        isShowingUnrated = true;
        if (mRankedAdapter != null) mRankedAdapter.stopListening();
        if (mUnratedAdapter != null) mUnratedAdapter.startListening();
        
        mRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        mRecyclerView.setAdapter(mUnratedAdapter);
        mToolbar.setTitle("Unrated Movies");
        
        // Update menu icon or text if needed, e.g. to "Show Ranked"
        // For now, the same icon toggles.
    }

    private void showRankedMovies() {
        isShowingUnrated = false;
        if (mUnratedAdapter != null) mUnratedAdapter.stopListening();
        if (mRankedAdapter != null) mRankedAdapter.startListening();

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(mRankedAdapter);
        mToolbar.setTitle("My Rankings");
    }

    @Override
    public void onStart() {
        super.onStart();
        if (isShowingUnrated && mUnratedAdapter != null) {
            mUnratedAdapter.startListening();
        } else if (!isShowingUnrated && mRankedAdapter != null) {
            mRankedAdapter.startListening();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mUnratedAdapter != null) {
            mUnratedAdapter.stopListening();
        }
        if (mRankedAdapter != null) {
            mRankedAdapter.stopListening();
        }
    }

    public void onUnratedMovieSelected(DocumentSnapshot movie) {
        MovieListItem item = movie.toObject(MovieListItem.class);
        if (item != null) {
            // Go to RankingActivity
            Intent intent = new Intent(getContext(), RankingActivity.class);
            intent.putExtra(RankingActivity.KEY_MOVIE_ID, movie.getId());
            startActivity(intent);
        }
    }

    public void onRankedMovieSelected(DocumentSnapshot movie) {
        MovieListItem item = movie.toObject(MovieListItem.class);
        if (item != null) {
             Intent intent = new Intent(getContext(), MovieDetailActivity.class);
             // We need to pass the document ID of the movie in the "movies" collection.
             // Assuming item.getTmdbId() corresponds to the document ID in "movies"
             intent.putExtra(MovieDetailActivity.KEY_MOVIE_ID, String.valueOf(item.getTmdbId()));
             startActivity(intent);
        }
    }
}
