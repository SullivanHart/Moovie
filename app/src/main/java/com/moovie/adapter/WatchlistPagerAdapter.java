package com.moovie.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.moovie.WantToWatchFragment;
import com.moovie.WatchedFragment;

public class WatchlistPagerAdapter extends FragmentStateAdapter {

    public WatchlistPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            return new WantToWatchFragment();
        } else {
            return new WatchedFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
