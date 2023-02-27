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
import android.widget.EditText;
import android.widget.Toast;

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
import java.util.Objects;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener, GoogleMap.OnMapClickListener {

    private final String TAG = "MapsActivity";
    private GoogleMap mMap;

    private boolean locationPermissionGranted = false;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private FusedLocationProviderClient fusedLocationClient;
    private LatLng currentDeviceLocation;

    private ArrayList<TravelLocation> locations;
    private final ArrayList<Marker> markers = new ArrayList<>();

    private View getWeatherButton;
    private View saveMarkerButton;
    private View locationNameEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMapsBinding binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // get the markers previously added by the user
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            locations = bundle.getParcelableArrayList("markers");
        }

        getWeatherButton = findViewById(R.id.get_weather_button);
        saveMarkerButton = findViewById(R.id.save_marker_button);
        locationNameEditText = findViewById(R.id.location_name_edit_text);

        // set up location
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
        createUserMapMarkers();
    }

    private Marker createMapMarker(String name, LatLng latLng) {
        return mMap.addMarker(new MarkerOptions().position(latLng).title(name));
    }

    /**
     * Creates markers for all the locations in the locations list.
     */
    private void createUserMapMarkers() {
        for (TravelLocation location : locations) {
            Marker newMarker = createMapMarker(location.getName(), location.getLatLng());
            markers.add(newMarker);
        }
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

        // show the get weather button and set its text to the marker's name
        setUpGetWeatherButton(name, marker.getPosition().latitude, marker.getPosition().longitude);
        getWeatherButton.setVisibility(View.VISIBLE);

        // remove any markers that were added to the map but not saved
        removeUnsavedMarkers();
        saveMarkerButton.setVisibility(View.GONE);
        locationNameEditText.setVisibility(View.GONE);

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

        // if the button is visible, remove it from the screen
        if (getWeatherButton.getVisibility() == View.VISIBLE) {
            getWeatherButton.setVisibility(View.GONE);
            // also, remove any markers that were added to the map but not saved
            removeUnsavedMarkers();
            saveMarkerButton.setVisibility(View.GONE);
            locationNameEditText.setVisibility(View.GONE);
        } else {
            // if the button was not visible, propose to add a new marker
            Marker newMarker = createMapMarker("new location", latLng);
            markers.add(newMarker);
            locationNameEditText.setVisibility(View.VISIBLE);
            setUpSaveButtonAndEditText(newMarker);
            saveMarkerButton.setVisibility(View.VISIBLE);
            setUpGetWeatherButton("new location", latLng.latitude, latLng.longitude);
            getWeatherButton.setVisibility(View.VISIBLE);
        }
    }

    private void removeUnsavedMarkers() {
        for (Marker marker : markers) {
            if (Objects.equals(marker.getTitle(), "new location")) {
                marker.remove();
            }
        }
    }

    private void setUpGetWeatherButton(String name, double lat, double lng) {
        ((Button) getWeatherButton).setText(getString(R.string.get_weather, name));
        ((Button) getWeatherButton).setText(getString(R.string.get_weather, name));
        getWeatherButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("location", name);
            intent.putExtra("lat", lat);
            intent.putExtra("lng", lng);
            startActivity(intent);
        });
    }

    private void setUpSaveButtonAndEditText(Marker marker) {
        ((EditText)locationNameEditText).setText(marker.getTitle());

        saveMarkerButton.setOnClickListener(v -> {
            String name = ((EditText)locationNameEditText).getText().toString();
            if (name.isEmpty() || name.equals("new location")) {
                Toast.makeText(this, "Please enter a valid name for the location", Toast.LENGTH_SHORT).show();
            } else {
                marker.setTitle(name);
                saveMarkerButton.setVisibility(View.GONE);
                locationNameEditText.setVisibility(View.GONE);

                // add the marker to the list of user locations
                locations.add(new TravelLocation(name, new LatLng(marker.getPosition().latitude, marker.getPosition().longitude)));

                // TODO: save the marker to the database

                Toast.makeText(this, "Location saved", Toast.LENGTH_SHORT).show();
            }
        });
    }
}