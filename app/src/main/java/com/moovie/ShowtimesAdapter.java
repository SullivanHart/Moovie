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

public class ShowtimesAdapter extends RecyclerView.Adapter<ShowtimesAdapter.MovieViewHolder> {

    private List<ShowtimesActivity.MovieShowtime> movies;

    public ShowtimesAdapter(List<ShowtimesActivity.MovieShowtime> movies) {
        this.movies = movies;
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_movie_showtime, parent, false);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        ShowtimesActivity.MovieShowtime movie = movies.get(position);
        
        holder.titleTextView.setText(movie.getTitle());
        holder.genreTextView.setText(movie.getGenre() != null && !movie.getGenre().trim().isEmpty() ? 
            movie.getGenre() : "Genre not available");
        
        // Load movie poster using Glide
        if (movie.getPosterUrl() != null && !movie.getPosterUrl().isEmpty()) {
            String fullPosterUrl = movie.getPosterUrl().startsWith("http") ? 
                movie.getPosterUrl() : "https://image.tmdb.org/t/p/w500" + movie.getPosterUrl();
            
            Glide.with(holder.itemView.getContext())
                .load(fullPosterUrl)
                .placeholder(R.drawable.ic_movie_placeholder)
                .error(R.drawable.ic_movie_placeholder)
                .into(holder.posterImageView);
        } else {
            holder.posterImageView.setImageResource(R.drawable.ic_movie_placeholder);
        }
        
        // Clear existing buttons and add new ones in rows
        holder.showtimesContainer.removeAllViews();
        
        // Create rows for showtimes to prevent horizontal overflow
        LinearLayout currentRow = null;
        int buttonsInCurrentRow = 0;
        final int maxButtonsPerRow = 3;
        
        for (int i = 0; i < movie.getShowtimes().size(); i++) {
            String showtime = movie.getShowtimes().get(i);
            
            // Create new row if needed
            if (currentRow == null || buttonsInCurrentRow >= maxButtonsPerRow) {
                currentRow = new LinearLayout(holder.itemView.getContext());
                currentRow.setOrientation(LinearLayout.HORIZONTAL);
                LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                );
                rowParams.setMargins(0, 0, 0, 8);
                currentRow.setLayoutParams(rowParams);
                holder.showtimesContainer.addView(currentRow);
                buttonsInCurrentRow = 0;
            }
            
            Button button = new Button(holder.itemView.getContext());
            button.setText(showtime);
            button.setTextSize(11);
            button.setPadding(16, 8, 16, 8);
            
            // Set layout parameters for the button with equal weight
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1.0f
            );
            params.setMargins(0, 0, buttonsInCurrentRow < maxButtonsPerRow - 1 ? 8 : 0, 0);
            button.setLayoutParams(params);
            
            currentRow.addView(button);
            buttonsInCurrentRow++;
        }
    }

    @Override
    public int getItemCount() {
        return movies.size();
    }

    static class MovieViewHolder extends RecyclerView.ViewHolder {
        ImageView posterImageView;
        TextView titleTextView;
        TextView genreTextView;
        LinearLayout showtimesContainer;

        public MovieViewHolder(@NonNull View itemView) {
            super(itemView);
            posterImageView = itemView.findViewById(R.id.movie_poster);
            titleTextView = itemView.findViewById(R.id.movie_title);
            genreTextView = itemView.findViewById(R.id.movie_genre);
            showtimesContainer = itemView.findViewById(R.id.showtimes_container);
        }
    }
}