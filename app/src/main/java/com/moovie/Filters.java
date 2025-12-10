/**
 * Copyright 2017 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.moovie;

import android.content.Context;
import android.text.TextUtils;

import com.moovie.model.Movie;
import com.google.firebase.firestore.Query;

/**
 * Object for passing movie filters around.
 */
public class Filters {

    private String genre = null;
    private String releaseYear = null;
    private String sortBy = null;
    private Query.Direction sortDirection = null;

    /**
     * Default constructor for Filters.
     */
    public Filters() {}

    /** Default filter: sort by rating descending */
    public static Filters getDefault() {
        Filters filters = new Filters();
        filters.setSortBy(Movie.FIELD_AVG_RATING);
        filters.setSortDirection(Query.Direction.DESCENDING);
        return filters;
    }

    /**
     * Checks if a genre filter is set.
     *
     * @return true if genre is not empty, false otherwise.
     */
    public boolean hasGenre() {
        return !TextUtils.isEmpty(genre);
    }

    /**
     * Checks if a release year filter is set.
     *
     * @return true if release year is not empty, false otherwise.
     */
    public boolean hasReleaseYear() {
        return !TextUtils.isEmpty(releaseYear);
    }

    /**
     * Checks if a sort by criteria is set.
     *
     * @return true if sort by is not empty, false otherwise.
     */
    public boolean hasSortBy() {
        return !TextUtils.isEmpty(sortBy);
    }

    /**
     * Gets the genre filter.
     *
     * @return The genre.
     */
    public String getGenre() {
        return genre;
    }

    /**
     * Sets the genre filter.
     *
     * @param genre The genre to set.
     */
    public void setGenre(String genre) {
        this.genre = genre;
    }

    /**
     * Gets the release year filter.
     *
     * @return The release year.
     */
    public String getReleaseYear() {
        return releaseYear;
    }

    /**
     * Sets the release year filter.
     *
     * @param releaseYear The release year to set.
     */
    public void setReleaseYear(String releaseYear) {
        this.releaseYear = releaseYear;
    }

    /**
     * Gets the sort by criteria.
     *
     * @return The sort by field name.
     */
    public String getSortBy() {
        return sortBy;
    }

    /**
     * Sets the sort by criteria.
     *
     * @param sortBy The field name to sort by.
     */
    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    /**
     * Gets the sort direction.
     *
     * @return The sort direction (ASCENDING or DESCENDING).
     */
    public Query.Direction getSortDirection() {
        return sortDirection;
    }

    /**
     * Sets the sort direction.
     *
     * @param sortDirection The sort direction to set.
     */
    public void setSortDirection(Query.Direction sortDirection) {
        this.sortDirection = sortDirection;
    }

    /** Returns a description of the selected filters (for UI display) */
    public String getSearchDescription(Context context) {
        StringBuilder desc = new StringBuilder();

        if (genre == null && releaseYear == null) {
            desc.append("<b>");
            desc.append(context.getString(R.string.all_movies));
            desc.append("</b>");
        }

        if (genre != null) {
            desc.append("<b>");
            desc.append(genre);
            desc.append("</b>");
        }

        if (genre != null && releaseYear != null) {
            desc.append(" from ");
        }

        if (releaseYear != null) {
            desc.append("<b>");
            desc.append(releaseYear);
            desc.append("</b>");
        }

        return desc.toString();
    }

    /** Returns a description of the current sort order */
    public String getOrderDescription(Context context) {
        if (Movie.FIELD_RELEASE_YEAR.equals(sortBy)) {
            return context.getString(R.string.sorted_by_release_year);
        } else if (Movie.FIELD_POPULARITY.equals(sortBy)) {
            return context.getString(R.string.sorted_by_popularity);
        } else {
            return context.getString(R.string.sorted_by_rating);
        }
    }
}
