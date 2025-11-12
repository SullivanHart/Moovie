package com.moovie.model;

import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.gson.annotations.SerializedName;

@IgnoreExtraProperties
public class Movie {

    public static final String FIELD_GENRE = "genre";
    public static final String FIELD_POPULARITY = "numRatings";
    public static final String FIELD_AVG_RATING = "avgRating";
    public static final String FIELD_RELEASE_YEAR = "avgRating";

    @SerializedName("title")
    private String title;

    // TMDB returns release_date as a String (like "2024-11-10")
    @SerializedName("release_date")
    private String releaseDate;

    private String genre;

    // TMDB uses poster_path
    @SerializedName("poster_path")
    private String posterUrl;

    // TMDB uses "id"
    @SerializedName("id")
    private int tmdbId;

    private int numRatings;
    private double avgRating;

    public Movie() {}

    public Movie(String title, int releaseYear, String genre, String posterUrl,
                 int tmdbId, int numRatings, double avgRating) {
        this.title = title;
        this.genre = genre;
        this.posterUrl = posterUrl;
        this.tmdbId = tmdbId;
        this.numRatings = numRatings;
        this.avgRating = avgRating;
    }

    // --- getters/setters ---

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getReleaseDate() { return releaseDate; }
    public void setReleaseDate(String releaseDate) { this.releaseDate = releaseDate; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public String getPosterUrl() { return posterUrl; }
    public void setPosterUrl(String posterUrl) { this.posterUrl = posterUrl; }

    public int getTmdbId() { return tmdbId; }
    public void setTmdbId(int tmdbId) { this.tmdbId = tmdbId; }

    public int getNumRatings() { return numRatings; }
    public void setNumRatings(int numRatings) { this.numRatings = numRatings; }

    public double getAvgRating() { return avgRating; }
    public void setAvgRating(double avgRating) { this.avgRating = avgRating; }
    public String getReleaseYear() {
        if (releaseDate != null && releaseDate.length() >= 4) {
            return releaseDate.substring(0, 4);
        } else {
            return "N/A";
        }
    }
}
