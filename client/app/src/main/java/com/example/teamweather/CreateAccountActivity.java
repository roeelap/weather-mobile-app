package com.example.teamweather;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

public class CreateAccountActivity extends AppCompatActivity {

    private EditText userNameEditText;
    private EditText passwordEditText;
    private EditText confirmPasswordEditText;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        userNameEditText = findViewById(R.id.editTextUserName);
        passwordEditText = findViewById(R.id.editTextPassword);
        confirmPasswordEditText = findViewById(R.id.editTextConfirmPassword);
        progressBar = findViewById(R.id.idPBLoading);

        Button createAccountButton = findViewById(R.id.create_account_button);
        createAccountButton.setOnClickListener(v -> createAccount());
    }

    private void createAccount() {
        String userName = userNameEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        String confirmPassword = confirmPasswordEditText.getText().toString();

        // validate inputs
        if (userName.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(CreateAccountActivity.this, "Please fill out all fields", Toast.LENGTH_SHORT).show();
            return;
        } else if (!password.equals(confirmPassword)) {
            Toast.makeText(CreateAccountActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(ProgressBar.VISIBLE);
        UserFetcher fetcher = new UserFetcher(this);
        fetcher.fetchUser(true, userName, password, response -> {
            progressBar.setVisibility(ProgressBar.GONE);
            if (response.isError) {
                Toast.makeText(CreateAccountActivity.this, "Error while trying to create account, please try again", Toast.LENGTH_SHORT).show();
            } else if (response.isSuccessful) {
                Toast.makeText(CreateAccountActivity.this, "Account created successfully", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(CreateAccountActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(CreateAccountActivity.this, "There already exists a user with this username. Please try again", Toast.LENGTH_SHORT).show();
            }
        });
    }
}