package com.example.tayaran;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.DatePicker;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class FlightSearchActivity extends AppCompatActivity {

    public static final String EXTRA_BUNDLE = "flight_search_bundle";
    public static final String KEY_FROM = "from";
    public static final String KEY_TO = "to";
    public static final String KEY_DATE = "date";

    private static final int REQUEST_BOOKING = 101;

    private TextInputLayout fromLayout;
    private TextInputLayout toLayout;
    private TextInputEditText fromInput;
    private TextInputEditText toInput;
    private TextInputEditText dateInput;

    private String selectedDate = "";
    private String profileName = "";
    private String profileEmail = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flight_search);

        initViews();
        restoreState(savedInstanceState);
        retrieveProfileData();
        setupListeners();
    }

    private void initViews() {
        fromLayout = findViewById(R.id.fromLayout);
        toLayout = findViewById(R.id.toLayout);
        fromInput = findViewById(R.id.fromCity);
        toInput = findViewById(R.id.toCity);
        dateInput = findViewById(R.id.travelDate);
    }

    private void restoreState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            selectedDate = savedInstanceState.getString(KEY_DATE, "");
            if (!TextUtils.isEmpty(selectedDate)) {
                dateInput.setText(selectedDate);
            }
        }
    }

    private void retrieveProfileData() {
        Bundle profileBundle = getIntent().getBundleExtra(ProfileActivity.EXTRA_BUNDLE);
        if (profileBundle != null) {
            profileName = profileBundle.getString(ProfileActivity.KEY_NAME, "");
            profileEmail = profileBundle.getString(ProfileActivity.KEY_EMAIL, "");
        }
    }

    private void setupListeners() {
        MaterialButton searchButton = findViewById(R.id.searchButton);
        MaterialButton backButton = findViewById(R.id.backButton);
        TextInputLayout dateLayout = findViewById(R.id.dateLayout);

        View.OnClickListener openDatePickerListener = v -> showDatePicker();

        dateInput.setOnClickListener(openDatePickerListener);
        if (dateLayout != null) {
            dateLayout.setOnClickListener(openDatePickerListener);
        }

        searchButton.setOnClickListener(v -> submitSearch());
        backButton.setOnClickListener(v -> finish());
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_DATE, selectedDate);
    }

    private void showDatePicker() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                FlightSearchActivity.this,
                (DatePicker view, int pickedYear, int pickedMonth, int pickedDay) -> {
                    Calendar chosen = Calendar.getInstance();
                    chosen.set(pickedYear, pickedMonth, pickedDay);
                    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    selectedDate = formatter.format(chosen.getTime());
                    dateInput.setText(selectedDate);
                },
                year, month, day);

        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void submitSearch() {
        String from = fromInput.getText() != null ? fromInput.getText().toString().trim() : "";
        String to = toInput.getText() != null ? toInput.getText().toString().trim() : "";
        String date = selectedDate != null ? selectedDate : "";

        boolean isValid = true;
        if (TextUtils.isEmpty(from)) {
            fromLayout.setError(getString(R.string.error_required));
            isValid = false;
        } else {
            fromLayout.setError(null);
        }

        if (TextUtils.isEmpty(to)) {
            toLayout.setError(getString(R.string.error_required));
            isValid = false;
        } else {
            toLayout.setError(null);
        }

        if (!isValid) {
            return;
        }

        Bundle bundle = new Bundle();
        bundle.putString(KEY_FROM, from);
        bundle.putString(KEY_TO, to);
        bundle.putString(KEY_DATE, date);
        bundle.putString(ProfileActivity.KEY_NAME, profileName);
        bundle.putString(ProfileActivity.KEY_EMAIL, profileEmail);

        Intent intent = new Intent(FlightSearchActivity.this, BookingConfirmationActivity.class);
        intent.putExtra(EXTRA_BUNDLE, bundle);

        startActivityForResult(intent, REQUEST_BOOKING);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (requestCode == REQUEST_BOOKING && resultCode == RESULT_OK && data != null) {
                Bundle resultBundle = data.getBundleExtra(EXTRA_BUNDLE);
                if (resultBundle != null) {
                    Toast.makeText(this, R.string.toast_booking_result_received, Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.booking_no_data), Toast.LENGTH_SHORT).show();
        }
    }
}