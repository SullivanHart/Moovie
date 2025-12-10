package com.moovie.network;

import com.moovie.model.watchmode.TitleSourcesResponse;
import com.moovie.model.watchmode.WatchResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Interface for the WatchMode API service.
 */
public interface WMService {
    // Resolve a title by TMDB id (we’ll search by tmdb_id)
    /**
     * Searches for a title by TMDB ID.
     * @param apiKey The WatchMode API key.
     * @param searchField The field to search by (e.g., "tmdb_id").
     * @param searchValue The value to search for.
     * @param types The types of titles to search for.
     * @return A call to execute the request.
     */
    @GET("search/")
    Call<WatchResponse> searchByTmdbId(
            @Query("apiKey") String apiKey,
            @Query("search_field") String searchField,
            @Query("search_value") String searchValue,
            @Query("types") String types
    );

    // Get streaming sources for a Watchmode title_id
    /**
     * Gets the streaming sources for a specific title.
     * @param titleId The WatchMode title ID.
     * @param apiKey The WatchMode API key.
     * @return A call to execute the request.
     */
    @GET("title/{title_id}/sources/")
    Call<TitleSourcesResponse> getTitleSources(
            @Path("title_id") String titleId,
            @Query("apiKey") String apiKey
    );
}
