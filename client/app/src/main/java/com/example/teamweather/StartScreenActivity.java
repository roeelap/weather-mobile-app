package com.example.teamweather;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

public class StartScreenActivity extends AppCompatActivity {

    private static final String TAG = "StartScreenActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_screen);

        // check if user is already logged in
        SharedPreferences sharedPreferences = getSharedPreferences("user", MODE_PRIVATE);
        String savedUserName = sharedPreferences.getString("userName", "");
        String savedPassword = sharedPreferences.getString("password", "");
        if (!savedUserName.isEmpty() && !savedPassword.isEmpty()) {
            login(savedUserName, savedPassword);
        } else {
            goToLoginActivity();
        }
    }

    private void login(String userName, String password) {
        Log.d(TAG, "login attempt");

        UserFetcher fetcher = new UserFetcher(this);

        fetcher.fetchUser(false, userName, password, response -> {
            if (response.isError) {
                Toast.makeText(StartScreenActivity.this, "Error while trying to log in, please try again", Toast.LENGTH_SHORT).show();
                goToLoginActivity();
            } else if (response.isSuccessful) {
                // start MapsActivity
                startApp(userName, response.userLocations);
            } else {
                Toast.makeText(StartScreenActivity.this, "User does not exist", Toast.LENGTH_SHORT).show();
                goToLoginActivity();
            }
        });
    }

    private void startApp(String userName, ArrayList<TravelLocation> userLocations) {
        Intent intent = new Intent(StartScreenActivity.this, MapsActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("userName", userName);
        bundle.putParcelableArrayList("locations", userLocations);
        intent.putExtras(bundle);
        startActivity(intent);
        finish();
    }

    private void goToLoginActivity() {
        Intent intent = new Intent(StartScreenActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}