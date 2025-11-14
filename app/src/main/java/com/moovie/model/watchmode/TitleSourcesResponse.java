package com.moovie.model.watchmode;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class TitleSourcesResponse {
    @SerializedName("sources")
    public List<SourceItem> sources;

    public static class SourceItem {
        @SerializedName("source_id")
        public int source_id;
        @SerializedName("name")
        public String name;
        @SerializedName("region")
        public String region;
    }
}
