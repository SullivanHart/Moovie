package com.moovie;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.moovie.adapter.MovieListAdapter;
import com.moovie.adapter.RankedMovieAdapter;
import com.moovie.model.MovieListItem;
import com.moovie.util.FirebaseUtil;

public class RatingPageFragment extends Fragment {

    private static final String TAG = "RatingPageFragment";

    private RecyclerView mRecyclerView;
    private MovieListAdapter mUnratedAdapter;
    private RankedMovieAdapter mRankedAdapter;
    private FirebaseFirestore mFirestore;
    private Query mUnratedQuery;
    private Query mRankedQuery;

    private TabLayout mTabLayout;
    private boolean isShowingUnrated = true;
    private String userId;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_rating, container, false);

        mFirestore = FirebaseUtil.getFirestore();
        userId = FirebaseUtil.getAuth().getCurrentUser().getUid();

        mRecyclerView = root.findViewById(R.id.recycler_user_ratings);
        mTabLayout = root.findViewById(R.id.tabLayout);

        setupTabs();
        initQueries();
        initAdapters();

        // Default to Unrated
        showUnratedMovies();

        return root;
    }

    /* -------------------- SETUP -------------------- */

    private void setupTabs() {
        mTabLayout.addTab(mTabLayout.newTab().setText("Unrated"));
        mTabLayout.addTab(mTabLayout.newTab().setText("Ranked"));

        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) showUnratedMovies();
                else showRankedMovies();
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void initQueries() {
        mUnratedQuery = mFirestore.collection("users")
                .document(userId)
                .collection("watched")
                .whereEqualTo("ranked", false);

        mRankedQuery = mFirestore.collection("users")
                .document(userId)
                .collection("watched")
                .whereEqualTo("ranked", true)
                .orderBy("rankIndex", Query.Direction.ASCENDING);
    }

    private void initAdapters() {
        // Unrated
        mUnratedAdapter = new MovieListAdapter(mUnratedQuery, this::onUnratedMovieSelected) {
            @Override protected void onDataChanged() {
                if (isShowingUnrated && getItemCount() == 0) {
                    Log.d(TAG, "No unrated movies found.");
                }
            }
        };

        // Ranked
        mRankedAdapter = new RankedMovieAdapter(mRankedQuery, this::onRankedMovieSelected) {
            @Override protected void onDataChanged() {
                if (!isShowingUnrated && getItemCount() == 0) {
                    Log.d(TAG, "No ranked movies found.");
                }
            }
        };
    }

    /* -------------------- TAB HANDLERS -------------------- */

    private void showUnratedMovies() {
        isShowingUnrated = true;

        if (mRankedAdapter != null) mRankedAdapter.stopListening();
        mUnratedAdapter.startListening();

        mRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        mRecyclerView.setAdapter(mUnratedAdapter);
    }

    private void showRankedMovies() {
        isShowingUnrated = false;

        if (mUnratedAdapter != null) mUnratedAdapter.stopListening();

        mRankedAdapter.startListening();
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(mRankedAdapter);

        // === DRAG HANDLER ===
        ItemTouchHelper.Callback callback = new ItemTouchHelper.Callback() {

            @Override
            public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
                return makeMovementFlags(dragFlags, 0);
            }

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {

                int fromPos = viewHolder.getBindingAdapterPosition();
                int toPos = target.getBindingAdapterPosition();

                // Validate positions
                if (fromPos == RecyclerView.NO_POSITION || toPos == RecyclerView.NO_POSITION) {
                    return false;
                }

                mRankedAdapter.onItemMove(fromPos, toPos);
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                // No swipe functionality
            }

            @Override
            public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
                super.onSelectedChanged(viewHolder, actionState);

                if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
                    // Started dragging
                    mRankedAdapter.startDrag();
                    Log.d(TAG, "Started dragging");
                }
            }

            @Override
            public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);

                // Save and finish dragging
                mRankedAdapter.endDrag();
                Log.d(TAG, "Finished dragging");
            }
        };

        ItemTouchHelper helper = new ItemTouchHelper(callback);
        helper.attachToRecyclerView(mRecyclerView);
    }

    /* -------------------- LIFECYCLE -------------------- */

    @Override
    public void onStart() {
        super.onStart();
        if (isShowingUnrated) mUnratedAdapter.startListening();
        else mRankedAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        mUnratedAdapter.stopListening();
        mRankedAdapter.stopListening();
    }

    /* -------------------- CLICK EVENTS -------------------- */

    private void onUnratedMovieSelected(DocumentSnapshot movie) {
        MovieListItem item = movie.toObject(MovieListItem.class);
        if (item != null) {
            Intent intent = new Intent(getContext(), RankingActivity.class);
            intent.putExtra(RankingActivity.KEY_MOVIE_ID, movie.getId());
            startActivity(intent);
        }
    }

    private void onRankedMovieSelected(DocumentSnapshot movie) {
        MovieListItem item = movie.toObject(MovieListItem.class);
        if (item != null) {
            Intent intent = new Intent(getContext(), MovieDetailActivity.class);
            intent.putExtra(MovieDetailActivity.KEY_MOVIE_ID, movie.getId());
            startActivity(intent);
        }
    }
}