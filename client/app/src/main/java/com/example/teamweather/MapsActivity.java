package com.example.teamweather;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.teamweather.databinding.ActivityMapsBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
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

    private String userName;

    private ArrayList<TravelLocation> locations;
    private final ArrayList<Marker> markers = new ArrayList<>();

    private View getWeatherButton;
    private View saveMarkerButton;
    private View locationNameEditText;
    private View deleteMarkerButton;

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMapsBinding binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // get the markers previously added by the user
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            userName = bundle.getString("userName");
            locations = bundle.getParcelableArrayList("locations");
        }

        getWeatherButton = findViewById(R.id.get_weather_button);
        saveMarkerButton = findViewById(R.id.save_marker_button);
        locationNameEditText = findViewById(R.id.location_name_edit_text);
        deleteMarkerButton = findViewById(R.id.delete_marker_button);

        // set up logout button
        sharedPreferences = getSharedPreferences("user", MODE_PRIVATE);
        Button logoutButton = findViewById(R.id.logout_button);
        logoutButton.setOnClickListener(v -> logout());

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

    /**
     * Creates a marker on the map.
     * @param name The name of the marker.
     * @param latLng The location of the marker.
     */
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
     * Removes any marker that was created by the user but not saved.
     * This is done by checking if the marker's title is "new location".
     * Will execute when the user clicks on the map after creating a marker without saving it.
     */
    private void removeUnsavedMarkers() {
        for (Marker marker : markers) {
            if (Objects.equals(marker.getTitle(), "new location")) {
                marker.remove();
            }
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

        // show the delete and save button, and the location name edit text
        setUpSaveButtonAndEditText(marker);
        saveMarkerButton.setVisibility(View.VISIBLE);
        locationNameEditText.setVisibility(View.VISIBLE);
        setUpDeleteButton(marker);
        deleteMarkerButton.setVisibility(View.VISIBLE);

        // remove any markers that were added to the map but not saved
        removeUnsavedMarkers();

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

        // if a button is visible, remove it from the screen
        if (getWeatherButton.getVisibility() == View.VISIBLE) {
            getWeatherButton.setVisibility(View.GONE);
            // also, remove any markers that were added to the map but not saved
            removeUnsavedMarkers();
            saveMarkerButton.setVisibility(View.GONE);
            locationNameEditText.setVisibility(View.GONE);
            deleteMarkerButton.setVisibility(View.GONE);
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

    /**
     * Sets up the get weather button. Makes that when it will be clicked,
     * the MainActivity will be opened with the marker's name, and LatLng fields as parameters.
     * @param name The name of the location.
     * @param lat The latitude of the location.
     * @param lng The longitude of the location.
     */
    private void setUpGetWeatherButton(String name, double lat, double lng) {
        ((Button) getWeatherButton).setText(getString(R.string.get_weather, name));
        ((Button) getWeatherButton).setText(getString(R.string.get_weather, name));
        getWeatherButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, WeatherActivity.class);
            intent.putExtra("location", name);
            intent.putExtra("lat", lat);
            intent.putExtra("lng", lng);
            startActivity(intent);
        });
    }

    /**
     * Sets up the save button and the location name edit text. Makes that when it will be clicked,
     * the given marker will be saved to the map, and the location
     * will be added to the list of user locations.
     * If the location already exists, its name will be updated.
     * @param marker The marker that the button will delete.
     */
    private void setUpSaveButtonAndEditText(Marker marker) {
        ((EditText)locationNameEditText).setText(marker.getTitle());

        saveMarkerButton.setOnClickListener(v -> {
            String newName = ((EditText)locationNameEditText).getText().toString();
            if (newName.isEmpty() || newName.equals("new location")) {
                Toast.makeText(this, "Please enter a valid name for the location", Toast.LENGTH_SHORT).show();
            } else {
                setUpGetWeatherButton(newName, marker.getPosition().latitude, marker.getPosition().longitude);
                saveMarkerButton.setVisibility(View.GONE);
                locationNameEditText.setVisibility(View.GONE);
                deleteMarkerButton.setVisibility(View.GONE);

                // add the marker to the list of user locations, or update its name if it already exists
                addLocation(marker, newName);
                marker.setTitle(newName);

                // save the marker to the database
                saveLocationsToDatabase();
            }
        });
    }

    /**
     * Adds a location to the list of user locations, or updates its name if it already exists.
     * Also saves the list of locations to the database.
     * @param marker The marker to add.
     * @param newName The new name of the marker.
     */
    private void addLocation(Marker marker, String newName) {
        for (TravelLocation location : locations) {
            String name = location.getName();
            double lat = location.getLatLng().latitude;
            double lng = location.getLatLng().longitude;
            if (name.equals(marker.getTitle()) &&
                    lat == marker.getPosition().latitude &&
                    lng == marker.getPosition().longitude) {
                location.setName(newName);
                saveLocationsToDatabase();
                return;
            }
        }

        locations.add(new TravelLocation(newName, new LatLng(marker.getPosition().latitude, marker.getPosition().longitude)));
        saveLocationsToDatabase();
    }

    /**
     * Sets up the delete button. Makes that when it will be clicked,
     * the given marker will be deleted from the map, and the location
     * will be deleted from the list of user locations.
     * @param marker The marker to delete.
     */
    private void setUpDeleteButton(Marker marker) {
        deleteMarkerButton.setOnClickListener(v -> {
            deleteLocation(marker);
            deleteMarkerButton.setVisibility(View.GONE);
            saveMarkerButton.setVisibility(View.GONE);
            locationNameEditText.setVisibility(View.GONE);
            getWeatherButton.setVisibility(View.GONE);
            marker.remove();
        });
    }

    /**
     * Deletes a location from the list of user locations, and saves the list of locations to the database.
     * @param marker The marker to delete.
     */
    private void deleteLocation(Marker marker) {
        for (TravelLocation location : locations) {
            String name = location.getName();
            double lat = location.getLatLng().latitude;
            double lng = location.getLatLng().longitude;
            if (name.equals(marker.getTitle()) &&
                    lat == marker.getPosition().latitude &&
                    lng == marker.getPosition().longitude) {
                locations.remove(location);
                break;
            }
        }

        saveLocationsToDatabase();
    }

    /**
     * Saves the ArrayList<TravelLocation> locations to the database.
     */
    private void saveLocationsToDatabase() {
        Log.d(TAG, "saving locations to database");

        UserFetcher fetcher = new UserFetcher(this);
        fetcher.updateUserLocations(userName, locations, response -> {
            if (response.isError) {
                // server error
                Toast.makeText(MapsActivity.this, "Error while trying save, please try again", Toast.LENGTH_SHORT).show();
            } else if (response.isSuccessful) {
                Toast.makeText(MapsActivity.this, "Updated locations", Toast.LENGTH_SHORT).show();
            } else {
                // this should never be executed
                Toast.makeText(MapsActivity.this, "Not a valid location to save", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Logs out the user and returns to the login screen.
     */
    private void logout() {
        // clear the shared preferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
        // return to the login screen
        Intent intent = new Intent(this, StartScreenActivity.class);
        startActivity(intent);
        finish();
    }
}