package com.moovie.util;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

public class WMApiClient {
    private static final String BASE = "https://api.watchmode.com/v1/";
    private final String apiKey;
    private final OkHttpClient client = new OkHttpClient();

    public WMApiClient(String apiKey) {
        this.apiKey = apiKey;
    }

    public String get(String endpoint) throws IOException {
        String url = BASE + endpoint + (endpoint.contains("?") ? "&" : "?") + "apiKey=" + apiKey;
        Request req = new Request.Builder().url(url).build();
        try (Response res = client.newCall(req).execute()) {
            if (!res.isSuccessful()) throw new IOException("Unexpected code " + res);
            return res.body() != null ? res.body().string() : null;
        }
    }
}
