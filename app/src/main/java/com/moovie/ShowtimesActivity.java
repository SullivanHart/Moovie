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

/**
 * Activity that displays movie showtimes for a selected theater.
 * 
 * This activity:
 * - Loads movies from Firebase Firestore database
 * - Randomly selects 4 movies for the theater
 * - Generates random showtimes for each movie
 * - Displays movies with posters, genres, and available showtimes
 * - Provides fallback placeholder movies if database is unavailable
 * 
 * The movie selection is randomized per theater visit to simulate
 * different theaters having different movie selections.
 * 
 * @author Moovie Team
 * @version 1.0
 * @since 1.0
 */
public class ShowtimesActivity extends AppCompatActivity {

    /** Tag for logging */
    private static final String TAG = "ShowtimesActivity";
    
    /** Maximum number of movies to retrieve from database */
    private static final int MAX_MOVIES_QUERY = 50;
    
    /** Number of movies to display per theater */
    private static final int MOVIES_PER_THEATER = 4;
    
    /** Minimum number of showtimes per movie */
    private static final int MIN_SHOWTIMES = 3;
    
    /** Maximum number of showtimes per movie */
    private static final int MAX_SHOWTIMES = 5;

    /** Firestore database instance */
    private FirebaseFirestore mFirestore;
    
    /** RecyclerView adapter for displaying movies */
    private ShowtimesAdapter adapter;
    
    /** List of movies with showtimes to display */
    private List<MovieShowtime> movies;

    /**
     * Initializes the activity with theater showtimes display.
     * Sets up UI components and initiates movie loading from Firebase.
     * 
     * @param savedInstanceState Previous saved state, if any
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_showtimes);

        String theaterName = getIntent().getStringExtra("theater_name");
        
        initializeViews(theaterName);
        initializeFirebase();
        loadMoviesFromFirebase();
    }

    /**
     * Initializes UI components and sets up RecyclerView.
     * 
     * @param theaterName Name of the theater to display
     */
    private void initializeViews(String theaterName) {
        TextView theaterNameTextView = findViewById(R.id.theater_name_showtimes);
        RecyclerView recyclerView = findViewById(R.id.recycler_view_showtimes);
        Button backButton = findViewById(R.id.btn_back_showtimes);

        theaterNameTextView.setText(theaterName != null ? theaterName : "Theater Showtimes");

        setupRecyclerView(recyclerView);
        setupBackButton(backButton);
    }

    /**
     * Sets up the RecyclerView with adapter and layout manager.
     * 
     * @param recyclerView The RecyclerView to configure
     */
    private void setupRecyclerView(RecyclerView recyclerView) {
        movies = new ArrayList<>();
        adapter = new ShowtimesAdapter(movies);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    /**
     * Sets up the back button click listener.
     * 
     * @param backButton The back button to configure
     */
    private void setupBackButton(Button backButton) {
        backButton.setOnClickListener(v -> finish());
    }

    /**
     * Initializes Firebase Firestore instance.
     */
    private void initializeFirebase() {
        mFirestore = FirebaseFirestore.getInstance();
    }

    /**
     * Loads movies from Firebase Firestore and randomly selects movies for this theater.
     * On success, displays random movies with generated showtimes.
     * On failure, falls back to placeholder movies.
     */
    private void loadMoviesFromFirebase() {
        mFirestore.collection("movies")
                .limit(MAX_MOVIES_QUERY)
                .get()
                .addOnSuccessListener(this::handleMovieLoadSuccess)
                .addOnFailureListener(this::handleMovieLoadFailure);
    }

    /**
     * Handles successful movie loading from Firebase.
     * 
     * @param queryDocumentSnapshots The query results from Firebase
     */
    private void handleMovieLoadSuccess(com.google.firebase.firestore.QuerySnapshot queryDocumentSnapshots) {
        List<Movie> allMovies = parseMoviesFromSnapshot(queryDocumentSnapshots);
        
        if (allMovies.isEmpty()) {
            Log.w(TAG, "No movies found in database, using placeholders");
            loadPlaceholderMovies();
            return;
        }
        
        displayRandomMovies(allMovies);
    }

    /**
     * Parses Movie objects from Firebase query snapshot.
     * 
     * @param queryDocumentSnapshots The Firebase query results
     * @return List of valid Movie objects
     */
    private List<Movie> parseMoviesFromSnapshot(com.google.firebase.firestore.QuerySnapshot queryDocumentSnapshots) {
        List<Movie> allMovies = new ArrayList<>();
        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
            try {
                Movie movie = document.toObject(Movie.class);
                if (isValidMovie(movie)) {
                    allMovies.add(movie);
                }
            } catch (Exception e) {
                Log.w(TAG, "Error parsing movie document: " + document.getId(), e);
            }
        }
        return allMovies;
    }

