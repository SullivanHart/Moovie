package com.moovie.util;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

/**
 * Client for the WatchMode API.
 */
public class WMApiClient {
    private static final String BASE = "https://api.watchmode.com/v1/";
    private final String apiKey;
    private final OkHttpClient client = new OkHttpClient();

    /**
     * Constructor for WMApiClient.
     * @param apiKey The WatchMode API key.
     */
    public WMApiClient(String apiKey) {
        this.apiKey = apiKey;
    }

    /**
     * Executes a GET request to the WatchMode API.
     * @param endpoint The API endpoint (e.g., "search/").
     * @return The response body as a string.
     * @throws IOException If the request fails.
     */
    public String get(String endpoint) throws IOException {
        String url = BASE + endpoint + (endpoint.contains("?") ? "&" : "?") + "apiKey=" + apiKey;
        Request req = new Request.Builder().url(url).build();
        try (Response res = client.newCall(req).execute()) {
            if (!res.isSuccessful()) throw new IOException("Unexpected code " + res);
            return res.body() != null ? res.body().string() : null;
        }
    }
}
