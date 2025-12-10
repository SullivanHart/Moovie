package com.moovie.network;

import com.moovie.model.TMDBResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Interface for the TMDB API service.
 */
public interface TMDBService {
    /**
     * Searches for movies on TMDB.
     * @param apiKey The TMDB API key.
     * @param query The search query.
     * @param includeAdult Whether to include adult content in the results.
     * @return A call to execute the request.
     */
    @GET("search/movie")
    Call<TMDBResponse> searchMovies(
            @Query("api_key") String apiKey,
            @Query("query") String query,
            @Query("include_adult") boolean includeAdult
    );

    /**
     * Gets total movies from discover endpoint to calculate statistics.
     * @param apiKey The TMDB API key.
     * @return A call to execute the request.
     */
    @GET("discover/movie")
    Call<TMDBResponse> getTotalMovies(
            @Query("api_key") String apiKey
    );

}
