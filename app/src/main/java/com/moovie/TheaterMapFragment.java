package com.moovie;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.CircularBounds;
import com.google.android.libraries.places.api.model.LocationBias;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.api.net.SearchByTextRequest;

import java.util.Arrays;
import java.util.List;

/**
 * Fragment that displays a Google Map with nearby movie theaters.
 * 
 * This fragment integrates with Google Maps API and Google Places API to:
 * - Show the user's current location on the map
 * - Search for nearby movie theaters using Places API
 * - Display theater markers that can be clicked to view details and showtimes
 * 
 * Features:
 * - Location permission handling
 * - Real-time theater search within 5km radius
 * - Interactive markers that launch theater detail activities
 * - Automatic camera positioning to user location
 * 
 * @author Moovie Team
 * @version 1.0
 * @since 1.0
 */
public class TheaterMapFragment extends Fragment implements OnMapReadyCallback {

    /** Google Map instance for displaying theaters and user location */
    private GoogleMap mMap;
    
    /** Client for accessing device location services */
    private FusedLocationProviderClient fusedLocationClient;
    
    /** User's last known location for theater search */
    private Location lastKnownLocation;
    
    /** Client for Google Places API theater search */
    private PlacesClient placesClient;

    /** Request code for location permission dialog */
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    /**
     * Creates and returns the view hierarchy associated with the fragment.
     * 
     * @param inflater The LayoutInflater object that can be used to inflate views
     * @param container The parent view that the fragment's UI should be attached to
     * @param savedInstanceState Previous saved state, if any
     * @return The View for the fragment's UI
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_theater_map, container, false);
    }

    /**
     * Called immediately after onCreateView() has returned, providing the view hierarchy.
     * Initializes Google Places API, location services, and sets up the map fragment.
     * 
     * @param view The View returned by onCreateView()
     * @param savedInstanceState Previous saved state, if any
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializePlacesAPI();
        initializeLocationServices();
        setupMapFragment();
    }

    /**
     * Initializes the Google Places API with the configured API key.
     */
    private void initializePlacesAPI() {
        if (!Places.isInitialized()) {
            Places.initialize(requireActivity().getApplicationContext(), BuildConfig.MAPS_API_KEY);
        }
        placesClient = Places.createClient(requireActivity());
    }

