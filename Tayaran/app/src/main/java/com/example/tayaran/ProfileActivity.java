package com.example.tayaran;

import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class ProfileActivity extends AppCompatActivity {

    // Keys used to pass profile data to FlightSearchActivity (it reads these constants)
    public static final String EXTRA_BUNDLE = "profile_bundle";
    public static final String KEY_NAME = "name";
    public static final String KEY_EMAIL = "email";

    private static final String PREFS_NAME = "UserPrefs";
    private static final String PREF_USERNAME = "username";
    private static final String PREF_EMAIL = "email";

    private TextInputLayout nameLayout, emailLayout;
    private TextInputEditText nameEditText, emailEditText;
    private MaterialButton backButton, saveButton, btnGoToTayra;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        initViews();
        prefillFields();
        setupListeners();
    }

    private void initViews() {
        nameLayout = findViewById(R.id.nameLayout);
        emailLayout = findViewById(R.id.emailLayout);
        nameEditText = findViewById(R.id.name);
        emailEditText = findViewById(R.id.email);
        backButton = findViewById(R.id.backButton);
        saveButton = findViewById(R.id.saveButton);
        btnGoToTayra = findViewById(R.id.btnGoToTayra);
    }

    // Prefill: saved prefs first, then the email received from LoginActivity
    private void prefillFields() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String savedName = prefs.getString(PREF_USERNAME, "");
        String savedEmail = prefs.getString(PREF_EMAIL, "");

        Bundle loginBundle = getIntent().getBundleExtra(LoginActivity.EXTRA_BUNDLE);
        if (loginBundle != null) {
            String loginEmail = loginBundle.getString(LoginActivity.KEY_EMAIL, "");
            if (!TextUtils.isEmpty(loginEmail)) {
                savedEmail = loginEmail;
            }
        }

        if (!TextUtils.isEmpty(savedName)) nameEditText.setText(savedName);
        if (!TextUtils.isEmpty(savedEmail)) emailEditText.setText(savedEmail);
    }

    private void setupListeners() {
        // Back: LoginActivity already finished itself, so this returns to MainActivity
        backButton.setOnClickListener(v -> finish());

        // Save: validate, persist, then continue to FlightSearchActivity
        saveButton.setOnClickListener(v -> {
            String name = getText(nameEditText);
            String email = getText(emailEditText);

            nameLayout.setError(null);
            emailLayout.setError(null);

            boolean valid = true;
            if (name.isEmpty()) {
                nameLayout.setError(getString(R.string.error_field_required));
                valid = false;
            }
            if (email.isEmpty()) {
                emailLayout.setError(getString(R.string.error_field_required));
                valid = false;
            }
            if (!valid) return;

            getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit()
                    .putString(PREF_USERNAME, name)
                    .putString(PREF_EMAIL, email)
                    .apply();

            Toast.makeText(this, R.string.toast_profile_saved, Toast.LENGTH_SHORT).show();

            Bundle bundle = new Bundle();
            bundle.putString(KEY_NAME, name);
            bundle.putString(KEY_EMAIL, email);

            Intent intent = new Intent(ProfileActivity.this, FlightSearchActivity.class);
            intent.putExtra(EXTRA_BUNDLE, bundle);
            startActivity(intent);
        });

        // Open the second app (Tayra) with the current profile data.
        // This is an outbound intent; ProfileActivity itself remains non-exported.
        btnGoToTayra.setOnClickListener(v -> {
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            String name = getText(nameEditText);
            String email = getText(emailEditText);
            if (name.isEmpty()) name = prefs.getString(PREF_USERNAME, "Ahmed");
            if (email.isEmpty()) email = prefs.getString(PREF_EMAIL, "ahmed@example.com");

            Intent intent = new Intent();
            intent.setComponent(new ComponentName("com.example.tayra", "com.example.tayra.MainActivity"));
            intent.putExtra("EXTRA_NAME", name);
            intent.putExtra("EXTRA_EMAIL", email);

            try {
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "Tayra app is not installed on the device", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getText(TextInputEditText editText) {
        return editText.getText() != null ? editText.getText().toString().trim() : "";
    }
}