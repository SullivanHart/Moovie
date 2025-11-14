package com.moovie.network;

import com.moovie.model.watchmode.TitleSourcesResponse;
import com.moovie.model.watchmode.WatchResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface WMService {
    // Resolve a title by TMDB id (we’ll search by tmdb_id)
    @GET("search/")
    Call<WatchResponse> searchByTmdbId(
            @Query("apiKey") String apiKey,
            @Query("search_field") String searchField,
            @Query("search_value") String searchValue,
            @Query("types") String types
    );

    // Get streaming sources for a Watchmode title_id
    @GET("title/{title_id}/sources/")
    Call<TitleSourcesResponse> getTitleSources(
            @Path("title_id") String titleId,
            @Query("apiKey") String apiKey
    );
}
