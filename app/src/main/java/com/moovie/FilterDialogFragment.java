package com.moovie;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.moovie.model.Movie;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Dialog Fragment containing movie filter form.
 */
public class FilterDialogFragment extends DialogFragment implements View.OnClickListener {

    public static final String TAG = "FilterDialog";

    interface FilterListener {
        void onFilter(Filters filters);
    }

    private View mRootView;
    private Spinner mGenreSpinner;
    private Spinner mYearSpinner;
    private Spinner mSortSpinner;

    private FilterListener mFilterListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.dialog_filters, container, false);

        mGenreSpinner = mRootView.findViewById(R.id.spinner_genre);
        mYearSpinner = mRootView.findViewById(R.id.spinner_year);
        mSortSpinner = mRootView.findViewById(R.id.spinner_sort);

        mRootView.findViewById(R.id.button_search).setOnClickListener(this);
        mRootView.findViewById(R.id.button_cancel).setOnClickListener(this);

        // Populate genre spinner from string-array
        ArrayAdapter<CharSequence> genreAdapter = ArrayAdapter.createFromResource(
                requireContext(), R.array.genres, android.R.layout.simple_spinner_item);
        genreAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mGenreSpinner.setAdapter(genreAdapter);

        // Populate year spinner dynamically (from current year to 1900)
        List<String> years = new ArrayList<>();
        years.add(getString(R.string.value_any_year));
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        for (int y = currentYear; y >= 1900; y--) {
            years.add(String.valueOf(y));
        }
        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_item, years);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mYearSpinner.setAdapter(yearAdapter);

        // Populate sort spinner
        ArrayAdapter<CharSequence> sortAdapter = ArrayAdapter.createFromResource(
                requireContext(), R.array.sort_by, android.R.layout.simple_spinner_item);
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSortSpinner.setAdapter(sortAdapter);

        return mRootView;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        // if (context instanceof FilterListener) {
        //     mListener = (FilterListener) context;
        // }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_search:
                if (mFilterListener != null) {
                    // Call getFilters() to get the filters
                    mFilterListener.onFilter(getFilters());
                }
                dismiss();
                break;
            case R.id.button_cancel:
                dismiss();
                break;
        }
    }

    @Nullable
    private String getSelectedGenre() {
        String selected = (String) mGenreSpinner.getSelectedItem();
        return getString(R.string.value_any_genre).equals(selected) ? null : selected;
    }

    @Nullable
    private String getSelectedYear() {
        String selected = (String) mYearSpinner.getSelectedItem();
        return getString(R.string.value_any_year).equals(selected) ? null : selected;
    }

    @Nullable
    private String getSelectedSortBy() {
        String selected = (String) mSortSpinner.getSelectedItem();
        if (getString(R.string.sort_by_rating).equals(selected)) {
            return Movie.FIELD_AVG_RATING;
        } else if (getString(R.string.sort_by_popularity).equals(selected)) {
            return Movie.FIELD_POPULARITY;
        } else if (getString(R.string.sort_by_release_year).equals(selected)) {
            return Movie.FIELD_RELEASE_YEAR;
        }
        return null;
    }

    @Nullable
    private Query.Direction getSortDirection() {
        String selected = (String) mSortSpinner.getSelectedItem();
        if (getString(R.string.sort_by_rating).equals(selected) ||
                getString(R.string.sort_by_popularity).equals(selected) ||
                getString(R.string.sort_by_release_year).equals(selected)) {
            return Query.Direction.DESCENDING;
        }
        return null;
    }

    public void resetFilters() {
        if (mRootView != null) {
            mGenreSpinner.setSelection(0);
            mYearSpinner.setSelection(0);
            mSortSpinner.setSelection(0);
        }
    }

    public Filters getFilters() {
        Filters filters = new Filters();
        if (mRootView != null) {
            filters.setGenre(getSelectedGenre());
            filters.setReleaseYear(getSelectedYear());
            filters.setSortBy(getSelectedSortBy());
            filters.setSortDirection(getSortDirection());
        }
        return filters;
    }

    public void setFilterListener(FilterListener listener) {
        mFilterListener = listener;
    }
}