    /**
     * Initializes location services for getting user's current position.
     */
    private void initializeLocationServices() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
    }

    /**
     * Sets up the Google Maps fragment and requests map initialization.
     */
    private void setupMapFragment() {
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    /**
     * Callback triggered when the Google Map is ready to be used.
     * Sets up map interactions and initiates location-based theater search.
     * 
     * @param googleMap The GoogleMap instance that is ready for use
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        getDeviceLocationAndFindTheaters();
        setupMarkerClickListener();
    }

    /**
     * Sets up click listener for theater markers to launch detail activities.
     * When a marker is clicked, extracts theater information and launches TheaterDetailActivity.
     */
    private void setupMarkerClickListener() {
        mMap.setOnMarkerClickListener(marker -> {
            Place place = (Place) marker.getTag();
            if (place != null) {
                launchTheaterDetailActivity(place);
            }
            return true;
        });
    }

    /**
     * Launches the theater detail activity with theater information.
     * 
     * @param place The Place object containing theater details
     */
    private void launchTheaterDetailActivity(Place place) {
        Intent intent = new Intent(getActivity(), TheaterDetailActivity.class);
        intent.putExtra("theater_name", place.getName());
        intent.putExtra("theater_address", place.getAddress());
        if (place.getLatLng() != null) {
            intent.putExtra("theater_lat", place.getLatLng().latitude);
            intent.putExtra("theater_lng", place.getLatLng().longitude);
        }
        startActivity(intent);
    }

    /**
     * Checks location permissions and initiates location-based theater search.
     * If permissions are granted, gets device location and searches for nearby theaters.
     * If permissions are not granted, requests them from the user.
     */
    private void getDeviceLocationAndFindTheaters() {
        if (hasLocationPermission()) {
            enableLocationAndSearchTheaters();
        } else {
            requestLocationPermission();
        }
    }

    /**
     * Checks if the app has fine location permission.
     * 
     * @return true if location permission is granted, false otherwise
     */
    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(getContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Enables location display on map and initiates theater search.
     */
    private void enableLocationAndSearchTheaters() {
        try {
            mMap.setMyLocationEnabled(true);
            getCurrentLocationAndSearch();
        } catch (SecurityException e) {
            Log.e("TheaterMapFragment", "Location permission error: " + e.getMessage());
        }
    }

    /**
     * Gets the user's current location and searches for nearby theaters.
     */
    private void getCurrentLocationAndSearch() {
        fusedLocationClient.getLastLocation().addOnSuccessListener(requireActivity(), location -> {
            if (location != null) {
                handleLocationSuccess(location);
            } else {
                showLocationError();
            }
        });
    }

    /**
     * Handles successful location retrieval by updating map camera and searching theaters.
     * 
     * @param location The user's current location
     */
    private void handleLocationSuccess(Location location) {
        lastKnownLocation = location;
        LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 14f));
        findNearbyTheaters();
    }

    /**
     * Shows error message when location cannot be obtained.
     */
    private void showLocationError() {
        Toast.makeText(getContext(), "Unable to get current location.", Toast.LENGTH_SHORT).show();
    }

    /**
     * Requests location permission from the user.
     */
    private void requestLocationPermission() {
        requestPermissions(
                new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
    }

    /**
     * Handles the result of location permission request.
     * If permission is granted, initiates location and theater search.
     * If denied, shows appropriate message to user.
     * 
     * @param requestCode The request code passed to requestPermissions()
     * @param permissions The requested permissions
     * @param grantResults The grant results for the corresponding permissions
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            if (isPermissionGranted(grantResults)) {
                getDeviceLocationAndFindTheaters();
            } else {
                Toast.makeText(getContext(), "Location permission denied.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Checks if permission was granted based on grant results.
     * 
     * @param grantResults The grant results array
     * @return true if permission was granted, false otherwise
     */
    private boolean isPermissionGranted(int[] grantResults) {
        return grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Searches for nearby movie theaters using Google Places API.
     * Creates map markers for each theater found within a 5km radius of user location.
     * Each marker contains theater name, address, and location data.
     */
    private void findNearbyTheaters() {
        if (lastKnownLocation == null) {
            Log.w("TheaterMapFragment", "Cannot search theaters: location is null");
            return;
        }

        SearchByTextRequest request = createTheaterSearchRequest();
        executeTheaterSearch(request);
    }

    /**
     * Creates a Places API search request for movie theaters.
     * 
     * @return SearchByTextRequest configured for theater search
     */
    private SearchByTextRequest createTheaterSearchRequest() {
        List<Place.Field> placeFields = Arrays.asList(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.LAT_LNG,
                Place.Field.ADDRESS
        );

        LatLng userLatLng = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
        LocationBias locationBias = CircularBounds.newInstance(userLatLng, 5000.0);

        return SearchByTextRequest.builder("movie theater", placeFields)
                .setLocationBias(locationBias)
                .build();
    }

    /**
     * Executes the theater search request and handles results.
     * 
     * @param request The configured search request
     */
    private void executeTheaterSearch(SearchByTextRequest request) {
        placesClient.searchByText(request)
                .addOnSuccessListener(this::handleTheaterSearchSuccess)
                .addOnFailureListener(this::handleTheaterSearchFailure);
    }

    /**
     * Handles successful theater search by adding markers to the map.
     * 
     * @param response The Places API search response
     */
    private void handleTheaterSearchSuccess(com.google.android.libraries.places.api.net.SearchByTextResponse response) {
        for (Place place : response.getPlaces()) {
            if (place.getLatLng() != null) {
                addTheaterMarker(place);
            }
        }
    }

    /**
     * Adds a theater marker to the map.
     * 
     * @param place The theater place information
     */
    private void addTheaterMarker(Place place) {
        LatLng placeLatLng = new LatLng(place.getLatLng().latitude, place.getLatLng().longitude);
        Marker marker = mMap.addMarker(new MarkerOptions()
                .position(placeLatLng)
                .title(place.getName())
                .snippet(place.getAddress()));
        marker.setTag(place);
    }

    /**
     * Handles theater search failure by logging the error.
     * 
     * @param exception The exception that occurred during search
     */
    private void handleTheaterSearchFailure(Exception exception) {
        Log.e("TheaterMapFragment", "Theater search failed: " + exception.getMessage());
    }
}