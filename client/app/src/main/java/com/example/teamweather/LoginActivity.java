package com.example.teamweather;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private EditText emailEditText;
    private EditText passwordEditText;

    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailEditText = findViewById(R.id.editTextEmailAddress);
        passwordEditText = findViewById(R.id.editTextPassword);
        progressBar = findViewById(R.id.idPBLoading);

        Button createAccountButton = findViewById(R.id.create_account_here_button);
        createAccountButton.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, CreateAccountActivity.class);
            startActivity(intent);
        });

        Button loginButton = findViewById(R.id.login_button);
        loginButton.setOnClickListener(v -> login());
    }

    /**
     * This method is called when the login button is clicked.
     * It validates the user and if the user exists, it will start the MapsActivity.
     * It checks if the user exists in the database by using the UserFetcher class to communicate with the server.
     */
    private void login() {
        UserFetcher fetcher = new UserFetcher(this);
        final String email = emailEditText.getText().toString();
        final String password = passwordEditText.getText().toString();
        progressBar.setVisibility(ProgressBar.VISIBLE);
        fetcher.validateUser(email, password, response -> {
            progressBar.setVisibility(ProgressBar.GONE);
            if (response.isError) {
                Toast.makeText(LoginActivity.this, "User does not exists", Toast.LENGTH_SHORT).show();
            } else if (response.isSuccessful) {
                Intent intent = new Intent(LoginActivity.this, MapsActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(LoginActivity.this, "Error while trying to log in, please try again", Toast.LENGTH_SHORT).show();
            }
        });
    }
}