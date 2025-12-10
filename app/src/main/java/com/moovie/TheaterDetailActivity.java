package com.moovie;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class TheaterDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theater_detail);

        // Get theater info from intent
        String theaterName = getIntent().getStringExtra("theater_name");
        String theaterAddress = getIntent().getStringExtra("theater_address");
        double latitude = getIntent().getDoubleExtra("theater_lat", 0.0);
        double longitude = getIntent().getDoubleExtra("theater_lng", 0.0);

        // Set up views
        TextView nameTextView = findViewById(R.id.theater_name);
        TextView addressTextView = findViewById(R.id.theater_address);
        TextView coordinatesTextView = findViewById(R.id.theater_coordinates);
        Button showtimesButton = findViewById(R.id.btn_view_showtimes);
        Button backButton = findViewById(R.id.btn_back);

        // Set theater information
        nameTextView.setText(theaterName != null ? theaterName : "Unknown Theater");
        addressTextView.setText(theaterAddress != null ? theaterAddress : "Address not available");
        coordinatesTextView.setText(String.format("Coordinates: %.4f, %.4f", latitude, longitude));

        // Set up showtimes button
        showtimesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TheaterDetailActivity.this, ShowtimesActivity.class);
                intent.putExtra("theater_name", theaterName);
                startActivity(intent);
            }
        });

        // Set up back button
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Set up action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Theater Details");
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}