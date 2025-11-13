package com.moovie;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;
import com.moovie.adapter.RatingAdapter;
import com.moovie.model.Movie;
import com.moovie.model.MovieListItem;
import com.moovie.model.Rating;
import com.moovie.util.FirebaseUtil;
import com.moovie.util.ImageUtil;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

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
    private Button buttonWatched;
    private Button buttonWantToWatch;

    private RatingDialogFragment mRatingDialog;

    private FirebaseFirestore mFirestore;
    private DocumentReference mMovieRef;
    private DocumentReference mUserRef;
    private ListenerRegistration mMovieRegistration;
    private RatingAdapter mRatingAdapter;

    private Movie mCurrentMovie;

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
        buttonWatched = findViewById(R.id.button_watched);
        buttonWantToWatch = findViewById(R.id.button_want_to_watch);

        String movieId = getIntent().getExtras().getString(KEY_MOVIE_ID);
        if (movieId == null) {
            throw new IllegalArgumentException("Must pass extra " + KEY_MOVIE_ID);
        }

        mFirestore = FirebaseUtil.getFirestore();
        String userId = FirebaseUtil.getAuth().getCurrentUser().getUid();

        mMovieRef = mFirestore.collection("movies").document(movieId);
        mUserRef = mFirestore.collection("users").document(userId);

        // Get ratings
        Query ratingsQuery = mMovieRef
                .collection("ratings")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(50);

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

        // Button listeners
        buttonWatched.setOnClickListener(v -> toggleWatched());
        buttonWantToWatch.setOnClickListener(v -> toggleWantToWatch());

        mRatingDialog = new RatingDialogFragment();

        findViewById(R.id.fab_show_rating_dialog).setOnClickListener(this::onAddRatingClicked);
        findViewById(R.id.movie_button_back).setOnClickListener(this::onBackArrowClicked);

        // Load user's status for this movie
        loadUserMovieStatus();
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

    private void loadUserMovieStatus() {
        mUserRef.collection("watched")
                .document(mMovieRef.getId())
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        buttonWatched.setSelected(true);
                    }
                })
                .addOnFailureListener(e -> Log.w(TAG, "Error loading watched status", e));

        mUserRef.collection("wantToWatch")
                .document(mMovieRef.getId())
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        buttonWantToWatch.setSelected(true);
                    }
                })
                .addOnFailureListener(e -> Log.w(TAG, "Error loading want to watch status", e));
    }

    private void toggleWatched() {
        buttonWatched.setSelected(!buttonWatched.isSelected());

        DocumentReference watchedRef = mUserRef.collection("watched").document(mMovieRef.getId());

        if (buttonWatched.isSelected()) {
            // Add to watched
            watchedRef.set(new MovieListItem(mCurrentMovie))
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Added to Watched", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "Movie added to watched list");
                    })
                    .addOnFailureListener(e -> {
                        buttonWatched.setSelected(false);
                        Log.e(TAG, "Error adding to watched", e);
                        Toast.makeText(this, "Error saving", Toast.LENGTH_SHORT).show();
                    });
        } else {
            // Remove from watched
            watchedRef.delete()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Removed from Watched", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "Movie removed from watched list");
                    })
                    .addOnFailureListener(e -> {
                        buttonWatched.setSelected(true);
                        Log.e(TAG, "Error removing from watched", e);
                        Toast.makeText(this, "Error saving", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void toggleWantToWatch() {
        buttonWantToWatch.setSelected(!buttonWantToWatch.isSelected());

        DocumentReference wantRef = mUserRef.collection("wantToWatch").document(mMovieRef.getId());

        if (buttonWantToWatch.isSelected()) {
            // Add to want to watch
            wantRef.set(new MovieListItem(mCurrentMovie))
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Added to Want to Watch", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "Movie added to want to watch list");
                    })
                    .addOnFailureListener(e -> {
                        buttonWantToWatch.setSelected(false);
                        Log.e(TAG, "Error adding to want to watch", e);
                        Toast.makeText(this, "Error saving", Toast.LENGTH_SHORT).show();
                    });
        } else {
            // Remove from want to watch
            wantRef.delete()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Removed from Want to Watch", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "Movie removed from want to watch list");
                    })
                    .addOnFailureListener(e -> {
                        buttonWantToWatch.setSelected(true);
                        Log.e(TAG, "Error removing from want to watch", e);
                        Toast.makeText(this, "Error saving", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void addRating(final DocumentReference movieRef, final Rating rating) {
        final DocumentReference ratingRef = movieRef.collection("ratings").document();

        mFirestore.runTransaction(transaction -> {
            Movie movie = transaction.get(movieRef).toObject(Movie.class);
            int newNumRatings = movie.getNumRatings() + 1;
            double oldRatingTotal = movie.getAvgRating() * movie.getNumRatings();
            double newAvgRating = (oldRatingTotal + rating.getRating()) / newNumRatings;

            movie.setNumRatings(newNumRatings);
            movie.setAvgRating(newAvgRating);

            transaction.set(movieRef, movie);
            transaction.set(ratingRef, rating);
            return null;
        }).addOnSuccessListener(aVoid -> {
            Log.d(TAG, "Rating added successfully");
            hideKeyboard();
            mRatingsRecycler.smoothScrollToPosition(0);
        }).addOnFailureListener(e -> {
            Log.w(TAG, "Add rating failed", e);
            hideKeyboard();
            Snackbar.make(findViewById(android.R.id.content),
                    "Failed to add rating", Snackbar.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onEvent(DocumentSnapshot snapshot, FirebaseFirestoreException e) {
        if (e != null) {
            Log.w(TAG, "movie:onEvent", e);
            return;
        }

        mCurrentMovie = snapshot.toObject(Movie.class);
        onMovieLoaded(mCurrentMovie);
    }

    private void onMovieLoaded(Movie movie) {
        mTitleView.setText(movie.getTitle());
        mRatingIndicator.setRating((float) movie.getAvgRating());
        mNumRatingsView.setText(getString(R.string.fmt_num_ratings, movie.getNumRatings()));

        String imageUrl = ImageUtil.buildImageUrl(movie.getPosterUrl());
        if (imageUrl != null) {
            Glide.with(mImageView.getContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_movie_placeholder)
                    .error(R.drawable.ic_movie_placeholder)
                    .into(mImageView);
        } else {
            mImageView.setImageResource(R.drawable.ic_movie_placeholder);
        }
    }

    public void onBackArrowClicked(View view) {
        onBackPressed();
    }

    public void onAddRatingClicked(View view) {
        mRatingDialog.show(getSupportFragmentManager(), RatingDialogFragment.TAG);
    }

    @Override
    public void onRating(Rating rating) {
        if (FirebaseUtil.getAuth().getCurrentUser() != null) {
            Log.d(TAG, "User is signed in: " + FirebaseUtil.getAuth().getCurrentUser().getUid());
        } else {
            Log.w(TAG, "No signed-in user!");
        }

        Log.d(TAG, "Attempting to add rating for movie: " + mMovieRef.getId());
        Log.d(TAG, "Rating value: " + rating.getRating());

        addRating(mMovieRef, rating);
    }

    private void hideKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                    .hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
