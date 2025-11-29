package com.moovie.model;

import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.gson.annotations.SerializedName;

import java.util.List;

@IgnoreExtraProperties
public class Movie {

    public static final String FIELD_GENRE = "genre";
    public static final String FIELD_POPULARITY = "numRankings";
    public static final String FIELD_AVG_RATING = "avgRanking";
    public static final String FIELD_RELEASE_YEAR = "releaseYear";

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

    // TMDB returns genre_ids as an array of integers
    @SerializedName("genre_ids")
    private List<Integer> genreIds;

    private String releaseYear;

    // NEW: Rankings from cloud function (replaces avgRating/numRatings)
    private double avgRanking;  // Star rating (0.5 - 5.0) calculated from user rankings
    private int numRankings;     // Number of users who ranked it
    private double avgRankIndex; // Average position in users' lists
    private double percentile;   // Percentile ranking

    // OLD: Keep for backward compatibility but deprecated
    private int numRatings;
    private double avgRating;

    public Movie() {}

    public Movie(String title, String posterUrl, String releaseDate,
                 String releaseYear, int tmdbId) {
        this.title = title;
        this.posterUrl = posterUrl;
        this.releaseDate = releaseDate;
        this.releaseYear = releaseYear;
        this.tmdbId = tmdbId;
        this.avgRanking = 0;
        this.numRankings = 0;
    }

    // --- Getters/Setters ---

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getPosterUrl() {
        return posterUrl;
    }

    public void setPosterUrl(String posterUrl) {
        this.posterUrl = posterUrl;
    }

    public int getTmdbId() {
        return tmdbId;
    }

    public void setTmdbId(int tmdbId) {
        this.tmdbId = tmdbId;
    }

    public String getReleaseYear() {
        if (releaseYear != null) {
            return releaseYear;
        }
        if (releaseDate != null && releaseDate.length() >= 4) {
            return releaseDate.substring(0, 4);
        }
        return "N/A";
    }

    public void setReleaseYear(String releaseYear) {
        this.releaseYear = releaseYear;
    }

    // NEW: Primary ranking methods
    public double getAvgRanking() {
        return avgRanking;
    }

    public void setAvgRanking(double avgRanking) {
        this.avgRanking = avgRanking;
    }

    public int getNumRankings() {
        return numRankings;
    }

    public void setNumRankings(int numRankings) {
        this.numRankings = numRankings;
    }

    public double getAvgRankIndex() {
        return avgRankIndex;
    }

    public void setAvgRankIndex(double avgRankIndex) {
        this.avgRankIndex = avgRankIndex;
    }

    public double getPercentile() {
        return percentile;
    }

    public void setPercentile(double percentile) {
        this.percentile = percentile;
    }

    public List<Integer> getGenreIds() {
        return genreIds;
    }

    public void setGenreIds(List<Integer> genreIds) {
        this.genreIds = genreIds;
    }

    // BACKWARD COMPATIBILITY: Map old methods to new fields
    public int getNumRatings() {
        return numRankings; // Now returns numRankings
    }

    public double getAvgRating() {
        return avgRanking; // Now returns avgRanking (star rating)
    }

    // Keep setters for Firestore deserialization
    public void setNumRatings(int numRatings) {
        this.numRatings = numRatings;
    }

    public void setAvgRating(double avgRating) {
        this.avgRating = avgRating;
    }
}