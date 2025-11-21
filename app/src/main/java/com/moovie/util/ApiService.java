package com.moovie.util;

import com.moovie.model.TMDBResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public interface ApiService {
    @GET("search/movie")
    Call<TMDBResponse> searchMovies(
            @Header("Authorization") String bearerToken,
            @Query("query") String query
    );

    @GET("discover/movie")
    Call<TMDBResponse> discoverMovies(
            @Header("Authorization") String auth,
            @Query("page") int page
    );

}
