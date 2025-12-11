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

    interface RatingListener {
        void onRating(Rating rating);
    }

    private RatingListener mRatingListener;

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

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof RatingListener) {
            mRatingListener = (RatingListener) context;
        }
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
            case R.id.restaurant_form_button:
                onSubmitClicked(v);
                break;
            case R.id.restaurant_form_cancel:
                onCancelClicked(v);
                break;
        }
    }

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

    public void onCancelClicked(View view) {
        dismiss();
    }
}
