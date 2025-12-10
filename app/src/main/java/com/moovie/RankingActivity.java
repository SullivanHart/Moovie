package com.moovie;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;
import com.moovie.model.MovieListItem;
import com.moovie.util.FirebaseUtil;
import com.moovie.util.ImageUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity for ranking a movie against other watched movies.
 */
public class RankingActivity extends AppCompatActivity {

    private static final String TAG = "RankingActivity";
    public static final String KEY_MOVIE_ID = "key_movie_id";

    private ImageView mNewMovieImage;
    private TextView mNewMovieTitle;
    private ImageView mComparisonMovieImage;
    private TextView mComparisonMovieTitle;
    private CardView mComparisonCard;
    private TextView mQuestionText;
    private Button mButtonBetter;
    private Button mButtonWorse;

    private FirebaseFirestore mFirestore;
    private DocumentReference mNewMovieRef;
    private CollectionReference mWatchedCollection;

    private MovieListItem mNewMovie;
    private List<DocumentSnapshot> mRankedMovies = new ArrayList<>();

    // Binary Search State
    private int mLow;
    private int mHigh;
    private int mMid;

    /**
     * Called when the activity is starting.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down then this Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle).
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranking);

        mNewMovieImage = findViewById(R.id.image_new_movie);
        mNewMovieTitle = findViewById(R.id.text_new_movie_title);
        mComparisonMovieImage = findViewById(R.id.image_comparison_movie);
        mComparisonMovieTitle = findViewById(R.id.text_comparison_movie_title);
        mComparisonCard = findViewById(R.id.card_comparison_movie);
        mQuestionText = findViewById(R.id.text_comparison_question);
        mButtonBetter = findViewById(R.id.button_better);
        mButtonWorse = findViewById(R.id.button_worse);

        String movieId = getIntent().getStringExtra(KEY_MOVIE_ID);
        if (movieId == null) {
            throw new IllegalArgumentException("Must pass extra " + KEY_MOVIE_ID);
        }

        mFirestore = FirebaseUtil.getFirestore();
        String userId = FirebaseUtil.getAuth().getCurrentUser().getUid();
        mWatchedCollection = mFirestore.collection("users").document(userId).collection("watched");
        mNewMovieRef = mWatchedCollection.document(movieId);

        // Load the new movie and the list of ranked movies
        loadData();

        mButtonBetter.setOnClickListener(v -> onUserChoice(true));
        mButtonWorse.setOnClickListener(v -> onUserChoice(false));
    }

    private void loadData() {
        // 1. Get the movie to rank
        mNewMovieRef.get().addOnSuccessListener(snapshot -> {
            mNewMovie = snapshot.toObject(MovieListItem.class);
            if (mNewMovie == null) {
                finish();
                return;
            }
            displayNewMovie();

            // 2. Get all already ranked movies, ordered by rankIndex
            mWatchedCollection
                    .whereEqualTo("ranked", true)
                    .orderBy("rankIndex", Query.Direction.ASCENDING)
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        mRankedMovies = querySnapshot.getDocuments();
                        startBinarySearch();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error loading ranked movies", e);
                        Toast.makeText(this, "Error loading data", Toast.LENGTH_SHORT).show();
                        finish();
                    });
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error loading new movie", e);
            finish();
        });
    }

    private void displayNewMovie() {
        mNewMovieTitle.setText(mNewMovie.getTitle());
        String imageUrl = ImageUtil.buildImageUrl(mNewMovie.getPosterUrl());
        if (imageUrl != null) {
            Glide.with(this).load(imageUrl).into(mNewMovieImage);
        }
    }

    private void startBinarySearch() {
        if (mRankedMovies.isEmpty()) {
            // No ranked movies yet, this is the first one (index 0)
            saveRank(0);
        } else {
            mLow = 0;
            mHigh = mRankedMovies.size() - 1;
            showNextComparison();
        }
    }

    private void showNextComparison() {
        if (mLow > mHigh) {
            // Binary search complete. Insert at mLow.
            saveRank(mLow);
            return;
        }

        mMid = (mLow + mHigh) / 2;
        DocumentSnapshot comparisonSnapshot = mRankedMovies.get(mMid);
        MovieListItem comparisonMovie = comparisonSnapshot.toObject(MovieListItem.class);

        if (comparisonMovie != null) {
            mComparisonMovieTitle.setText(comparisonMovie.getTitle());
            String imageUrl = ImageUtil.buildImageUrl(comparisonMovie.getPosterUrl());
            if (imageUrl != null) {
                Glide.with(this).load(imageUrl).into(mComparisonMovieImage);
            } else {
                mComparisonMovieImage.setImageResource(R.drawable.ic_movie_placeholder);
            }
        }
    }

    private void onUserChoice(boolean isBetter) {
        // "Better" means it should have a LOWER index (closer to 0)
        // "Worse" means it should have a HIGHER index
        
        if (isBetter) {
            // New movie is better than mid. It belongs in the upper half (lower indices).
            // So we eliminate the bottom half (higher indices).
            mHigh = mMid - 1;
        } else {
            // New movie is worse than mid. It belongs in the lower half (higher indices).
            // So we eliminate the upper half (lower indices).
            mLow = mMid + 1;
        }
        showNextComparison();
    }

    private void saveRank(int newIndex) {
        WriteBatch batch = mFirestore.batch();

        // 1. Shift existing movies down if necessary
        // Any movie with rankIndex >= newIndex needs to be incremented
        for (DocumentSnapshot doc : mRankedMovies) {
            MovieListItem item = doc.toObject(MovieListItem.class);
            if (item != null && item.getRankIndex() >= newIndex) {
                batch.update(doc.getReference(), "rankIndex", item.getRankIndex() + 1);
            }
        }

        // 2. Update the new movie
        batch.update(mNewMovieRef, 
                "ranked", true,
                "rankIndex", newIndex);

        // 3. Commit
        batch.commit().addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "Ranking saved!", Toast.LENGTH_SHORT).show();
            finish();
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error saving rank", e);
            Toast.makeText(this, "Failed to save rank", Toast.LENGTH_SHORT).show();
        });
    }
}
