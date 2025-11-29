package com.moovie.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class TMDBResponse {

    @SerializedName("results")
    private List<Movie> results;

    @SerializedName("total_results")
    private int totalResults;

    public List<Movie> getResults() {
        return results;
    }

    public int getTotalResults() {
        return totalResults;
    }
}
