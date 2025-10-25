package com.moovie.util;

import android.content.Context;

import com.moovie.model.Movie;

import java.util.Arrays;
import java.util.Locale;
import java.util.Random;

/**
 * Utilities for Movies.
 * Can be replaced later with TMDB API integration.
 */
public class MovieUtil {

    private static final String[] GENRES = {
            "Action", "Comedy", "Drama", "Fantasy",
            "Horror", "Romance", "Sci-Fi", "Thriller"
    };

    private static final String[] TITLE_WORDS = {
            "The", "Return of", "Rise of", "Attack of",
            "Legend of", "Adventures of", "Chronicles of"
    };

    private static final String[] TITLE_NAMES = {
            "Dragon", "Hero", "Galaxy", "Kingdom", "Ninja", "Mystery", "Journey", "Shadow"
    };

    private static final int MIN_YEAR = 1980;
    private static final int MAX_YEAR = 2025;

    private static final int MAX_POSTER_NUM = 10; // dummy placeholder images
    private static final String POSTER_URL_FMT = "https://storage.googleapis.com/firestorequickstarts.appspot.com/movie_%d.png";

    private static final Random random = new Random();

    /**
     * Generate a random Movie POJO (dummy data for UI/testing)
     */
    public static Movie getRandom(Context context) {
        Movie movie = new Movie();
        movie.setTitle(getRandomTitle());
        movie.setReleaseYear(MIN_YEAR + random.nextInt(MAX_YEAR - MIN_YEAR + 1));
        movie.setGenre(getRandomString(GENRES));
        movie.setPosterUrl(getRandomPosterUrl());
        movie.setNumRatings(random.nextInt(1000));
        movie.setAvgRating(1.0 + random.nextDouble() * 4.0);
        movie.setTmdbId(random.nextInt(100000)); // dummy ID, replace with real TMDB ID later
        return movie;
    }

    private static String getRandomTitle() {
        return getRandomString(TITLE_WORDS) + " " + getRandomString(TITLE_NAMES);
    }

    private static String getRandomString(String[] array) {
        return array[random.nextInt(array.length)];
    }

    private static String getRandomPosterUrl() {
        int id = random.nextInt(MAX_POSTER_NUM) + 1;
        return String.format(Locale.getDefault(), POSTER_URL_FMT, id);
    }
}