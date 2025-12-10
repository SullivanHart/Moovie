package com.moovie.util;

/**
 * Utility class for handling image URLs.
 */
public class ImageUtil {
    public static final String IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w500";

    /**
     * Builds a full image URL from a poster path.
     * @param posterPath The poster path from TMDB.
     * @return The full image URL, or null if the path is invalid.
     */
    public static String buildImageUrl(String posterPath) {
        if (posterPath == null || posterPath.isEmpty()) {
            return null;
        }
        return IMAGE_BASE_URL + posterPath;
    }
}
