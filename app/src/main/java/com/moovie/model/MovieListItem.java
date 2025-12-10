package com.moovie.model;

import com.google.firebase.firestore.IgnoreExtraProperties;
import com.moovie.util.GenreUtil;

/**
 * Model class for a movie item in a list (e.g., watchlist, favorites).
 */
@IgnoreExtraProperties
public class MovieListItem {
    private int tmdbId;
    private String title;
    private String posterUrl;
    private long addedAt;
    private boolean ranked;
    private int rankIndex;
    private String genre; // Add genre field

    /**
     * Default constructor for MovieListItem.
     */
    public MovieListItem() {
    }

    /**
     * Constructor that creates a MovieListItem from a Movie object.
     * @param movie The Movie object to create the list item from.
     */
    public MovieListItem(Movie movie) {
        this.tmdbId = movie.getTmdbId();
        this.title = movie.getTitle();
        this.posterUrl = movie.getPosterUrl();

        // Debug logging
        android.util.Log.d("MovieListItem", "Creating MovieListItem for: " + movie.getTitle());
        android.util.Log.d("MovieListItem", "  genre field: " + movie.getGenre());
        android.util.Log.d("MovieListItem", "  genre_ids: " + movie.getGenreIds());

        // Convert genre_ids to genre name
        if (movie.getGenreIds() != null && !movie.getGenreIds().isEmpty()) {
            this.genre = GenreUtil.getPrimaryGenre(movie.getGenreIds());
            android.util.Log.d("MovieListItem", "  Converted to genre: " + this.genre);
        } else if (movie.getGenre() != null) {
            // Fallback if genre is already set
            this.genre = movie.getGenre();
            android.util.Log.d("MovieListItem", "  Using existing genre: " + this.genre);
        } else {
            android.util.Log.d("MovieListItem", "  No genre data available!");
        }

        this.addedAt = System.currentTimeMillis();
        this.ranked = false;
        this.rankIndex = -1;
    }

    public int getTmdbId() { return tmdbId; }
    public void setTmdbId(int tmdbId) { this.tmdbId = tmdbId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getPosterUrl() { return posterUrl; }
    public void setPosterUrl(String posterUrl) { this.posterUrl = posterUrl; }

    public long getAddedAt() { return addedAt; }
    public void setAddedAt(long addedAt) { this.addedAt = addedAt; }

    public boolean isRanked() { return ranked; }
    public void setRanked(boolean ranked) { this.ranked = ranked; }

    public int getRankIndex() { return rankIndex; }
    public void setRankIndex(int rankIndex) { this.rankIndex = rankIndex; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }
}
