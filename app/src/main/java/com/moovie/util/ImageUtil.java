package com.moovie.util;

public class ImageUtil {
    public static final String IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w500";

    public static String buildImageUrl(String posterPath) {
        if (posterPath == null || posterPath.isEmpty()) {
            return null;
        }
        return IMAGE_BASE_URL + posterPath;
    }
}
