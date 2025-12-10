package com.moovie.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Model class for the TMDB API response.
 */
public class TMDBResponse {

    @SerializedName("results")
    private List<Movie> results;

    @SerializedName("total_results")
    private int totalResults;

    /**
     * Gets the list of movies from the response.
     * @return The list of movies.
     */
    public List<Movie> getResults() {
        return results;
    }

    /**
     * Gets the total number of results.
     * @return The total number of results.
     */
    public int getTotalResults() {
        return totalResults;
    }
}
