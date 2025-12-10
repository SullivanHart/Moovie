package com.moovie;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;
import com.moovie.adapter.PlatformAdapter;
import com.moovie.adapter.RatingAdapter;
import com.moovie.app.AppStore;
import com.moovie.data.WatchmodeRepository;
import com.moovie.model.Movie;
import com.moovie.model.MovieListItem;
import com.moovie.model.Rating;
import com.moovie.model.watchmode.Platform;
import com.moovie.util.FirebaseUtil;
import com.moovie.util.ImageUtil;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.List;

import me.zhanghai.android.materialratingbar.MaterialRatingBar;

/**
 * Activity to display details about a movie.
 */
public class MovieDetailActivity extends AppCompatActivity
        implements EventListener<DocumentSnapshot> {

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

    private RecyclerView mPlatformsRecycler;
    private ViewGroup mPlatformsContainer;

    /**
     * Called when the activity is starting.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down then this Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle).
     */
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

        mPlatformsContainer = findViewById(R.id.view_platforms);
        mPlatformsRecycler = findViewById(R.id.recycler_platforms);

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

        // Find expand headers
        View platformsHeader = findViewById(R.id.platforms_header);
        ImageView platformsExpandIcon = findViewById(R.id.platforms_expand_icon);
        View reviewsHeader = findViewById(R.id.reviews_header);
        ImageView reviewsExpandIcon = findViewById(R.id.reviews_expand_icon);

        // Expand/collapse listeners
        platformsHeader.setOnClickListener(v -> {
            boolean visible = mPlatformsRecycler.getVisibility() == View.VISIBLE;
            mPlatformsRecycler.setVisibility(visible ? View.GONE : View.VISIBLE);
            platformsExpandIcon.setImageResource(
                    visible ? R.drawable.ic_expand_more : R.drawable.ic_expand_less);
        });

        reviewsHeader.setOnClickListener(v -> {
            boolean visible = mRatingsRecycler.getVisibility() == View.VISIBLE;
            mRatingsRecycler.setVisibility(visible ? View.GONE : View.VISIBLE);
            reviewsExpandIcon.setImageResource(
                    visible ? R.drawable.ic_expand_more : R.drawable.ic_expand_less);
        });

        // Button listeners
        buttonWatched.setOnClickListener(v -> toggleWatched());
        buttonWantToWatch.setOnClickListener(v -> toggleWantToWatch());

        mRatingDialog = new RatingDialogFragment();

        findViewById(R.id.fab_show_rating_dialog).setOnClickListener(this::onAddRatingClicked);
        findViewById(R.id.movie_button_back).setOnClickListener(this::onBackArrowClicked);

        // Load user's status for this movie
        loadUserMovieStatus();
    }

    /**
     * Called when the activity is becoming visible to the user.
     */
    @Override
    public void onStart() {
        super.onStart();
        mRatingAdapter.startListening();
        mMovieRegistration = mMovieRef.addSnapshotListener(this);
    }

    /**
     * Called when the activity is no longer visible to the user.
     */
    @Override
    public void onStop() {
        super.onStop();
        mRatingAdapter.stopListening();

        if (mMovieRegistration != null) {
            mMovieRegistration.remove();
            mMovieRegistration = null;
        }
    }

    private void updateButtonAppearance(Button button, boolean isSelected) {
        if (isSelected) {
            // Darker green when selected (pressed in) with dimmed text
            button.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#1B5E20")));
            button.setTextColor(Color.parseColor("#B0B0B0")); // Dimmed gray text
        } else {
            // Standard green when not selected (raised up) with white text
            button.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#4CAF50")));
            button.setTextColor(Color.WHITE); // Bright white text
        }
        Log.d(TAG, "Button " + button.getId() + " appearance updated, selected: " + isSelected);
    }

    private void loadUserMovieStatus() {
        mUserRef.collection("watched")
                .document(mMovieRef.getId())
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        buttonWatched.setSelected(true);
                        updateButtonAppearance(buttonWatched, true);
                        Log.d(TAG, "Loaded: Movie is in watched list");
                    } else {
                        updateButtonAppearance(buttonWatched, false);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error loading watched status", e);
                    updateButtonAppearance(buttonWatched, false);
                });

        mUserRef.collection("wantToWatch")
                .document(mMovieRef.getId())
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        buttonWantToWatch.setSelected(true);
                        updateButtonAppearance(buttonWantToWatch, true);
                        Log.d(TAG, "Loaded: Movie is in want to watch list");
                    } else {
                        updateButtonAppearance(buttonWantToWatch, false);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error loading want to watch status", e);
                    updateButtonAppearance(buttonWantToWatch, false);
                });
    }

    private void toggleWatched() {
        boolean newState = !buttonWatched.isSelected();
        buttonWatched.setSelected(newState);
        updateButtonAppearance(buttonWatched, newState);

        Log.d(TAG, "toggleWatched - newState: " + newState);

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
                        updateButtonAppearance(buttonWatched, false);
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
                        updateButtonAppearance(buttonWatched, true);
                        Log.e(TAG, "Error removing from watched", e);
                        Toast.makeText(this, "Error saving", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void toggleWantToWatch() {
        boolean newState = !buttonWantToWatch.isSelected();
        buttonWantToWatch.setSelected(newState);
        updateButtonAppearance(buttonWantToWatch, newState);

        Log.d(TAG, "toggleWantToWatch - newState: " + newState);

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
                        updateButtonAppearance(buttonWantToWatch, false);
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
                        updateButtonAppearance(buttonWantToWatch, true);
                        Log.e(TAG, "Error removing from want to watch", e);
                        Toast.makeText(this, "Error saving", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    /**
     * Called when the Firestore document snapshot is available.
     * @param snapshot The document snapshot.
     * @param e The exception that occurred, if any.
     */
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
        mCurrentMovie = movie;
        mTitleView.setText(movie.getTitle());

        // CHANGED: Use avgRanking (star rating from cloud function)
        mRatingIndicator.setRating((float) movie.getAvgRanking());
        mNumRatingsView.setText(getString(R.string.fmt_num_ratings, movie.getNumRankings()));

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

        // Streaming platforms integration
        WatchmodeRepository repo = AppStore.getWatchmodeRepo(this);

        repo.fetchPlatformsByTmdbId(movie.getTmdbId(), new WatchmodeRepository.PlatformsCallback() {
            @Override
            public void onSuccess(String titleId, List<Platform> platforms) {
                runOnUiThread(() -> renderPlatforms(platforms, titleId));
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "WM platforms error", e);
                runOnUiThread(() -> {
                    mPlatformsContainer.setVisibility(View.GONE);
                });
            }
        });
    }

    private void renderPlatforms(List<Platform> platforms, String titleId) {
        if (platforms == null || platforms.isEmpty()) {
            mPlatformsContainer.setVisibility(View.GONE);
            return;
        }

        PlatformAdapter adapter = new PlatformAdapter(platforms);
        // Use grid layout (single expandable row)
        mPlatformsRecycler.setLayoutManager(new GridLayoutManager(this, 3));
        mPlatformsRecycler.setAdapter(adapter);
        mPlatformsContainer.setVisibility(View.VISIBLE);

        Log.d(TAG, "WM title_id: " + titleId + " platforms: " + platforms.size());
    }

    /**
     * Handles the click event for the back arrow.
     * @param view The view that was clicked.
     */
    public void onBackArrowClicked(View view) {
        onBackPressed();
    }

    /**
     * Handles the click event for adding a rating.
     * @param view The view that was clicked.
     */
    public void onAddRatingClicked(View view) {
        mRatingDialog.show(getSupportFragmentManager(), RatingDialogFragment.TAG);
    }

    private void hideKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                    .hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
