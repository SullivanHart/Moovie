package com.moovie;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.moovie.adapter.RatingAdapter;
import com.moovie.model.Movie;
import com.moovie.model.Rating;
import com.moovie.util.FirebaseUtil;
import com.moovie.util.MovieUtil;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.Transaction;

import me.zhanghai.android.materialratingbar.MaterialRatingBar;

public class MovieDetailActivity extends AppCompatActivity
        implements EventListener<DocumentSnapshot>, RatingDialogFragment.RatingListener {

    private static final String TAG = "MovieDetailActivity";

    public static final String KEY_MOVIE_ID = "key_movie_id";

    private ImageView mImageView;
    private TextView mTitleView;
    private MaterialRatingBar mRatingIndicator;
    private TextView mNumRatingsView;
    private ViewGroup mEmptyView;
    private RecyclerView mRatingsRecycler;

    private RatingDialogFragment mRatingDialog;

    private FirebaseFirestore mFirestore;
    private DocumentReference mMovieRef;
    private ListenerRegistration mMovieRegistration;

    private RatingAdapter mRatingAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);

        mImageView = findViewById(R.id.movie_image);
        mTitleView = findViewById(R.id.movie_title);
        mRatingIndicator = findViewById(R.id.movie_rating);
        mNumRatingsView = findViewById(R.id.movie_num_ratings);
        mEmptyView = findViewById(R.id.view_empty_ratings);
        mRatingsRecycler = findViewById(R.id.recycler_ratings);

        // Get movie ID from extras
        String movieId = getIntent().getExtras().getString(KEY_MOVIE_ID);
        if (movieId == null) {
            throw new IllegalArgumentException("Must pass extra " + KEY_MOVIE_ID);
        }

        // Initialize Firestore
        mFirestore = FirebaseUtil.getFirestore();

        // Get reference to the movie
        mMovieRef = mFirestore.collection("movies").document(movieId);

        // Get ratings
        Query ratingsQuery = mMovieRef
                .collection("ratings")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(50);

        // RecyclerView
        mRatingAdapter = new RatingAdapter(ratingsQuery) {
            @Override
            protected void onDataChanged() {
                if (getItemCount() == 0) {
                    mRatingsRecycler.setVisibility(View.GONE);
                    mEmptyView.setVisibility(View.VISIBLE);
                } else {
                    mRatingsRecycler.setVisibility(View.VISIBLE);
                    mEmptyView.setVisibility(View.GONE);
                }
            }
        };

        mRatingsRecycler.setLayoutManager(new LinearLayoutManager(this));
        mRatingsRecycler.setAdapter(mRatingAdapter);

        mRatingDialog = new RatingDialogFragment();

        // Set click listeners programmatically for buttons
        findViewById(R.id.fab_show_rating_dialog).setOnClickListener(this::onAddRatingClicked);
        findViewById(R.id.movie_button_back).setOnClickListener(this::onBackArrowClicked);
    }

    @Override
    public void onStart() {
        super.onStart();
        mRatingAdapter.startListening();
        mMovieRegistration = mMovieRef.addSnapshotListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        mRatingAdapter.stopListening();

        if (mMovieRegistration != null) {
            mMovieRegistration.remove();
            mMovieRegistration = null;
        }
    }

    private Task<Void> addRating(final DocumentReference movieRef, final Rating rating) {
        final DocumentReference ratingRef = movieRef.collection("ratings").document();

        return mFirestore.runTransaction(transaction -> {
            Movie movie = transaction.get(movieRef).toObject(Movie.class);
            int newNumRatings = movie.getNumRatings() + 1;
            double oldRatingTotal = movie.getAvgRating() * movie.getNumRatings();
            double newAvgRating = (oldRatingTotal + rating.getRating()) / newNumRatings;

            movie.setNumRatings(newNumRatings);
            movie.setAvgRating(newAvgRating);

            transaction.set(movieRef, movie);
            transaction.set(ratingRef, rating);
            return null;
        });
    }

    @Override
    public void onEvent(DocumentSnapshot snapshot, FirebaseFirestoreException e) {
        if (e != null) {
            Log.w(TAG, "movie:onEvent", e);
            return;
        }

        onMovieLoaded(snapshot.toObject(Movie.class));
    }

    private void onMovieLoaded(Movie movie) {
        mTitleView.setText(movie.getTitle());
        mRatingIndicator.setRating((float) movie.getAvgRating());
        mNumRatingsView.setText(getString(R.string.fmt_num_ratings, movie.getNumRatings()));

        Glide.with(mImageView.getContext())
                .load(movie.getPosterUrl())
                .into(mImageView);
    }

    public void onBackArrowClicked(View view) {
        onBackPressed();
    }

    public void onAddRatingClicked(View view) {
        mRatingDialog.show(getSupportFragmentManager(), RatingDialogFragment.TAG);
    }

    @Override
    public void onRating(Rating rating) {
        addRating(mMovieRef, rating)
                .addOnSuccessListener(this, aVoid -> {
                    Log.d(TAG, "Rating added");
                    hideKeyboard();
                    mRatingsRecycler.smoothScrollToPosition(0);
                })
                .addOnFailureListener(this, e -> {
                    Log.w(TAG, "Add rating failed", e);
                    hideKeyboard();
                    Snackbar.make(findViewById(android.R.id.content), "Failed to add rating",
                            Snackbar.LENGTH_SHORT).show();
                });
    }

    private void hideKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                    .hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
