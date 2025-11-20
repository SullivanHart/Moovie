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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;
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

    private TabLayout mTabLayout;
    private boolean isShowingUnrated = true;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_rating, container, false);

        mFirestore = FirebaseUtil.getFirestore();
        String userId = FirebaseUtil.getAuth().getCurrentUser().getUid();
        
        mRecyclerView = root.findViewById(R.id.recycler_user_ratings);
        mTabLayout = root.findViewById(R.id.tabLayout);

        // Setup Tabs
        mTabLayout.addTab(mTabLayout.newTab().setText("Unrated"));
        mTabLayout.addTab(mTabLayout.newTab().setText("Ranked"));

        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    showUnratedMovies();
                } else {
                    showRankedMovies();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
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

    private void showUnratedMovies() {
        isShowingUnrated = true;
        if (mRankedAdapter != null) mRankedAdapter.stopListening();
        if (mUnratedAdapter != null) mUnratedAdapter.startListening();
        
        mRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        mRecyclerView.setAdapter(mUnratedAdapter);
    }

    private void showRankedMovies() {
        isShowingUnrated = false;
        if (mUnratedAdapter != null) mUnratedAdapter.stopListening();
        if (mRankedAdapter != null) mRankedAdapter.startListening();

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(mRankedAdapter);
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
             // Passed movie.getId() instead of tmdbId to ensure we use the document ID
             intent.putExtra(MovieDetailActivity.KEY_MOVIE_ID, movie.getId());
             startActivity(intent);
        }
    }
}
