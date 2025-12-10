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

// We implement OnMapReadyCallback here
public class TheaterMapFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private Location lastKnownLocation;
    private PlacesClient placesClient;

    // A constant for the permission request code
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    // We don't need onCreateView, but we'll use onViewCreated
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_theater_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Places
        if (!Places.isInitialized()) {
            // Use requireActivity().getApplicationContext() for context
            Places.initialize(requireActivity().getApplicationContext(), BuildConfig.MAPS_API_KEY);
        }
        // Use requireActivity() for context
        placesClient = Places.createClient(requireActivity());
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // Obtain the SupportMapFragment.
        // **IMPORTANT:** Use getChildFragmentManager()
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Get the user's location and find theaters
        getDeviceLocationAndFindTheaters();

        // Set the marker click listener
        mMap.setOnMarkerClickListener(marker -> {
            Place place = (Place) marker.getTag();
            if (place != null) {
                // Launch theater detail activity
                Intent intent = new Intent(getActivity(), TheaterDetailActivity.class);
                intent.putExtra("theater_name", place.getName());
                intent.putExtra("theater_address", place.getAddress());
                if (place.getLatLng() != null) {
                    intent.putExtra("theater_lat", place.getLatLng().latitude);
                    intent.putExtra("theater_lng", place.getLatLng().longitude);
                }
                startActivity(intent);
            }
            return true;
        });
    }

    private void getDeviceLocationAndFindTheaters() {
        // Use getContext() for checking permission
        if (ContextCompat.checkSelfPermission(getContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            try {
                mMap.setMyLocationEnabled(true);
                fusedLocationClient.getLastLocation().addOnSuccessListener(requireActivity(), location -> {
                    if (location != null) {
                        lastKnownLocation = location;
                        LatLng userLocation = new LatLng(lastKnownLocation.getLatitude(),
                                lastKnownLocation.getLongitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 14f));
                        findNearbyTheaters();
                    } else {
                        // Use getContext() for the Toast
                        Toast.makeText(getContext(), "Unable to get current location.", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (SecurityException e) {
                Log.e("Exception: %s", e.getMessage());
            }
        } else {
            // Use the Fragment's requestPermissions method
            requestPermissions(
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    /**
     * Handle the result of the permission request
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getDeviceLocationAndFindTheaters();
            } else {
                Toast.makeText(getContext(), "Location permission denied.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Uses the Places API to find nearby movie theaters
     */
    private void findNearbyTheaters() {
        if (lastKnownLocation == null) return;

        List<Place.Field> placeFields = Arrays.asList(Place.Field.ID,
                Place.Field.NAME,
                Place.Field.LAT_LNG,
                Place.Field.ADDRESS);

        LatLng userLatLng = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
        LocationBias locationBias = CircularBounds.newInstance(userLatLng, 5000.0); // 5km radius

        SearchByTextRequest request =
                SearchByTextRequest.builder("movie theater", placeFields)
                        .setLocationBias(locationBias)
                        .build();

        placesClient.searchByText(request).addOnSuccessListener(response -> {
            for (Place place : response.getPlaces()) {
                if (place.getLatLng() != null) {
                    LatLng placeLatLng = new LatLng(place.getLatLng().latitude, place.getLatLng().longitude);
                    Marker marker = mMap.addMarker(new MarkerOptions()
                            .position(placeLatLng)
                            .title(place.getName())
                            .snippet(place.getAddress()));
                    marker.setTag(place);
                }
            }
        }).addOnFailureListener(exception -> {
            Log.e("TheaterMap", "Place search failed: " + exception.getMessage());
        });
    }
}