package com.moovie.util;

import com.moovie.model.TMDBResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

/**
 * Interface for the API service.
 */
public interface ApiService {
    /**
     * Searches for movies based on a query string.
     * @param bearerToken The bearer token for authorization.
     * @param query The search query.
     * @return A call to execute the request.
     */
    @GET("search/movie")
    Call<TMDBResponse> searchMovies(
            @Header("Authorization") String bearerToken,
            @Query("query") String query
    );

    /**
     * Discovers movies, typically for recommendations or random selection.
     * @param auth The authorization header.
     * @param page The page number to retrieve.
     * @return A call to execute the request.
     */
    @GET("discover/movie")
    Call<TMDBResponse> discoverMovies(
            @Header("Authorization") String auth,
            @Query("page") int page
    );

}
