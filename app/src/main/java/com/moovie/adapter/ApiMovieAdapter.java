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
