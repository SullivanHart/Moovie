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

import java.util.ArrayList;
import java.util.List;

public class ApiMovieAdapter extends RecyclerView.Adapter<ApiMovieAdapter.MovieViewHolder> {

    private List<Movie> movies = new ArrayList<>();
    private static final String IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w500";

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_api_movie, parent, false);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        Movie movie = movies.get(position);

        holder.title.setText(movie.getTitle());
        holder.year.setText(String.valueOf(movie.getReleaseYear()));

        // Load poster if available
        if (movie.getPosterUrl() != null && !movie.getPosterUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(IMAGE_BASE_URL + movie.getPosterUrl())
                    .placeholder(R.drawable.ic_movie_placeholder)
                    .into(holder.poster);
        } else {
            holder.poster.setImageResource(R.drawable.ic_movie_placeholder);
        }
    }

    @Override
    public int getItemCount() {
        return movies.size();
    }

    public void setMovies(List<Movie> newMovies) {
        this.movies.clear();
        if (newMovies != null) {
            this.movies.addAll(newMovies);
        }
        notifyDataSetChanged();
    }

    public void clearMovies() {
        this.movies.clear();
        notifyDataSetChanged();
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
