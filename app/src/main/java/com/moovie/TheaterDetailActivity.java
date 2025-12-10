package com.moovie;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Activity that displays detailed information about a selected movie theater.
 * 
 * This activity shows:
 * - Theater name and address
 * - Geographic coordinates
 * - Navigation options to view showtimes
 * 
 * The activity receives theater information via Intent extras from TheaterMapFragment
 * and provides navigation to ShowtimesActivity for viewing movie schedules.
 * 
 * @author Moovie Team
 * @version 1.0
 * @since 1.0
 */
public class TheaterDetailActivity extends AppCompatActivity {

    /**
     * Initializes the activity with theater information and sets up UI components.
     * Extracts theater data from Intent extras and configures view elements.
     * 
     * @param savedInstanceState Previous saved state, if any
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theater_detail);

        TheaterInfo theaterInfo = extractTheaterInfoFromIntent();
        setupViews(theaterInfo);
        setupActionBar();
    }

    /**
     * Extracts theater information from the launching Intent.
     * 
     * @return TheaterInfo object containing theater details
     */
    private TheaterInfo extractTheaterInfoFromIntent() {
        Intent intent = getIntent();
        return new TheaterInfo(
                intent.getStringExtra("theater_name"),
                intent.getStringExtra("theater_address"),
                intent.getDoubleExtra("theater_lat", 0.0),
                intent.getDoubleExtra("theater_lng", 0.0)
        );
    }

    /**
     * Sets up all view components with theater information and click listeners.
     * 
     * @param theaterInfo The theater information to display
     */
    private void setupViews(TheaterInfo theaterInfo) {
        TextView nameTextView = findViewById(R.id.theater_name);
        TextView addressTextView = findViewById(R.id.theater_address);
        TextView coordinatesTextView = findViewById(R.id.theater_coordinates);
        Button showtimesButton = findViewById(R.id.btn_view_showtimes);
        Button backButton = findViewById(R.id.btn_back);

        populateTheaterInfo(nameTextView, addressTextView, coordinatesTextView, theaterInfo);
        setupClickListeners(showtimesButton, backButton, theaterInfo.name);
    }

    /**
     * Populates theater information in the UI components.
     * 
     * @param nameTextView TextView for theater name
     * @param addressTextView TextView for theater address
     * @param coordinatesTextView TextView for coordinates
     * @param theaterInfo Theater information to display
     */
    private void populateTheaterInfo(TextView nameTextView, TextView addressTextView, 
                                   TextView coordinatesTextView, TheaterInfo theaterInfo) {
        nameTextView.setText(theaterInfo.name != null ? theaterInfo.name : "Unknown Theater");
        addressTextView.setText(theaterInfo.address != null ? theaterInfo.address : "Address not available");
        coordinatesTextView.setText(String.format("Coordinates: %.4f, %.4f", 
                theaterInfo.latitude, theaterInfo.longitude));
    }

    /**
     * Sets up click listeners for navigation buttons.
     * 
     * @param showtimesButton Button to view showtimes
     * @param backButton Button to go back
     * @param theaterName Name of the theater for passing to showtimes
     */
    private void setupClickListeners(Button showtimesButton, Button backButton, String theaterName) {
        showtimesButton.setOnClickListener(v -> launchShowtimesActivity(theaterName));
        backButton.setOnClickListener(v -> finish());
    }

    /**
     * Launches the ShowtimesActivity with theater information.
     * 
     * @param theaterName Name of the theater to pass to showtimes activity
     */
    private void launchShowtimesActivity(String theaterName) {
        Intent intent = new Intent(this, ShowtimesActivity.class);
        intent.putExtra("theater_name", theaterName);
        startActivity(intent);
    }

    /**
     * Configures the action bar with back navigation and title.
     */
    private void setupActionBar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Theater Details");
        }
    }

    /**
     * Handles action bar back button navigation.
     * 
     * @return true if navigation was handled
     */
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    /**
     * Data class to hold theater information extracted from Intent.
     */
    private static class TheaterInfo {
        final String name;
        final String address;
        final double latitude;
        final double longitude;

        TheaterInfo(String name, String address, double latitude, double longitude) {
            this.name = name;
            this.address = address;
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }
}