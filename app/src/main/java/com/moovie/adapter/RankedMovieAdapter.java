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

    private final OnMovieSelectedListener mListener;
    private final List<DocumentSnapshot> items = new ArrayList<>();
    private boolean isDragging = false;

    /**
     * Constructor for RankedMovieAdapter.
     * @param query The Firestore query to listen to.
     * @param listener The listener for movie selection events.
     */
    public RankedMovieAdapter(Query query, OnMovieSelectedListener listener) {
        super(query);
        mListener = listener;
    }

    /**
     * Called when the Firestore query returns a snapshot.
     * @param snapshots The new query snapshot.
     * @param e The exception that occurred, if any.
     */
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

    /**
     * Starts a drag operation, blocking Firestore updates.
     */
    public void startDrag() {
        isDragging = true;
        Log.d(TAG, "Drag started - blocking updates");
    }

    /**
     * Moves an item from one position to another.
     * @param fromPosition The starting position.
     * @param toPosition The destination position.
     */
    public void onItemMove(int fromPosition, int toPosition) {
        if (fromPosition < 0 || fromPosition >= items.size() ||
                toPosition < 0 || toPosition >= items.size()) {
            return;
        }

        DocumentSnapshot moved = items.remove(fromPosition);
        items.add(toPosition, moved);
        notifyItemMoved(fromPosition, toPosition);
    }

    /**
     * Ends a drag operation, updating the order in Firestore.
     */
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

    /**
     * Returns the total number of items in the data set held by the adapter.
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        return items.size();
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
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ranked_movie, parent, false));
    }

    /**
     * Called by RecyclerView to display the data at the specified position.
     * @param holder The ViewHolder which should be updated to represent the contents of the item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (position >= 0 && position < items.size()) {
            holder.bind(items.get(position), mListener, position);
        }
    }

    /**
     * ViewHolder for ranked movie items.
     */
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
