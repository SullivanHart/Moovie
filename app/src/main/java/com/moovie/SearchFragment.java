package com.moovie;

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
import com.moovie.util.ApiClient;
import com.moovie.util.ApiService;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchFragment extends Fragment {

    private static final String API_KEY = "eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiI4MWRmYjlmMjBjZTkyMGU3MTlhZTI2NzlmNDIwYmZlZCIsIm5iZiI6MTc2MTM2MDI1OC42MjksInN1YiI6IjY4ZmMzOTgyNTQyMjlkYzc2M2NjZDY5OSIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.T_LnhGBaAnpkJH540MxZ_qmsiJVUNGUnWxmvhglfF_Y";

    private EditText searchInput;
    private RecyclerView recyclerView;
    private ApiMovieAdapter apiMovieAdapter;
    private ApiService apiService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_search, container, false);

        searchInput = view.findViewById(R.id.searchInput);
        recyclerView = view.findViewById(R.id.recyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        apiMovieAdapter = new ApiMovieAdapter();
        recyclerView.setAdapter(apiMovieAdapter);

        apiService = ApiClient.getClient().create(ApiService.class);

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
                Log.d( "SearchFragment", response.raw().toString() );
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
}
