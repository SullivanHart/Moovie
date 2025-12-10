package com.moovie;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

/**
 * RecyclerView adapter for displaying movie showtimes in a theater.
 * 
 * This adapter handles:
 * - Displaying movie posters using Glide image loading
 * - Showing movie titles and genres
 * - Creating interactive showtime buttons in organized rows
 * - Handling different screen sizes with responsive layout
 * 
 * Each movie item shows:
 * - Movie poster (loaded from TMDB URLs)
 * - Movie title and genre information
 * - Showtimes arranged in rows of up to 3 buttons each
 * 
 * @author Moovie Team
 * @version 1.0
 * @since 1.0
 */
public class ShowtimesAdapter extends RecyclerView.Adapter<ShowtimesAdapter.MovieViewHolder> {

    /** Maximum number of showtime buttons per row */
    private static final int MAX_BUTTONS_PER_ROW = 3;
    
    /** Base URL for TMDB movie posters */
    private static final String TMDB_IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w500";

    /** List of movies with showtimes to display */
    private final List<ShowtimesActivity.MovieShowtime> movies;

    /**
     * Creates a new ShowtimesAdapter with the provided movie list.
     * 
     * @param movies List of MovieShowtime objects to display
     */
    public ShowtimesAdapter(List<ShowtimesActivity.MovieShowtime> movies) {
        this.movies = movies;
    }

    /**
     * Creates a new ViewHolder for movie showtime items.
     * 
     * @param parent The ViewGroup into which the new View will be added
     * @param viewType The view type of the new View
     * @return A new MovieViewHolder that holds a View of the given view type
     */
    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_movie_showtime, parent, false);
        return new MovieViewHolder(view);
    }

    /**
     * Binds movie data to the ViewHolder for display.
     * 
     * @param holder The ViewHolder which should be updated
     * @param position The position of the item within the adapter's data set
     */
    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        ShowtimesActivity.MovieShowtime movie = movies.get(position);
        
        bindMovieInfo(holder, movie);
        loadMoviePoster(holder, movie);
        createShowtimeButtons(holder, movie);
    }

    /**
     * Binds basic movie information to text views.
     * 
     * @param holder The ViewHolder containing the views
     * @param movie The movie data to bind
     */
    private void bindMovieInfo(MovieViewHolder holder, ShowtimesActivity.MovieShowtime movie) {
        holder.titleTextView.setText(movie.getTitle());
        
        String genre = movie.getGenre();
        holder.genreTextView.setText(
            (genre != null && !genre.trim().isEmpty()) ? genre : "Genre not available"
        );
    }

    /**
     * Loads movie poster using Glide image loading library.
     * 
     * @param holder The ViewHolder containing the ImageView
     * @param movie The movie containing poster URL
     */
    private void loadMoviePoster(MovieViewHolder holder, ShowtimesActivity.MovieShowtime movie) {
        String posterUrl = movie.getPosterUrl();
        
        if (posterUrl != null && !posterUrl.isEmpty()) {
            String fullPosterUrl = buildFullPosterUrl(posterUrl);
            
            Glide.with(holder.itemView.getContext())
                .load(fullPosterUrl)
                .placeholder(R.drawable.ic_movie_placeholder)
                .error(R.drawable.ic_movie_placeholder)
                .into(holder.posterImageView);
        } else {
            holder.posterImageView.setImageResource(R.drawable.ic_movie_placeholder);
        }
    }

    /**
     * Builds the full poster URL from the movie's poster path.
     * 
     * @param posterUrl The poster URL or path from the movie
     * @return Complete URL for loading the poster
     */
    private String buildFullPosterUrl(String posterUrl) {
        return posterUrl.startsWith("http") ? posterUrl : TMDB_IMAGE_BASE_URL + posterUrl;
    }

    /**
     * Creates showtime buttons arranged in rows for better layout.
     * 
     * @param holder The ViewHolder containing the container
     * @param movie The movie containing showtimes
     */
    private void createShowtimeButtons(MovieViewHolder holder, ShowtimesActivity.MovieShowtime movie) {
        holder.showtimesContainer.removeAllViews();
        
        LinearLayout currentRow = null;
        int buttonsInCurrentRow = 0;
        
        for (int i = 0; i < movie.getShowtimes().size(); i++) {
            String showtime = movie.getShowtimes().get(i);
            
            if (needsNewRow(currentRow, buttonsInCurrentRow)) {
                currentRow = createNewRow(holder);
                buttonsInCurrentRow = 0;
            }
            
            Button button = createShowtimeButton(showtime, buttonsInCurrentRow);
            currentRow.addView(button);
            buttonsInCurrentRow++;
        }
    }

    /**
     * Determines if a new row is needed for showtime buttons.
     * 
     * @param currentRow The current row container
     * @param buttonsInCurrentRow Number of buttons in current row
     * @return true if a new row is needed
     */
    private boolean needsNewRow(LinearLayout currentRow, int buttonsInCurrentRow) {
        return currentRow == null || buttonsInCurrentRow >= MAX_BUTTONS_PER_ROW;
    }

    /**
     * Creates a new row container for showtime buttons.
     * 
     * @param holder The ViewHolder containing the parent container
     * @return New LinearLayout for the row
     */
    private LinearLayout createNewRow(MovieViewHolder holder) {
        LinearLayout row = new LinearLayout(holder.itemView.getContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        rowParams.setMargins(0, 0, 0, 8);
        row.setLayoutParams(rowParams);
        
        holder.showtimesContainer.addView(row);
        return row;
    }

    /**
     * Creates a showtime button with proper styling and layout.
     * 
     * @param showtime The showtime text to display
     * @param buttonIndex The index of the button in its row
     * @return Configured Button for the showtime
     */
    private Button createShowtimeButton(String showtime, int buttonIndex) {
        Button button = new Button(null);
        button.setText(showtime);
        button.setTextSize(11);
        button.setPadding(16, 8, 16, 8);
        
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1.0f
        );
        
        // Add margin except for the last button in the row
        boolean isLastInRow = buttonIndex >= MAX_BUTTONS_PER_ROW - 1;
        params.setMargins(0, 0, isLastInRow ? 0 : 8, 0);
        button.setLayoutParams(params);
        
        return button;
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     * 
     * @return The total number of items in this adapter
     */
    @Override
    public int getItemCount() {
        return movies.size();
    }

    /**
     * ViewHolder class for movie showtime items.
     * Holds references to all views in the movie item layout.
     */
    static class MovieViewHolder extends RecyclerView.ViewHolder {
        /** ImageView for displaying movie poster */
        final ImageView posterImageView;
        
        /** TextView for displaying movie title */
        final TextView titleTextView;
        
        /** TextView for displaying movie genre */
        final TextView genreTextView;
        
        /** Container for showtime buttons */
        final LinearLayout showtimesContainer;

        /**
         * Creates a new MovieViewHolder with view references.
         * 
         * @param itemView The item view containing all child views
         */
        public MovieViewHolder(@NonNull View itemView) {
            super(itemView);
            posterImageView = itemView.findViewById(R.id.movie_poster);
            titleTextView = itemView.findViewById(R.id.movie_title);
            genreTextView = itemView.findViewById(R.id.movie_genre);
            showtimesContainer = itemView.findViewById(R.id.showtimes_container);
        }
    }
}