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

    public PlatformAdapter(List<Platform> platforms) {
        this.platforms = platforms;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_platform, parent, false);
        return new ViewHolder(view);
    }

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

    @Override
    public int getItemCount() {
        return platforms == null ? 0 : platforms.size();
    }

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
