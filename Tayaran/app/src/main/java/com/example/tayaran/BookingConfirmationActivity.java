package com.example.tayaran;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;

public class BookingConfirmationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_confirmation);

        TextView summary = findViewById(R.id.summary);
        MaterialButton backButton = findViewById(R.id.backButton);

        Intent i = getIntent();
        Bundle bundle = i.getBundleExtra(FlightSearchActivity.EXTRA_BUNDLE);

        if (bundle != null) {
            String name = bundle.getString(ProfileActivity.KEY_NAME, "");
            String from = bundle.getString(FlightSearchActivity.KEY_FROM, "");
            String to = bundle.getString(FlightSearchActivity.KEY_TO, "");
            String date = bundle.getString(FlightSearchActivity.KEY_DATE, "");

            String route = date.isEmpty()
                    ? getString(R.string.booking_route, from, to)
                    : getString(R.string.booking_route_with_date, from, to, date);

            String message = getString(R.string.booking_thanks, name) + "\n" + route;
            summary.setText(message);

            //Intent resultIntent = new Intent();
            i.putExtra(FlightSearchActivity.EXTRA_BUNDLE, bundle);
            setResult(Activity.RESULT_OK, i);
        } else {
            summary.setText(R.string.booking_no_data);
            setResult(Activity.RESULT_CANCELED);
        }

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}