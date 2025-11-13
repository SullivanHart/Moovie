package com.moovie;

// ALL YOUR OLD IMPORTS GO HERE
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
import android.widget.Filter;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.firebase.ui.auth.AuthUI;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseApp;
import com.moovie.adapter.MovieAdapter;
import com.moovie.model.Movie;
import com.moovie.util.FirebaseUtil;
import com.moovie.util.MovieUtil;
import com.moovie.viewmodel.MainActivityViewModel;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import java.util.Collections;

// 1. Implement all your old interfaces
public class HomeFragment extends Fragment implements
        View.OnClickListener,
        FilterDialogFragment.FilterListener,
        MovieAdapter.OnMovieSelectedListener {

    // 2. Copy ALL your old member variables (private Toolbar mToolbar, etc.)
    private static final String TAG = "HomeFragment"; // Renamed for clarity
    private static final int RC_SIGN_IN = 9001;
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

    // 3. This part is new: Fragments use onCreateView
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // This line inflates your RENAMED layout: fragment_home.xml
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    // 4. This is new: All your old "onCreate" code goes here
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // This tells the Fragment it has a menu
        setHasOptionsMenu(true);

        // --- PASTE YOUR OLD onCreate() CODE BELOW ---
        // (Remember to use 'view.findViewById' and 'getContext()')

        if (FirebaseApp.getApps(getContext()).isEmpty()) {
            FirebaseApp.initializeApp(getContext());
        }

        mToolbar = view.findViewById(R.id.toolbar);
        // Important: Set the toolbar for the activity
        ((AppCompatActivity)getActivity()).setSupportActionBar(mToolbar);

        mCurrentSearchView = view.findViewById(R.id.text_current_search);
        mCurrentSortByView = view.findViewById(R.id.text_current_sort_by);
        mMoviesRecycler = view.findViewById(R.id.recycler_movies);
        mEmptyView = view.findViewById(R.id.view_empty);

        view.findViewById(R.id.filter_bar).setOnClickListener(this);
        view.findViewById(R.id.button_clear_filter).setOnClickListener(this);

        mViewModel = new ViewModelProvider(this).get(MainActivityViewModel.class);

        mFirestore = FirebaseUtil.getFirestore();
        mQuery = mFirestore.collection("movies")
                .orderBy(Movie.FIELD_AVG_RATING, Query.Direction.DESCENDING)
                .limit(LIMIT);

        initRecyclerView(); // No need to pass view, we can use getView()

        mFilterDialog = new FilterDialogFragment();
        mFilterDialog.setFilterListener(this); // Set listener
    }

    // 5. Copy/Paste all your other methods (initRecyclerView, onStart, onStop, onFilter, etc.)
    //    You must change 'this' to 'getContext()' or 'getActivity()'
    //    And use 'getView()' to find views outside of onViewCreated

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

    @Override
    public void onStop() {
        super.onStop();
        if (mAdapter != null) mAdapter.stopListening();
    }

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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.filter_bar:
                // Use getParentFragmentManager() for dialogs in Fragments
                mFilterDialog.show(getParentFragmentManager(), FilterDialogFragment.TAG);
                break;
            case R.id.button_clear_filter:
                mFilterDialog.resetFilters();
                onFilter(Filters.getDefault());
                break;
        }
    }

    @Override
    public void onMovieSelected(DocumentSnapshot movie) {
        Intent intent = new Intent(getActivity(), MovieDetailActivity.class);
        intent.putExtra(MovieDetailActivity.KEY_MOVIE_ID, movie.getId());
        startActivity(intent);
    }

    private boolean shouldStartSignIn() {
        return (!mViewModel.getIsSigningIn() && FirebaseUtil.getAuth().getCurrentUser() == null);
    }
}