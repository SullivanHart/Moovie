package com.moovie;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.moovie.adapter.MovieListAdapter;
import com.moovie.util.FirebaseUtil;

public class WatchedFragment extends Fragment implements MovieListAdapter.OnMovieSelectedListener {

    private static final String TAG = "WatchedFragment";

    private RecyclerView mMoviesRecycler;
    private ViewGroup mEmptyView;
    private MovieListAdapter mAdapter;
    private FirebaseFirestore mFirestore;
    private Query mQuery;
    private String mUserId;

    public WatchedFragment() {}

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_watchlist_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mMoviesRecycler = view.findViewById(R.id.recycler_movies);
        mEmptyView = view.findViewById(R.id.view_empty);

        mFirestore = FirebaseUtil.getFirestore();
        mUserId = FirebaseUtil.getAuth().getCurrentUser().getUid();

        CollectionReference collectionRef = mFirestore
                .collection("users")
                .document(mUserId)
                .collection("watched");

        mQuery = collectionRef.orderBy("addedAt", Query.Direction.DESCENDING);

        // Temporary debug: print documents
        collectionRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "Watched collection size: " + task.getResult().size());
                for (DocumentSnapshot doc : task.getResult()) {
                    Log.d(TAG, "Document ID: " + doc.getId() + ", Data: " + doc.getData());
                }
            } else {
                Log.e(TAG, "Error getting documents", task.getException());
            }
        });

        initRecyclerView();
    }

    private void initRecyclerView() {
        if (mQuery == null) {
            Log.w(TAG, "No query, not initializing RecyclerView");
            return;
        }

        mAdapter = new MovieListAdapter(mQuery, this) {
            @Override
            protected void onDataChanged() {
                Log.d(TAG, "onDataChanged called, item count: " + getItemCount());
                if (getItemCount() == 0) {
                    mMoviesRecycler.setVisibility(View.GONE);
                    mEmptyView.setVisibility(View.VISIBLE);
                    Log.d(TAG, "No items in adapter, showing empty view");
                } else {
                    mMoviesRecycler.setVisibility(View.VISIBLE);
                    mEmptyView.setVisibility(View.GONE);
                    Log.d(TAG, "Items found, showing RecyclerView");
                }
            }


            @Override
            protected void onError(FirebaseFirestoreException e) {
                if (getView() != null) {
                    Snackbar.make(mMoviesRecycler, "Error loading watched movies", Snackbar.LENGTH_LONG).show();
                }
                Log.e(TAG, "Error: ", e);
            }
        };

        mMoviesRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        mMoviesRecycler.setAdapter(mAdapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mAdapter != null) mAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAdapter != null) mAdapter.stopListening();
    }

    @Override
    public void onMovieSelected(DocumentSnapshot movie) {
        Intent intent = new Intent(getActivity(), MovieDetailActivity.class);
        intent.putExtra(MovieDetailActivity.KEY_MOVIE_ID, movie.getId());
        startActivity(intent);
    }
}
