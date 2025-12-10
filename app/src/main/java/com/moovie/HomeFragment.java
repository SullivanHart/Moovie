package com.moovie;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseApp;
import com.moovie.adapter.MovieAdapter;
import com.moovie.model.Movie;
import com.moovie.util.FirebaseUtil;
import com.moovie.viewmodel.MainActivityViewModel;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;

public class HomeFragment extends Fragment implements
        View.OnClickListener,
        FilterDialogFragment.FilterListener,
        MovieAdapter.OnMovieSelectedListener {

    private static final String TAG = "HomeFragment";
    private static final int LIMIT = 50;

    private Toolbar mToolbar;
    private TextView mCurrentSearchView;
    private TextView mCurrentSortByView;
    private RecyclerView mMoviesRecycler;
    private ViewGroup mEmptyView;
    private FirebaseFirestore mFirestore;
    private Query mQuery;
    private FilterDialogFragment mFilterDialog;
    private MovieAdapter mAdapter;
    private MainActivityViewModel mViewModel;

    /**
     * Called to have the fragment instantiate its user interface view.
     *
     * @param inflater           The LayoutInflater object that can be used to inflate any views in the fragment.
     * @param container          If non-null, this is the parent view that the fragment's UI should be attached to.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state as given here.
     * @return Return the View for the fragment's UI, or null.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    /**
     * Called immediately after onCreateView has returned, but before any saved state has been restored in to the view.
     *
     * @param view               The View returned by onCreateView.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state as given here.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setHasOptionsMenu(true);

        if (FirebaseApp.getApps(getContext()).isEmpty()) {
            FirebaseApp.initializeApp(getContext());
        }

        mToolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity)getActivity()).setSupportActionBar(mToolbar);

        mCurrentSearchView = view.findViewById(R.id.text_current_search);
        mCurrentSortByView = view.findViewById(R.id.text_current_sort_by);
        mMoviesRecycler = view.findViewById(R.id.recycler_movies);
        mEmptyView = view.findViewById(R.id.view_empty);

        view.findViewById(R.id.filter_bar).setOnClickListener(this);
        view.findViewById(R.id.button_clear_filter).setOnClickListener(this);

        mViewModel = new ViewModelProvider(this).get(MainActivityViewModel.class);

        mFirestore = FirebaseUtil.getFirestore();

        // Only load movies that have at least 1 review
        mQuery = mFirestore.collection("movies")
                .whereGreaterThanOrEqualTo("numRatings", 1)                // Movies with 1+ reviews
                .orderBy("numRatings", Query.Direction.DESCENDING)         // REQUIRED first orderBy
                .orderBy(Movie.FIELD_AVG_RATING, Query.Direction.DESCENDING)
                .limit(LIMIT);

        initRecyclerView();

        mFilterDialog = new FilterDialogFragment();
        mFilterDialog.setFilterListener(this);
    }

    private void initRecyclerView() {
        if (mQuery == null) {
            Log.w(TAG, "No query, not initializing RecyclerView");
            return;
        }

        mAdapter = new MovieAdapter(mQuery, this) {
            @Override
            protected void onDataChanged() {
                if (getItemCount() == 0) {
                    mMoviesRecycler.setVisibility(View.GONE);
                    mEmptyView.setVisibility(View.VISIBLE);
                } else {
                    mMoviesRecycler.setVisibility(View.VISIBLE);
                    mEmptyView.setVisibility(View.GONE);
                }
            }

            @Override
            protected void onError(FirebaseFirestoreException e) {
                if (getView() != null) {
                    Snackbar.make(getView(), "Error: check logs.", Snackbar.LENGTH_LONG).show();
                }
            }
        };

        mMoviesRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        mMoviesRecycler.setAdapter(mAdapter);
    }

    /**
     * Called when the Fragment is visible to the user.
     */
    @Override
    public void onStart() {
        super.onStart();
        if (shouldStartSignIn()) {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            return;
        }
        onFilter(mViewModel.getFilters());
        if (mAdapter != null) mAdapter.startListening();
    }

    /**
     * Called when the Fragment is no longer started.
     */
    @Override
    public void onStop() {
        super.onStop();
        if (mAdapter != null) mAdapter.stopListening();
    }

    /**
     * Called when filters are applied.
     *
     * @param filters The selected filters.
     */
    @Override
    public void onFilter(Filters filters) {
        Query query = mFirestore.collection("movies");
        if (filters.hasGenre()) query = query.whereEqualTo("genre", filters.getGenre());
        if (filters.hasReleaseYear()) query = query.whereEqualTo("releaseYear", filters.getReleaseYear());
        if (filters.hasSortBy()) query = query.orderBy(filters.getSortBy(), filters.getSortDirection());
        query = query.limit(LIMIT);

        mQuery = query;
        mAdapter.setQuery(query);

        mCurrentSearchView.setText(Html.fromHtml(filters.getSearchDescription(getContext())));
        mCurrentSortByView.setText(filters.getOrderDescription(getContext()));
        mViewModel.setFilters(filters);
    }

    /**
     * Initialize the contents of the Fragment host's standard options menu.
     *
     * @param menu     The options menu in which you place your items.
     * @param inflater The MenuInflater object.
     */
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    /**
     * This hook is called whenever an item in your options menu is selected.
     *
     * @param item The menu item that was selected.
     * @return boolean Return false to allow normal menu processing to proceed, true to consume it here.
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_search) {
            startActivity(new Intent(getActivity(), SearchActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.filter_bar:
                mFilterDialog.show(getParentFragmentManager(), FilterDialogFragment.TAG);
                break;
            case R.id.button_clear_filter:
                mFilterDialog.resetFilters();
                onFilter(Filters.getDefault());
                break;
        }
    }

    /**
     * Called when a movie is selected from the list.
     *
     * @param movie The selected movie snapshot.
     */
    @Override
    public void onMovieSelected(DocumentSnapshot movie) {
        Intent intent = new Intent(getActivity(), MovieDetailActivity.class);
        intent.putExtra(MovieDetailActivity.KEY_MOVIE_ID, movie.getId());
        startActivity(intent);
    }

    private boolean shouldStartSignIn() {
        return FirebaseUtil.getAuth().getCurrentUser() == null;
    }
}
