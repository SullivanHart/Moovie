package com.moovie;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.moovie.model.Movie;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class ShowtimesActivity extends AppCompatActivity {

    private static final String TAG = "ShowtimesActivity";
    private FirebaseFirestore mFirestore;
    private ShowtimesAdapter adapter;
    private List<MovieShowtime> movies;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_showtimes);

        String theaterName = getIntent().getStringExtra("theater_name");

        TextView theaterNameTextView = findViewById(R.id.theater_name_showtimes);
        RecyclerView recyclerView = findViewById(R.id.recycler_view_showtimes);
        Button backButton = findViewById(R.id.btn_back_showtimes);

        theaterNameTextView.setText(theaterName != null ? theaterName : "Theater Showtimes");

        // Initialize Firebase
        mFirestore = FirebaseFirestore.getInstance();
        movies = new ArrayList<>();

        // Set up RecyclerView
        adapter = new ShowtimesAdapter(movies);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Set up back button
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Load movies from Firebase
        loadMoviesFromFirebase();
    }

    private void loadMoviesFromFirebase() {
        mFirestore.collection("movies")
                .limit(50) // Get up to 50 movies
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Movie> allMovies = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Movie movie = document.toObject(Movie.class);
                        if (movie != null && movie.getTitle() != null) {
                            allMovies.add(movie);
                        }
                    }
                    
                    // Randomly select 4 movies
                    List<Movie> selectedMovies = getRandomMovies(allMovies, 4);
                    
                    // Convert to MovieShowtime objects with random showtimes
                    movies.clear();
                    for (Movie movie : selectedMovies) {
                        movies.add(new MovieShowtime(movie, generateRandomShowtimes()));
                    }
                    
                    // Update adapter
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading movies", e);
                    // Fallback to placeholder movies
                    movies.clear();
                    movies.addAll(createPlaceholderMovies());
                    adapter.notifyDataSetChanged();
                });
    }

    private List<Movie> getRandomMovies(List<Movie> allMovies, int count) {
        if (allMovies.size() <= count) {
            return allMovies;
        }
        
        List<Movie> shuffled = new ArrayList<>(allMovies);
        Collections.shuffle(shuffled);
        return shuffled.subList(0, count);
    }

    private List<String> generateRandomShowtimes() {
        List<String> possibleTimes = Arrays.asList(
            "12:00 PM", "12:30 PM", "1:00 PM", "1:30 PM", "2:00 PM", "2:30 PM",
            "3:00 PM", "3:30 PM", "4:00 PM", "4:30 PM", "5:00 PM", "5:30 PM",
            "6:00 PM", "6:30 PM", "7:00 PM", "7:30 PM", "8:00 PM", "8:30 PM",
            "9:00 PM", "9:30 PM", "10:00 PM", "10:30 PM"
        );
        
        List<String> shuffled = new ArrayList<>(possibleTimes);
        Collections.shuffle(shuffled);
        
        // Return 3-5 random showtimes
        Random random = new Random();
        int numShowtimes = 3 + random.nextInt(3); // 3 to 5 showtimes
        return shuffled.subList(0, Math.min(numShowtimes, shuffled.size()));
    }

    private List<MovieShowtime> createPlaceholderMovies() {
        List<MovieShowtime> movieList = new ArrayList<>();
        
        movieList.add(new MovieShowtime(
            "The Dark Knight Returns",
            "Action, Drama",
            null,
            Arrays.asList("2:30 PM", "5:15 PM", "8:00 PM", "10:45 PM")
        ));
        
        movieList.add(new MovieShowtime(
            "Cosmic Adventure",
            "Sci-Fi, Adventure",
            null,
            Arrays.asList("1:00 PM", "4:20 PM", "7:30 PM", "10:15 PM")
        ));
        
        movieList.add(new MovieShowtime(
            "Love in Paris",
            "Romance, Comedy",
            null,
            Arrays.asList("3:45 PM", "6:30 PM", "9:15 PM")
        ));
        
        movieList.add(new MovieShowtime(
            "Mystery of the Lost City",
            "Mystery, Thriller",
            null,
            Arrays.asList("2:00 PM", "5:45 PM", "8:30 PM")
        ));
        
        return movieList;
    }

    // Inner class for movie showtime data
    public static class MovieShowtime {
        private String title;
        private String genre;
        private String posterUrl;
        private List<String> showtimes;

        public MovieShowtime(Movie movie, List<String> showtimes) {
            this.title = movie.getTitle();
            this.genre = movie.getGenre();
            this.posterUrl = movie.getPosterUrl();
            this.showtimes = showtimes;
        }

        public MovieShowtime(String title, String genre, String posterUrl, List<String> showtimes) {
            this.title = title;
            this.genre = genre;
            this.posterUrl = posterUrl;
            this.showtimes = showtimes;
        }

        // Getters
        public String getTitle() { return title; }
        public String getGenre() { return genre; }
        public String getPosterUrl() { return posterUrl; }
        public List<String> getShowtimes() { return showtimes; }
    }
}