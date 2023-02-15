package com.example.teamweather;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.teamweather.databinding.ActivityMapsBinding;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, GoogleMap.OnMapClickListener {

    private final String TAG = "MapsActivity";
    private GoogleMap mMap;

    private boolean locationPermissionGranted = false;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private FusedLocationProviderClient fusedLocationClient;
    private LatLng currentDeviceLocation;

    private final List<TravelLocation> locations = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMapsBinding binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        locations.add(new TravelLocation("Tel Aviv", new LatLng(32.0853, 34.7818)));
        locations.add(new TravelLocation("Haifa", new LatLng(32.7940, 34.9896)));
        locations.add(new TravelLocation("Jerusalem", new LatLng(31.7683, 35.2137)));
        locations.add(new TravelLocation("Eilat", new LatLng(29.5581, 34.9482)));
        locations.add(new TravelLocation("Beer Sheva", new LatLng(31.2524, 34.7913)));
        locations.add(new TravelLocation("Kiryat Shmona", new LatLng(33.2100, 35.5700)));

        getLocationPermission();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);
    }

    /**
     * Get current device location, store it in currentDeviceLocation, and move the camera to that location.
     */
    @SuppressLint("MissingPermission")
    private void getCurrentDeviceLocation() {
        try {
            if (locationPermissionGranted) {
                Task<Location> locationResult = fusedLocationClient.getLastLocation();
                locationResult.addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Location lastKnownLocation = task.getResult();
                        if (lastKnownLocation != null) {
                            Log.d(TAG, "getCurrentDeviceLocation: " + lastKnownLocation.getLatitude() + ", " + lastKnownLocation.getLongitude());
                            currentDeviceLocation = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentDeviceLocation, 10));
                            updateLocationUI();
                        }
                    } else {
                        Log.d(TAG, "Current location is null. Using defaults.");
                        Log.e(TAG, "Exception: %s", task.getException());
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e(TAG, "Exception: %s", e);
        }
    }

    /**
     * Request location permission, so that we can get the location of the
     * device. The result of the permission request is handled by a callback,
     * onRequestPermissionsResult.
     */
    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
        } else {
            // if location permission is not granted request permission
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    /**
     * Callback for the result from requesting permissions.
     * This method is invoked for every call on requestPermissions(android.app.Activity, String[], int).
     * @param requestCode The request code passed in requestPermissions(android.app.Activity, String[], int).
     * @param permissions The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions which is either PERMISSION_GRANTED or PERMISSION_DENIED. Never null.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        locationPermissionGranted = false;
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION &&
                grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
            getCurrentDeviceLocation();
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (locationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                currentDeviceLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    /**
     * Called when the map is ready to be used.
     * @param googleMap The GoogleMap object.
     */
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        getCurrentDeviceLocation();
        updateLocationUI();
        mMap.setOnMapClickListener(this);
        mMap.setOnMarkerClickListener(this);
        createMapMarkers();
    }

    /**
     * Called when a marker is clicked.
     * shows the get weather button and sets its text to the marker's name.
     * When the button is clicked, the MainActivity is opened with the marker's name as a parameter.
     * @param marker The marker that was clicked.
     */
    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        String name = marker.getTitle();
        Log.i(TAG, "onMarkerClick: " + name);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 10));

        View getWeatherButton = findViewById(R.id.get_weather_button);
        ((Button) getWeatherButton).setText(getString(R.string.get_weather, name));
        getWeatherButton.setVisibility(View.VISIBLE);

        getWeatherButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("location", name);
            startActivity(intent);
        });
        return false;
    }

    /**
     * Called when the map is clicked.
     * Removes the get weather button from the screen.
     * @param latLng The location that was clicked - not used currently.
     */
    @Override
    public void onMapClick(@NonNull LatLng latLng) {
        Log.e(TAG, "onMapClick: " + latLng.latitude + ", " + latLng.longitude);
        View getWeatherButton = findViewById(R.id.get_weather_button);
        getWeatherButton.setVisibility(View.GONE);
    }

    /**
     * Creates markers for all the locations in the locations list.
     */
    private void createMapMarkers() {
        for (TravelLocation location : locations) {
            mMap.addMarker(new MarkerOptions().position(location.getLatLng()).title(location.getName()));
        }
    }
}