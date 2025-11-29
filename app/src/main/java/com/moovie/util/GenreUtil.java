package com.moovie.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GenreUtil {

    private static final Map<Integer, String> GENRE_MAP = new HashMap<>();

    static {
        // TMDB Genre IDs to Names
        GENRE_MAP.put(28, "Action");
        GENRE_MAP.put(12, "Adventure");
        GENRE_MAP.put(16, "Animation");
        GENRE_MAP.put(35, "Comedy");
        GENRE_MAP.put(80, "Crime");
        GENRE_MAP.put(99, "Documentary");
        GENRE_MAP.put(18, "Drama");
        GENRE_MAP.put(10751, "Family");
        GENRE_MAP.put(14, "Fantasy");
        GENRE_MAP.put(36, "History");
        GENRE_MAP.put(27, "Horror");
        GENRE_MAP.put(10402, "Music");
        GENRE_MAP.put(9648, "Mystery");
        GENRE_MAP.put(10749, "Romance");
        GENRE_MAP.put(878, "Science Fiction");
        GENRE_MAP.put(10770, "TV Movie");
        GENRE_MAP.put(53, "Thriller");
        GENRE_MAP.put(10752, "War");
        GENRE_MAP.put(37, "Western");
    }

    /**
     * Get genre name from TMDB genre ID
     */
    public static String getGenreName(int genreId) {
        return GENRE_MAP.getOrDefault(genreId, "Unknown");
    }

    /**
     * Get the first (primary) genre name from a list of genre IDs
     */
    public static String getPrimaryGenre(List<Integer> genreIds) {
        if (genreIds == null || genreIds.isEmpty()) {
            return null;
        }
        return getGenreName(genreIds.get(0));
    }

    /**
     * Get all genre names from a list of genre IDs, comma-separated
     */
    public static String getAllGenres(List<Integer> genreIds) {
        if (genreIds == null || genreIds.isEmpty()) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < genreIds.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(getGenreName(genreIds.get(i)));
        }
        return sb.toString();
    }
}