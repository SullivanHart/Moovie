package com.moovie.model.watchmode;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Model class for the WatchMode API response.
 */
public class WatchResponse {
    @SerializedName("title_results")
    public List<WatchTitle> title_results;

    /**
     * Inner class representing a title in the search results.
     */
    public static class WatchTitle {
        @SerializedName("id")
        public String id;
        @SerializedName("name")
        public String name;
        @SerializedName("type")
        public String type;
        @SerializedName("tmdb_id")
        public Integer tmdb_id;
    }
}
