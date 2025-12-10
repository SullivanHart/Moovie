package com.moovie.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.moovie.WantToWatchFragment;
import com.moovie.WatchedFragment;

public class WatchlistPagerAdapter extends FragmentStateAdapter {

    /**
     * Constructor for WatchlistPagerAdapter.
     * @param fragment The fragment that hosts the ViewPager2.
     */
    public WatchlistPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    /**
     * Provide a new Fragment associated with the specified position.
     * @param position The position of the fragment.
     * @return A new Fragment.
     */
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            return new WantToWatchFragment();
        } else {
            return new WatchedFragment();
        }
    }

    /**
     * Returns the total number of items in the adapter.
     * @return The total number of items.
     */
    @Override
    public int getItemCount() {
        return 2;
    }
}
