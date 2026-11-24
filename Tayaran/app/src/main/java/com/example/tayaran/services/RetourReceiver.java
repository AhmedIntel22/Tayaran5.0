package com.example.tayaran.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.tayaran.R;

/**
 * Receives the preferences broadcast by AnalyseService (same action as in the manifest),
 * logs them, shows them in a Toast and keeps the last result for ServicesActivity.
 */
public class RetourReceiver extends BroadcastReceiver {

    private static final String TAG = "RetourReceiver";

    public static final String PREFS_NAME = "TravelPrefs";
    public static final String KEY_LAST_ANALYSIS = "last_analysis";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || !AnalyseService.ACTION_PREFERENCES.equals(intent.getAction())) {
            return;
        }
        Bundle extras = intent.getExtras();
        if (extras == null || extras.isEmpty()) {
            Log.d(TAG, "Broadcast received without data");
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (String key : extras.keySet()) {
            sb.append(key).append(" = ")
                    .append(extras.getString(key)).append("\n");
        }
        String result = sb.toString().trim();

        Log.d(TAG, "Preferences received:\n" + result);
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit().putString(KEY_LAST_ANALYSIS, result).apply();
        Toast.makeText(context, context.getString(R.string.toast_analysis_result, result),
                Toast.LENGTH_LONG).show();
    }
}
