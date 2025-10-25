package com.moovie.adapter;

import android.content.res.Resources;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;

import me.zhanghai.android.materialratingbar.MaterialRatingBar;

/**
 * RecyclerView adapter for a list of Movies.
 */
public class MovieAdapter extends FirestoreAdapter<MovieAdapter.ViewHolder> {

    public interface OnMovieSelectedListener {
        void onMovieSelected(DocumentSnapshot movie);
    }

    private OnMovieSelectedListener mListener;

    public MovieAdapter(Query query, OnMovieSelectedListener listener) {
        super(query);
        mListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new ViewHolder(inflater.inflate(R.layout.item_movie, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getSnapshot(position), mListener);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;
        TextView titleView;
        MaterialRatingBar ratingBar;
        TextView numRatingsView;
        TextView genreView;
        TextView yearView;

        public ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.movie_item_poster);
            titleView = itemView.findViewById(R.id.movie_item_title);
            ratingBar = itemView.findViewById(R.id.movie_item_rating);
            numRatingsView = itemView.findViewById(R.id.movie_item_num_ratings);
            genreView = itemView.findViewById(R.id.movie_item_genre);
            yearView = itemView.findViewById(R.id.movie_item_year);
        }

        public void bind(final DocumentSnapshot snapshot,
                         final OnMovieSelectedListener listener) {

            Movie movie = snapshot.toObject(Movie.class);
            Resources resources = itemView.getResources();

            // Load image
            Glide.with(imageView.getContext())
                    .load(movie.getPosterUrl())
                    .into(imageView);

            titleView.setText(movie.getTitle());
            ratingBar.setRating((float) movie.getAvgRating());
            numRatingsView.setText(resources.getString(R.string.fmt_num_ratings,
                    movie.getNumRatings()));
            genreView.setText(movie.getGenre());
            yearView.setText(String.valueOf(movie.getReleaseYear()));

            // Click listener
            itemView.setOnClickListener(view -> {
                if (listener != null) {
                    listener.onMovieSelected(snapshot);
                }
            });
        }
    }
}
