package com.moovie.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.moovie.R;
import com.moovie.model.MovieListItem;
import com.moovie.util.ImageUtil;

public class RankedMovieAdapter extends FirestoreAdapter<RankedMovieAdapter.ViewHolder> {

    public interface OnMovieSelectedListener {
        void onMovieSelected(DocumentSnapshot movie);
    }

    private OnMovieSelectedListener mListener;

    public RankedMovieAdapter(Query query, OnMovieSelectedListener listener) {
        super(query);
        mListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new ViewHolder(inflater.inflate(R.layout.item_ranked_movie, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getSnapshot(position), mListener);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView rankIndexView;
        ImageView imageView;
        TextView titleView;

        public ViewHolder(View itemView) {
            super(itemView);
            rankIndexView = itemView.findViewById(R.id.text_rank_index);
            imageView = itemView.findViewById(R.id.image_poster);
            titleView = itemView.findViewById(R.id.text_title);
        }

        public void bind(final DocumentSnapshot snapshot,
                         final OnMovieSelectedListener listener) {

            MovieListItem movie = snapshot.toObject(MovieListItem.class);
            if (movie == null) return;

            rankIndexView.setText(String.valueOf(movie.getRankIndex() + 1)); // 1-based rank
            titleView.setText(movie.getTitle());

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

            itemView.setOnClickListener(view -> {
                if (listener != null) {
                    listener.onMovieSelected(snapshot);
                }
            });
        }
    }
}
