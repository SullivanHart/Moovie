package com.moovie.adapter;

import android.util.Log;
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
import com.google.firebase.firestore.WriteBatch;
import com.moovie.R;
import com.moovie.model.MovieListItem;
import com.moovie.util.FirebaseUtil;
import com.moovie.util.ImageUtil;

import java.util.ArrayList;
import java.util.List;

public class RankedMovieAdapter extends FirestoreAdapter<RankedMovieAdapter.ViewHolder> {

    private static final String TAG = "RankedMovieAdapter";

    public interface OnMovieSelectedListener {
        void onMovieSelected(DocumentSnapshot movie);
    }

    private final OnMovieSelectedListener mListener;
    private final List<DocumentSnapshot> items = new ArrayList<>();
    private boolean isDragging = false;

    public RankedMovieAdapter(Query query, OnMovieSelectedListener listener) {
        super(query);
        mListener = listener;
    }

    @Override
    public void onEvent(QuerySnapshot snapshots, FirebaseFirestoreException e) {
        // Handle errors
        if (e != null) {
            Log.w(TAG, "onEvent:error", e);
            return;
        }

        // Block updates during drag
        if (isDragging) {
            Log.d(TAG, "Blocking Firestore update during drag");
            return;
        }

        // DO NOT call super.onEvent() - we want to replace the entire list, not process changes incrementally
        items.clear();
        if (snapshots != null) {
            items.addAll(snapshots.getDocuments());
        }
        notifyDataSetChanged();
        onDataChanged();

        Log.d(TAG, "Updated list with " + items.size() + " items");
    }

    public void startDrag() {
        isDragging = true;
        Log.d(TAG, "Drag started - blocking updates");
    }

    public void onItemMove(int fromPosition, int toPosition) {
        if (fromPosition < 0 || fromPosition >= items.size() ||
                toPosition < 0 || toPosition >= items.size()) {
            return;
        }

        DocumentSnapshot moved = items.remove(fromPosition);
        items.add(toPosition, moved);
        notifyItemMoved(fromPosition, toPosition);
    }

    public void endDrag() {
        WriteBatch batch = FirebaseUtil.getFirestore().batch();

        for (int i = 0; i < items.size(); i++) {
            DocumentSnapshot snapshot = items.get(i);
            batch.update(snapshot.getReference(), "rankIndex", i);
        }

        batch.commit()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Batch update successful");
                    // Wait for Firestore to settle before accepting updates
                    new android.os.Handler().postDelayed(() -> {
                        isDragging = false;
                        Log.d(TAG, "Drag ended - accepting updates");
                    }, 800);
                })
                .addOnFailureListener(ex -> {
                    Log.e(TAG, "Batch update failed", ex);
                    isDragging = false;
                });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ranked_movie, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (position >= 0 && position < items.size()) {
            holder.bind(items.get(position), mListener, position);
        }
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

        void bind(DocumentSnapshot snapshot, OnMovieSelectedListener listener, int position) {
            MovieListItem movie = snapshot.toObject(MovieListItem.class);
            if (movie == null) return;

            rankIndexView.setText(String.valueOf(position + 1));
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