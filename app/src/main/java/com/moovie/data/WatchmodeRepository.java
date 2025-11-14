package com.moovie.data;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.moovie.model.watchmode.Platform;
import com.moovie.model.watchmode.TitleResultsResponse;
import com.moovie.model.watchmode.TitleSourcesResponse;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Request;
import okhttp3.OkHttpClient;
import okhttp3.Response;

public class WatchmodeRepository {

    private final String apiKey;
    private final OkHttpClient http = new OkHttpClient();
    private final Gson gson = new Gson();

    // Simple in-memory cache: titleId -> platforms
    private final Map<String, List<Platform>> cache = new HashMap<>();
    private Map<Integer, String> logoCache = new HashMap<>();


    public WatchmodeRepository(String apiKey) {
        this.apiKey = apiKey;
    }

    // Resolve by TMDB id to platforms
    public void fetchPlatformsByTmdbId(int tmdbId, PlatformsCallback callback) {
        new Thread(() -> {
            try {
                String url = "https://api.watchmode.com/v1/search/?apiKey=" + apiKey +
                        "&search_field=tmdb_movie_id&search_value=" + tmdbId +
                        "&types=movie";

                String json = httpGet(url);
                TitleResultsResponse resp = gson.fromJson(json, TitleResultsResponse.class);
                if (resp == null || resp.title_results == null || resp.title_results.isEmpty()) {
                    callback.onError(new IOException("Watchmode title not found for TMDB " + tmdbId));
                    return;
                }

                String titleId = String.valueOf(resp.title_results.get(0).id);
                List<Platform> platforms = fetchSourcesForTitleId(titleId);

                // Deduplicate
                platforms = deduplicatePlatforms(platforms);

                cache.put(titleId, platforms);
                callback.onSuccess(titleId, platforms);
            } catch (Exception e) {
                callback.onError(e);
            }
        }).start();
    }

    // Resolve by known WM title_id
    public void fetchSourcesForTitleId(String titleId, PlatformsCallback callback) {
        new Thread(() -> {
            try {
                if (cache.containsKey(titleId)) {
                    callback.onSuccess(titleId, cache.get(titleId));
                    return;
                }
                List<Platform> platforms = fetchSourcesForTitleId(titleId);
                cache.put(titleId, platforms);
                callback.onSuccess(titleId, platforms);
            } catch (Exception e) {
                callback.onError(e);
            }
        }).start();
    }

    private List<Platform> deduplicatePlatforms(List<Platform> input) {
        if (input == null) return new ArrayList<>();
        // Key by platform name; you can switch to id if you prefer
        Map<String, Platform> seen = new LinkedHashMap<>();
        for (Platform p : input) {
            String key = p.getName() == null ? "" : p.getName();
            // Optional: refine key with region or id to avoid collapsing distinct regions of same service
            if (!seen.containsKey(key)) {
                seen.put(key, p);
            }
        }
        return new ArrayList<>(seen.values());
    }

    // Original helper: assumes the WM response has { "sources": [ ... ] }
    private List<Platform> fetchSourcesForTitleId(String titleId) throws IOException {
        String json = httpGet("https://api.watchmode.com/v1/title/" + titleId + "/sources/?apiKey=" + apiKey);
        // First, try object-with-sources
        try {
            Type type = new TypeToken<TitleSourcesResponse>() {}.getType();
            TitleSourcesResponse resp = gson.fromJson(json, type);
            List<Platform> list = new ArrayList<>();

            if (resp != null && resp.sources != null) {
                for (TitleSourcesResponse.SourceItem s : resp.sources) {

                    String logo = logoCache.get(s.source_id);
                    list.add(new Platform(
                            String.valueOf(s.source_id),
                            s.name,
                            logo,
                            s.region
                    ));

                }
                return list;
            }
        } catch (Exception ignore) {
            // fall through to raw array parsing
        }

        // Fallback: try parsing as a raw array of SourceItem
        Type listType = new TypeToken<List<TitleSourcesResponse.SourceItem>>() {}.getType();
        List<TitleSourcesResponse.SourceItem> items = gson.fromJson(json, listType);
        List<Platform> list = new ArrayList<>();
        if (items != null) {
            for (TitleSourcesResponse.SourceItem s : items) {
                String logo = logoCache.get(s.source_id);
                list.add(new Platform(
                        String.valueOf(s.source_id),
                        s.name,
                        logo,
                        s.region
                ));
            }
        }
        return list;
    }

    private String httpGet(String url) throws IOException {
        Request req = new Request.Builder().url(url).build();
        try (Response res = http.newCall(req).execute()) {
            String body = res.body() != null ? res.body().string() : "";
            if (!res.isSuccessful()) throw new IOException("HTTP error " + res);
            return body;
        }
    }

    public interface PlatformsCallback {
        void onSuccess(String titleId, List<Platform> platforms);
        void onError(Exception e);
    }

    public void fetchSourceLogos(PlatformsCallback platformsCallback) throws IOException {
        String url = "https://api.watchmode.com/v1/sources/?apiKey=" + apiKey;
        Log.d("WPRepo", "Sources Request: " + url );
        String json = httpGet(url);
        Log.d("WPRepo", "Sources Response: " + json );
        Type listType = new TypeToken<List<SourceLogoItem>>() {}.getType();
        List<SourceLogoItem> items = gson.fromJson(json, listType);
        for (SourceLogoItem item : items) {
            logoCache.put(item.id, item.logo_100px);
        }
    }
    public static class SourceLogoItem {
        public int id;
        public String logo_100px;
    }

}
