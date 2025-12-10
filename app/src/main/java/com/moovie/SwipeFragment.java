package com.moovie;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentReference;
import com.moovie.adapter.ApiMovieAdapter;
import com.moovie.model.Movie;
import com.moovie.model.MovieListItem;
import com.moovie.model.TMDBResponse;
import com.moovie.util.ApiService;
import com.moovie.util.FirebaseUtil;
import com.moovie.util.TMDBApiClient;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Fragment for discovering movies with swipe actions (like/dislike).
 */
public class SwipeFragment extends Fragment implements ApiMovieAdapter.OnMovieSelectedListener {

    private static final String TAG = "RandomSwipe";
    private static final String API_KEY = BuildConfig.TMDB_API_KEY_RA;

    private RecyclerView recyclerView;
    private ApiMovieAdapter adapter;

    private ApiService apiService;
    private FirebaseFirestore firestore;

    private final Random random = new Random();

    /**
     * Default constructor for SwipeFragment.
     */
    public SwipeFragment() {
        // Required empty public constructor
    }

    /**
     * Called to have the fragment instantiate its user interface view.
     *
     * @param inflater           The LayoutInflater object that can be used to inflate any views in the fragment.
     * @param container          If non-null, this is the parent view that the fragment's UI should be attached to.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state as given here.
     * @return Return the View for the fragment's UI, or null.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_swipe, container, false);

        recyclerView = view.findViewById(R.id.randomRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new ApiMovieAdapter(this);
        recyclerView.setAdapter(adapter);

        apiService = TMDBApiClient.getClient().create(ApiService.class);
        firestore = FirebaseUtil.getFirestore();

        attachSwipeListener();
        loadRandomMovie();

        return view;
    }

    // Load 1 random movie using TMDB Discover API
    private void loadRandomMovie() {
        int page = random.nextInt(50) + 1; // Random page 1–50

        Call<TMDBResponse> call = apiService.discoverMovies(
                "Bearer " + API_KEY,
                page
        );

        call.enqueue(new Callback<TMDBResponse>() {
            @Override
            public void onResponse(@NonNull Call<TMDBResponse> call,
                                   @NonNull Response<TMDBResponse> response) {

                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(getContext(), "Error fetching movie", Toast.LENGTH_SHORT).show();
                    return;
                }

                List<Movie> movies = response.body().getResults();
                if (movies == null || movies.isEmpty()) {
                    loadRandomMovie();
                    return;
                }

                // Shuffle and take the first one
                Collections.shuffle(movies);
                adapter.setMovies(Collections.singletonList(movies.get(0)));
            }

            @Override
            public void onFailure(@NonNull Call<TMDBResponse> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "API Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void attachSwipeListener() {
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT
        ) {
            @Override
            public boolean onMove(@NonNull RecyclerView rv,
                                  @NonNull RecyclerView.ViewHolder vh,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder vh, int direction) {

                Movie current = adapter.getMovies().get(0); // Only one movie visible

                if (direction == ItemTouchHelper.RIGHT) {

                    String uid = FirebaseUtil.getAuth().getUid();

                    // First: make sure the movie exists in /movies (just like toggleWatched)
                    firestore.collection("movies")
                            .whereEqualTo("tmdbId", current.getTmdbId())
                            .limit(1)
                            .get()
                            .addOnSuccessListener(task -> {

                                // Will contain Firestore doc ID for the movie
                                final String movieDocId;

                                if (task.getDocuments().size() > 0) {

                                    // Movie already exists
                                    movieDocId = task.getDocuments().get(0).getId();

                                } else {

                                    // Movie does not exist — create it
                                    movieDocId = firestore.collection("movies")
                                            .document()
                                            .getId();

                                    firestore.collection("movies")
                                            .document(movieDocId)
                                            .set(current);
                                }

                                // Now reference watched entry just like toggleWatched:
                                DocumentReference watchedRef = firestore.collection("users")
                                        .document(uid)
                                        .collection("watched")
                                        .document(movieDocId);

                                watchedRef.set(new MovieListItem(current))
                                        .addOnSuccessListener(unused -> {
                                            Toast.makeText(getContext(), "Added to Watched", Toast.LENGTH_SHORT).show();
                                            Log.d(TAG, "Movie added to watched list");
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e(TAG, "Error adding to watched", e);
                                            Toast.makeText(getContext(), "Error saving", Toast.LENGTH_SHORT).show();
                                        });
                            });
                }

                // Always load the next movie
                loadRandomMovie();
            }


        }).attachToRecyclerView(recyclerView);
    }


    /**
     * Called when a movie is selected from the list.
     *
     * @param movie The selected movie.
     */
    @Override
    public void onMovieSelected(Movie movie) {
        saveMovieToFirebase(movie);
    }

    private void saveMovieToFirebase(Movie movie) {
        if (movie.getNumRatings() == 0) {
            movie.setNumRatings(0);
            movie.setAvgRating(0.0);
        }

        firestore.collection("movies")
                .whereEqualTo("tmdbId", movie.getTmdbId())
                .limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().size() > 0) {

                        String movieId = task.getResult().getDocuments().get(0).getId();
                        openMovieDetail(movieId);

                    } else {
                        firestore.collection("movies")
                                .add(movie)
                                .addOnSuccessListener(doc -> openMovieDetail(doc.getId()))
                                .addOnFailureListener(e ->
                                        Toast.makeText(getContext(), "Error saving movie", Toast.LENGTH_SHORT).show()
                                );
                    }
                });
    }

    private void openMovieDetail(String movieId) {
        Intent intent = new Intent(getActivity(), MovieDetailActivity.class);
        intent.putExtra(MovieDetailActivity.KEY_MOVIE_ID, movieId);
        startActivity(intent);
    }
}
