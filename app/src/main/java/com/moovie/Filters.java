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

    public Filters() {}

    /** Default filter: sort by rating descending */
    public static Filters getDefault() {
        Filters filters = new Filters();
        filters.setSortBy(Movie.FIELD_AVG_RATING);
        filters.setSortDirection(Query.Direction.DESCENDING);
        return filters;
    }

    public boolean hasGenre() {
        return !TextUtils.isEmpty(genre);
    }

    public boolean hasReleaseYear() {
        return !TextUtils.isEmpty(releaseYear);
    }

    public boolean hasSortBy() {
        return !TextUtils.isEmpty(sortBy);
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getReleaseYear() {
        return releaseYear;
    }

    public void setReleaseYear(String releaseYear) {
        this.releaseYear = releaseYear;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public Query.Direction getSortDirection() {
        return sortDirection;
    }

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
