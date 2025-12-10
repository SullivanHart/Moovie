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
import com.moovie.util.ImageUtil;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;

import me.zhanghai.android.materialratingbar.MaterialRatingBar;

/**
 * RecyclerView adapter for a list of Movies.
 */
public class MovieAdapter extends FirestoreAdapter<MovieAdapter.ViewHolder> {

    /**
     * Interface to listen for movie selection events.
     */
    public interface OnMovieSelectedListener {
        /**
         * Called when a movie is selected.
         * @param movie The selected movie document.
         */
        void onMovieSelected(DocumentSnapshot movie);
    }

    private OnMovieSelectedListener mListener;

    /**
     * Constructor for MovieAdapter.
     * @param query The Firestore query to listen to.
     * @param listener The listener for movie selection events.
     */
    public MovieAdapter(Query query, OnMovieSelectedListener listener) {
        super(query);
        mListener = listener;
    }

    /**
     * Called when RecyclerView needs a new ViewHolder of the given type to represent an item.
     * @param parent The ViewGroup into which the new View will be added after it is bound to an adapter position.
     * @param viewType The view type of the new View.
     * @return A new ViewHolder that holds a View of the given view type.
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new ViewHolder(inflater.inflate(R.layout.item_movie, parent, false));
    }

    /**
     * Called by RecyclerView to display the data at the specified position.
     * @param holder The ViewHolder which should be updated to represent the contents of the item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getSnapshot(position), mListener);
    }

    /**
     * ViewHolder for movie items.
     */
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

            // Load image using ImageUtil
            String imageUrl = ImageUtil.buildImageUrl(movie.getPosterUrl());
            if (imageUrl != null) {
                Glide.with(imageView.getContext())
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_movie_placeholder)
                        .error(R.drawable.ic_movie_placeholder)
                        .into(imageView);
            } else {
                imageView.setImageResource(R.drawable.ic_movie_placeholder);
            }

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
