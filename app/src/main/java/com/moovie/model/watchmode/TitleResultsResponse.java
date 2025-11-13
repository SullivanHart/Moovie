package com.moovie.model.watchmode;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class TitleResultsResponse {
    @SerializedName("title_results")
    public List<TitleResult> title_results;

    public static class TitleResult {
        @SerializedName("id")
        public long id;

        @SerializedName("name")
        public String name;

        @SerializedName("type")
        public String type;

        @SerializedName("year")
        public Integer year;

        @SerializedName("imdb_id")
        public String imdb_id;

        @SerializedName("tmdb_id")
        public Integer tmdb_id;

        @SerializedName("tmdb_type")
        public String tmdb_type;

        @SerializedName("resultType")
        public String resultType;
    }
}
