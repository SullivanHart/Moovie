package com.moovie.network;

import com.moovie.model.TMDBResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface TMDBService {
    @GET("search/movie")
    Call<TMDBResponse> searchMovies(
            @Query("api_key") String apiKey,
            @Query("query") String query,
            @Query("include_adult") boolean includeAdult
    );
}
