package com.moovie;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.moovie.adapter.ApiMovieAdapter;
import com.moovie.model.Movie;
import com.moovie.model.TMDBResponse;
import com.moovie.util.TMDBApiClient;
import com.moovie.util.ApiService;
import com.moovie.util.FirebaseUtil;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchFragment extends Fragment implements ApiMovieAdapter.OnMovieSelectedListener {

    private static final String TAG = "SearchFragment";
    private static final String API_KEY = BuildConfig.TMDB_API_KEY_RA;

    private EditText searchInput;
    private RecyclerView recyclerView;
    private ApiMovieAdapter apiMovieAdapter;
    private ApiService apiService;
    private FirebaseFirestore mFirestore;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_search, container, false);

        searchInput = view.findViewById(R.id.searchInput);
        recyclerView = view.findViewById(R.id.recyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        // Pass 'this' as listener to handle movie selection
        apiMovieAdapter = new ApiMovieAdapter(this);
        recyclerView.setAdapter(apiMovieAdapter);

        // Initialize Firestore
        mFirestore = FirebaseUtil.getFirestore();

        apiService = TMDBApiClient.getClient().create(ApiService.class);

        // Listen for text changes in the search box
        searchInput.addTextChangedListener(new TextWatcher() {
            private long lastEditTime = 0;
            private final long delay = 500; // ms delay to avoid firing too often

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                lastEditTime = System.currentTimeMillis();
                searchInput.removeCallbacks(runSearch);
                searchInput.postDelayed(runSearch, delay);
            }

            @Override
            public void afterTextChanged(Editable s) {}

            private final Runnable runSearch = () -> {
                if (System.currentTimeMillis() - lastEditTime >= delay) {
                    String query = searchInput.getText().toString().trim();
                    if (!query.isEmpty()) {
                        searchMovies(query);
                    } else {
                        apiMovieAdapter.clearMovies();
                    }
                }
            };
        });

        return view;
    }

    private void searchMovies(String query) {
        Call<TMDBResponse> call = apiService.searchMovies(
                "Bearer " + API_KEY,
                query);

        call.enqueue(new Callback<TMDBResponse>() {
            @Override
            public void onResponse(Call<TMDBResponse> call, Response<TMDBResponse> response) {
                Log.d(TAG, response.raw().toString());
                if (response.isSuccessful() && response.body() != null) {
                    List<Movie> movies = response.body().getResults();
                    if (movies.isEmpty()) {
                        Toast.makeText(getContext(), "No movies found", Toast.LENGTH_SHORT).show();
                    } else {
                        apiMovieAdapter.setMovies(movies);
                    }
                } else {
                    Toast.makeText(getContext(), "Error fetching movies", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<TMDBResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onMovieSelected(Movie movie) {
        // Save movie to Firebase and navigate to detail activity
        saveMovieToFirebase(movie);
    }

    private void saveMovieToFirebase(Movie movie) {
        // Initialize rating fields if not set
        if (movie.getNumRatings() == 0) {
            movie.setNumRatings(0);
            movie.setAvgRating(0.0);
        }

        // Check if movie already exists by TMDB ID
        mFirestore.collection("movies")
                .whereEqualTo("tmdbId", movie.getTmdbId())
                .limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().size() > 0) {
                        // Movie already exists, open it
                        String movieId = task.getResult().getDocuments().get(0).getId();
                        openMovieDetail(movieId);
                    } else {
                        // New movie, add it to Firebase
                        mFirestore.collection("movies")
                                .add(movie)
                                .addOnSuccessListener(documentReference -> {
                                    String movieId = documentReference.getId();
                                    openMovieDetail(movieId);
                                    Log.d(TAG, "Movie saved with ID: " + movieId);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error saving movie", e);
                                    Toast.makeText(getContext(), "Error adding movie", Toast.LENGTH_SHORT).show();
                                });
                    }
                });
    }

    private void openMovieDetail(String movieId) {
        Intent intent = new Intent(getActivity(), MovieDetailActivity.class);
        intent.putExtra(MovieDetailActivity.KEY_MOVIE_ID, movieId);
        startActivity(intent);
    }
}
