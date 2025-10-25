package com.moovie.model;

import com.google.firebase.firestore.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Movie {

    public static final String FIELD_GENRE = "genre";
    public static final String FIELD_POPULARITY = "numRatings";
    public static final String FIELD_AVG_RATING = "avgRating";
    public static final String FIELD_RELEASE_YEAR = "avgRating";

    private String title;
    private int releaseYear;
    private String genre;
    private String posterUrl;
    private int tmdbId;
    private int numRatings;
    private double avgRating;

    public Movie() {}

    public Movie(String title, int releaseYear, String genre, String posterUrl,
                 int tmdbId, int numRatings, double avgRating) {
        this.title = title;
        this.releaseYear = releaseYear;
        this.genre = genre;
        this.posterUrl = posterUrl;
        this.tmdbId = tmdbId;
        this.numRatings = numRatings;
        this.avgRating = avgRating;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public int getReleaseYear() { return releaseYear; }
    public void setReleaseYear(int releaseYear) { this.releaseYear = releaseYear; }

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
}
