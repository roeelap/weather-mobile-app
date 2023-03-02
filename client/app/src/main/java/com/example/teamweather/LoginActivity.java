package com.example.teamweather;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private EditText userNameEditText;
    private EditText passwordEditText;

    private ProgressBar progressBar;

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        userNameEditText = findViewById(R.id.editTextUserName);
        passwordEditText = findViewById(R.id.editTextPassword);
        progressBar = findViewById(R.id.idPBLoading);

        sharedPreferences = getSharedPreferences("user", MODE_PRIVATE);

        Button createAccountButton = findViewById(R.id.create_account_here_button);
        createAccountButton.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, CreateAccountActivity.class);
            startActivity(intent);
        });

        Button loginButton = findViewById(R.id.login_button);
        loginButton.setOnClickListener(v -> {
            final String userName = userNameEditText.getText().toString();
            final String password = passwordEditText.getText().toString();
            login(userName, password);
        });

        // autoLogin();
    }

    /**
     * This method is called when the login button is clicked.
     * It validates the user and if the user exists, it will start the MapsActivity.
     * It checks if the user exists in the database by using the UserFetcher class to communicate with the server.
     */
    private void login(String userName, String password) {
        Log.d(TAG, "login attempt");

        UserFetcher fetcher = new UserFetcher(this);
        progressBar.setVisibility(ProgressBar.VISIBLE);

        fetcher.fetchUser(false, userName, password, response -> {
            progressBar.setVisibility(ProgressBar.GONE);
            if (response.isError) {
                Toast.makeText(LoginActivity.this, "Error while trying to log in, please try again", Toast.LENGTH_SHORT).show();
            } else if (response.isSuccessful) {
                // save user credentials in shared preferences
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("userName", userName);
                editor.putString("password", password);
                editor.apply();
                // start MapsActivity
                startApp(userName, response.userLocations);
            } else {
                Toast.makeText(LoginActivity.this, "User does not exist", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startApp(String userName, ArrayList<TravelLocation> userLocations) {
        Intent intent = new Intent(LoginActivity.this, MapsActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("userName", userName);
        bundle.putParcelableArrayList("locations", userLocations);
        intent.putExtras(bundle);
        startActivity(intent);
        finish();
    }

    /**
     * This method is used for debugging purposes only.
     * It automatically logs in with the user "roee" and password "roee317" which
     * are valid credentials.
     */
    private void autoLogin() {
        login("roee", "roee317");
    }
}