    /**
     * Validates if a movie object has required fields.
     * 
     * @param movie The movie to validate
     * @return true if movie is valid, false otherwise
     */
    private boolean isValidMovie(Movie movie) {
        return movie != null && movie.getTitle() != null && !movie.getTitle().trim().isEmpty();
    }

    /**
     * Displays randomly selected movies with generated showtimes.
     * 
     * @param allMovies List of all available movies
     */
    private void displayRandomMovies(List<Movie> allMovies) {
        List<Movie> selectedMovies = getRandomMovies(allMovies, MOVIES_PER_THEATER);
        
        movies.clear();
        for (Movie movie : selectedMovies) {
            movies.add(new MovieShowtime(movie, generateRandomShowtimes()));
        }
        
        adapter.notifyDataSetChanged();
    }

    /**
     * Handles movie loading failure by using placeholder movies.
     * 
     * @param e The exception that occurred during loading
     */
    private void handleMovieLoadFailure(Exception e) {
        Log.e(TAG, "Error loading movies from Firebase", e);
        loadPlaceholderMovies();
    }

    /**
     * Loads placeholder movies when Firebase is unavailable.
     */
    private void loadPlaceholderMovies() {
        movies.clear();
        movies.addAll(createPlaceholderMovies());
        adapter.notifyDataSetChanged();
    }

    /**
     * Randomly selects a specified number of movies from the available list.
     * 
     * @param allMovies List of all available movies
     * @param count Number of movies to select
     * @return List of randomly selected movies
     */
    private List<Movie> getRandomMovies(List<Movie> allMovies, int count) {
        if (allMovies.size() <= count) {
            return new ArrayList<>(allMovies);
        }
        
        List<Movie> shuffled = new ArrayList<>(allMovies);
        Collections.shuffle(shuffled);
        return shuffled.subList(0, count);
    }

    /**
     * Generates random showtimes for a movie.
     * Creates 3-5 random showtimes from available time slots throughout the day.
     * 
     * @return List of random showtime strings
     */
    private List<String> generateRandomShowtimes() {
        List<String> possibleTimes = createPossibleShowtimes();
        
        Collections.shuffle(possibleTimes);
        
        Random random = new Random();
        int numShowtimes = MIN_SHOWTIMES + random.nextInt(MAX_SHOWTIMES - MIN_SHOWTIMES + 1);
        
        return possibleTimes.subList(0, Math.min(numShowtimes, possibleTimes.size()));
    }

    /**
     * Creates a list of all possible showtime slots throughout the day.
     * 
     * @return List of possible showtime strings
     */
    private List<String> createPossibleShowtimes() {
        return Arrays.asList(
            "12:00 PM", "12:30 PM", "1:00 PM", "1:30 PM", "2:00 PM", "2:30 PM",
            "3:00 PM", "3:30 PM", "4:00 PM", "4:30 PM", "5:00 PM", "5:30 PM",
            "6:00 PM", "6:30 PM", "7:00 PM", "7:30 PM", "8:00 PM", "8:30 PM",
            "9:00 PM", "9:30 PM", "10:00 PM", "10:30 PM"
        );
    }

    /**
     * Creates placeholder movies when Firebase is unavailable.
     * Provides fallback content to ensure the app remains functional.
     * 
     * @return List of placeholder MovieShowtime objects
     */
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

    /**
     * Data class representing a movie with its showtimes for display.
     * Combines movie information from the database with generated showtimes.
     */
    public static class MovieShowtime {
        private final String title;
        private final String genre;
        private final String posterUrl;
        private final List<String> showtimes;

        /**
         * Creates MovieShowtime from a Movie object and generated showtimes.
         * 
         * @param movie The movie from the database
         * @param showtimes List of showtime strings
         */
        public MovieShowtime(Movie movie, List<String> showtimes) {
            this.title = movie.getTitle();
            this.genre = movie.getGenre();
            this.posterUrl = movie.getPosterUrl();
            this.showtimes = new ArrayList<>(showtimes);
        }

        /**
         * Creates MovieShowtime with explicit values (for placeholders).
         * 
         * @param title Movie title
         * @param genre Movie genre
         * @param posterUrl URL for movie poster
         * @param showtimes List of showtime strings
         */
        public MovieShowtime(String title, String genre, String posterUrl, List<String> showtimes) {
            this.title = title;
            this.genre = genre;
            this.posterUrl = posterUrl;
            this.showtimes = new ArrayList<>(showtimes);
        }

        /** @return Movie title */
        public String getTitle() { return title; }
        
        /** @return Movie genre */
        public String getGenre() { return genre; }
        
        /** @return Movie poster URL */
        public String getPosterUrl() { return posterUrl; }
        
        /** @return List of showtimes */
        public List<String> getShowtimes() { return showtimes; }
    }
}