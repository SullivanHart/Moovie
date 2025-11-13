package com.moovie.model.watchmode;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class WatchResponse {
    @SerializedName("title_results")
    public List<WatchTitle> title_results;

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
