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
import com.moovie.util.ImageUtil;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;

public class MovieListAdapter extends FirestoreAdapter<MovieListAdapter.ViewHolder> {

    public interface OnMovieSelectedListener {
        void onMovieSelected(DocumentSnapshot movie);
    }

    private OnMovieSelectedListener mListener;

    public MovieListAdapter(Query query, OnMovieSelectedListener listener) {
        super(query);
        mListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new ViewHolder(inflater.inflate(R.layout.item_movie_list, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getSnapshot(position), mListener);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;
        TextView titleView;

        public ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.movie_list_poster);
            titleView = itemView.findViewById(R.id.movie_list_title);
        }

        public void bind(final DocumentSnapshot snapshot,
                         final OnMovieSelectedListener listener) {

            String title = snapshot.getString("title");
            String posterUrl = snapshot.getString("posterUrl");

            titleView.setText(title);

            String imageUrl = ImageUtil.buildImageUrl(posterUrl);
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
