package com.moovie.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.moovie.R;
import com.moovie.model.Movie;
import com.moovie.util.ImageUtil;

import java.util.ArrayList;
import java.util.List;

public class ApiMovieAdapter extends RecyclerView.Adapter<ApiMovieAdapter.MovieViewHolder> {

    private List<Movie> movies = new ArrayList<>();

    // Interface for click handling
    public interface OnMovieSelectedListener {
        void onMovieSelected(Movie movie);
    }

    private OnMovieSelectedListener mListener;

    // Constructor with listener
    public ApiMovieAdapter(OnMovieSelectedListener listener) {
        this.mListener = listener;
    }

    /**
     * Called when RecyclerView needs a new ViewHolder of the given type to represent an item.
     * @param parent The ViewGroup into which the new View will be added after it is bound to an adapter position.
     * @param viewType The view type of the new View.
     * @return A new ViewHolder that holds a View of the given view type.
     */
    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_api_movie, parent, false);
        return new MovieViewHolder(view);
    }

    /**
     * Called by RecyclerView to display the data at the specified position.
     * @param holder The ViewHolder which should be updated to represent the contents of the item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        Movie movie = movies.get(position);

        holder.title.setText(movie.getTitle());
        holder.year.setText(String.valueOf(movie.getReleaseYear()));

        // Load poster image using ImageUtil
        String imageUrl = ImageUtil.buildImageUrl(movie.getPosterUrl());
        if (imageUrl != null) {
            Glide.with(holder.itemView.getContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_movie_placeholder)
                    .error(R.drawable.ic_movie_placeholder)
                    .into(holder.poster);
        } else {
            holder.poster.setImageResource(R.drawable.ic_movie_placeholder);
        }

        // Add click listener
        holder.itemView.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onMovieSelected(movie);
            }
        });
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        return movies.size();
    }

    /**
     * Updates the list of movies and notifies the adapter of the change.
     * @param newMovies The new list of movies.
     */
    public void setMovies(List<Movie> newMovies) {
        this.movies.clear();
        if (newMovies != null) {
            this.movies.addAll(newMovies);
        }
        notifyDataSetChanged();
    }

    /**
     * Clears the list of movies and notifies the adapter.
     */
    public void clearMovies() {
        this.movies.clear();
        notifyDataSetChanged();
    }

    /**
     * Gets the current list of movies.
     * @return The list of movies.
     */
    public List<Movie> getMovies() {
        return movies;
    }

    static class MovieViewHolder extends RecyclerView.ViewHolder {
        ImageView poster;
        TextView title, year;

        public MovieViewHolder(@NonNull View itemView) {
            super(itemView);
            poster = itemView.findViewById(R.id.moviePoster);
            title = itemView.findViewById(R.id.movieTitle);
            year = itemView.findViewById(R.id.movieYear);
        }
    }
}
