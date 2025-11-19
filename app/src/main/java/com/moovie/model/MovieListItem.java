package com.moovie.model;

import com.google.firebase.firestore.IgnoreExtraProperties;

@IgnoreExtraProperties
public class MovieListItem {
    private int tmdbId;
    private String title;
    private String posterUrl;
    private long addedAt;
    private boolean ranked;

    public MovieListItem() {
    }

    public MovieListItem(Movie movie) {
        this.tmdbId = movie.getTmdbId();
        this.title = movie.getTitle();
        this.posterUrl = movie.getPosterUrl();
        this.addedAt = System.currentTimeMillis();
        this.ranked = false;
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
}
