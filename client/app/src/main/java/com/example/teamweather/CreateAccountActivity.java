package com.example.teamweather;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class CreateAccountActivity extends AppCompatActivity {

    private EditText emailEditText;
    private EditText passwordEditText;
    private EditText confirmPasswordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        emailEditText = findViewById(R.id.editTextEmailAddress);
        passwordEditText = findViewById(R.id.editTextPassword);
        confirmPasswordEditText = findViewById(R.id.editTextConfirmPassword);

        Button createAccountButton = findViewById(R.id.create_account_button);
        createAccountButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString();
            String password = passwordEditText.getText().toString();
            String confirmPassword = confirmPasswordEditText.getText().toString();

            // validate inputs
            if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                // TODO: display error message - please fill out all fields
                Toast.makeText(CreateAccountActivity.this, "Please fill out all fields", Toast.LENGTH_SHORT).show();
            } else if (!password.equals(confirmPassword)) {
                // TODO: display error message - passwords do not match
                Toast.makeText(CreateAccountActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            } else if ((!email.contains("@")) || (!email.contains(".")) || (email.indexOf("@") >= email.indexOf("."))) {
                // TODO: display error message - invalid email address
                Toast.makeText(CreateAccountActivity.this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
            }

            // TODO: create account
        });
    }
}