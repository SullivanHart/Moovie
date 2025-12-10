package com.moovie;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.moovie.model.Rating;
import com.moovie.util.FirebaseUtil;

/**
 * Dialog Fragment containing rating form.
 */
public class RatingDialogFragment extends DialogFragment implements View.OnClickListener {

    public static final String TAG = "RatingDialog";

    private EditText mRatingText;

    /**
     * Interface to listen for rating submission events.
     */
    interface RatingListener {
        /**
         * Called when a rating is submitted.
         * @param rating The rating object.
         */
        void onRating(Rating rating);
    }

    private RatingListener mRatingListener;

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
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_rating, container, false);
        mRatingText = v.findViewById(R.id.restaurant_form_text);

        // Set listeners for buttons
        v.findViewById(R.id.restaurant_form_button).setOnClickListener(this);
        v.findViewById(R.id.restaurant_form_cancel).setOnClickListener(this);

        return v;
    }

    /**
     * Called when the fragment is first attached to its context.
     * @param context The context.
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof RatingListener) {
            mRatingListener = (RatingListener) context;
        }
    }

    /**
     * Called when the fragment is visible to the user and actively running.
     */
    @Override
    public void onResume() {
        super.onResume();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    /**
     * Called when a view has been clicked.
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.restaurant_form_button:
                onSubmitClicked(v);
                break;
            case R.id.restaurant_form_cancel:
                onCancelClicked(v);
                break;
        }
    }

    /**
     * Handles the submit button click.
     * @param view The view that was clicked.
     */
    public void onSubmitClicked(View view) {
        FirebaseAuth auth = FirebaseUtil.getAuth();
        if (auth.getCurrentUser() != null) {
            // Pass -1 as rating to indicate it needs to be calculated
            Rating rating = new Rating(
                    auth.getCurrentUser(),
                    -1.0, 
                    mRatingText.getText().toString());

            if (mRatingListener != null) {
                mRatingListener.onRating(rating);
            }

            dismiss();
        }
    }

    /**
     * Handles the cancel button click.
     * @param view The view that was clicked.
     */
    public void onCancelClicked(View view) {
        dismiss();
    }
}
