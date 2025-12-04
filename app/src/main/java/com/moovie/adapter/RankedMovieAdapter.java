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
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.moovie.R;
import com.moovie.model.MovieListItem;
import com.moovie.util.ImageUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RankedMovieAdapter extends FirestoreAdapter<RankedMovieAdapter.ViewHolder> {

    public interface OnMovieSelectedListener {
        void onMovieSelected(DocumentSnapshot movie);
    }

    private final OnMovieSelectedListener mListener;

    /** Holds current ranked movies in display order */
    private final List<DocumentSnapshot> items = new ArrayList<>();

    public RankedMovieAdapter(Query query, OnMovieSelectedListener listener) {
        super(query);
        mListener = listener;
    }

    // === IMPORTANT: this runs every time Firestore sends new data ===
    @Override
    public void onEvent(QuerySnapshot snapshots, FirebaseFirestoreException e) {
        super.onEvent(snapshots, e);

        items.clear();
        if (snapshots != null) {
            items.addAll(snapshots.getDocuments());
        }
    }

    // === Drag movement handler, swaps items locally ===
    public void onItemMove(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(items, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(items, i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);
    }

    // === Persist the new order to Firestore ===
    public void updateRankIndexes() {
        for (int i = 0; i < items.size(); i++) {
            items.get(i).getReference().update("rankIndex", i);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ranked_movie, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position), mListener);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView rankIndexView;
        ImageView imageView;
        TextView titleView;

        ViewHolder(View itemView) {
            super(itemView);
            rankIndexView = itemView.findViewById(R.id.text_rank_index);
            imageView = itemView.findViewById(R.id.image_poster);
            titleView = itemView.findViewById(R.id.text_title);
        }

        void bind(DocumentSnapshot snapshot, OnMovieSelectedListener listener) {
            MovieListItem movie = snapshot.toObject(MovieListItem.class);
            if (movie == null) return;

            rankIndexView.setText(String.valueOf(movie.getRankIndex() + 1));
            titleView.setText(movie.getTitle());

            Glide.with(imageView.getContext())
                    .load(ImageUtil.buildImageUrl(movie.getPosterUrl()))
                    .placeholder(R.drawable.ic_movie_placeholder)
                    .into(imageView);

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onMovieSelected(snapshot);
            });
        }
    }
}
