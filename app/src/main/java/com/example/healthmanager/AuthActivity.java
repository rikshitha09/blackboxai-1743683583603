package com.example.healthmanager;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AuthActivity extends AppCompatActivity {

    private TextInputLayout emailLayout, passwordLayout;
    private TextInputEditText emailField, passwordField;
    private Button loginButton, signupButton;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Check if user is already logged in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            startMainActivity();
            return;
        }

        // Initialize views
        emailLayout = findViewById(R.id.emailLayout);
        passwordLayout = findViewById(R.id.passwordLayout);
        emailField = findViewById(R.id.emailField);
        passwordField = findViewById(R.id.passwordField);
        loginButton = findViewById(R.id.loginButton);
        signupButton = findViewById(R.id.signupButton);
        progressBar = findViewById(R.id.progressBar);

        // Set click listeners
        loginButton.setOnClickListener(v -> loginUser());
        signupButton.setOnClickListener(v -> signupUser());
    }

    private void loginUser() {
        String email = emailField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();

        if (!validateInputs(email, password)) return;

        progressBar.setVisibility(View.VISIBLE);
        loginButton.setEnabled(false);
        signupButton.setEnabled(false);

        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, task -> {
                progressBar.setVisibility(View.GONE);
                loginButton.setEnabled(true);
                signupButton.setEnabled(true);

                if (task.isSuccessful()) {
                    startMainActivity();
                } else {
                    Toast.makeText(AuthActivity.this, 
                        "Authentication failed: " + task.getException().getMessage(),
                        Toast.LENGTH_LONG).show();
                }
            });
    }

    private void signupUser() {
        String email = emailField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();

        if (!validateInputs(email, password)) return;

        progressBar.setVisibility(View.VISIBLE);
        loginButton.setEnabled(false);
        signupButton.setEnabled(false);

        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, task -> {
                progressBar.setVisibility(View.GONE);
                loginButton.setEnabled(true);
                signupButton.setEnabled(true);

                if (task.isSuccessful()) {
                    startMainActivity();
                } else {
                    Toast.makeText(AuthActivity.this, 
                        "Registration failed: " + task.getException().getMessage(),
                        Toast.LENGTH_LONG).show();
                }
            });
    }

    private boolean validateInputs(String email, String password) {
        boolean valid = true;

        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.setError("Please enter a valid email address");
            valid = false;
        } else {
            emailLayout.setError(null);
        }

        if (TextUtils.isEmpty(password) || password.length() < 6) {
            passwordLayout.setError("Password must be at least 6 characters");
            valid = false;
        } else {
            passwordLayout.setError(null);
        }

        return valid;
    }

    private void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}