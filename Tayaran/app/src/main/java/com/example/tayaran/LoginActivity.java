package com.example.tayaran;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends AppCompatActivity {

    public static final String EXTRA_BUNDLE = "login_bundle";
    public static final String KEY_EMAIL = "email";

    private TextInputLayout emailLayout;
    private TextInputLayout passwordLayout;
    private TextInputEditText emailInput;
    private TextInputEditText passwordInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailLayout = findViewById(R.id.emailLayout);
        passwordLayout = findViewById(R.id.passwordLayout);
        emailInput = findViewById(R.id.email);
        passwordInput = findViewById(R.id.password);
        MaterialButton loginButton = findViewById(R.id.loginButton);
        MaterialButton backButton = findViewById(R.id.backButton);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptLogin();
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void attemptLogin() {
        String email = emailInput.getText() != null ? emailInput.getText().toString().trim() : "";
        String password = passwordInput.getText() != null ? passwordInput.getText().toString().trim() : "";

        boolean isValid = true;

        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.setError(getString(R.string.error_invalid_email));
            isValid = false;
        } else {
            emailLayout.setError(null);
        }

        if (TextUtils.isEmpty(password) || password.length() < 6) {
            passwordLayout.setError(getString(R.string.error_short_password));
            isValid = false;
        } else {
            passwordLayout.setError(null);
        }

        if (isValid) {
            Bundle bundle = new Bundle();
            bundle.putString(KEY_EMAIL, email);

            Intent intent = new Intent(LoginActivity.this, ProfileActivity.class);
            intent.putExtra(EXTRA_BUNDLE, bundle);

            startActivity(intent);
            finish();
        }
    }
}