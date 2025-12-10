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
import com.moovie.model.watchmode.Platform;

import java.util.List;

public class PlatformAdapter extends RecyclerView.Adapter<PlatformAdapter.ViewHolder> {

    private final List<Platform> platforms;

    /**
     * Constructor for PlatformAdapter.
     * @param platforms The list of platforms to display.
     */
    public PlatformAdapter(List<Platform> platforms) {
        this.platforms = platforms;
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
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_platform, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Called by RecyclerView to display the data at the specified position.
     * @param holder The ViewHolder which should be updated to represent the contents of the item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Platform p = platforms.get(position);
        holder.name.setText(p.getName());
        if (p.getLogo() != null && !p.getLogo().isEmpty()) {
            Glide.with(holder.logo.getContext())
                    .load(p.getLogo())
                    .placeholder(R.drawable.ic_platform_placeholder)
                    .into(holder.logo);
        } else {
            holder.logo.setImageResource(R.drawable.ic_platform_placeholder);
        }
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        return platforms == null ? 0 : platforms.size();
    }

    /**
     * ViewHolder for platform items.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView logo;
        TextView name;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            logo = itemView.findViewById(R.id.platform_logo);
            name = itemView.findViewById(R.id.platform_name);
        }
    }
}
