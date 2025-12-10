package com.moovie.model.watchmode;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Model class for the WatchMode Title Sources API response.
 */
public class TitleSourcesResponse {
    @SerializedName("sources")
    public List<SourceItem> sources;

    /**
     * Inner class representing a single source item.
     */
    public static class SourceItem {
        @SerializedName("source_id")
        public int source_id;
        @SerializedName("name")
        public String name;
        @SerializedName("region")
        public String region;
    }
}
