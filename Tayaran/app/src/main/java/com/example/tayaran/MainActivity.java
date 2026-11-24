package com.example.tayaran;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivityLifecycle";

    private ProgressBar progressBar;
    private TextView progressText;
    private LinearLayout loadingLayout;
    private Button startButton, showLoadingButton, pauseButton, resumeButton, destroyButton, langButton;

    private int progressStatus = 0;
    private boolean isRunning = false;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable runnableCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Force Light Mode globally
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "onCreate called");

        // ربط العناصر
        startButton = findViewById(R.id.startButton);
        showLoadingButton = findViewById(R.id.showLoadingButton);
        pauseButton = findViewById(R.id.pauseButton);
        resumeButton = findViewById(R.id.resumeButton);
        destroyButton = findViewById(R.id.destroyButton);
        langButton = findViewById(R.id.langButton);

        progressBar = findViewById(R.id.progressBar);
        progressText = findViewById(R.id.progressText);
        loadingLayout = findViewById(R.id.loadingLayout);

        // الانتقال إلى LoginActivity
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "startButton onClick triggered");
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        // Atelier 4: open the screen that launches the three services
        findViewById(R.id.travelToolsButton).setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, ServicesActivity.class)));

        // زر تغيير اللغة
        langButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "langButton onClick triggered");

                LocaleListCompat currentLocales = AppCompatDelegate.getApplicationLocales();
                String currentLang = currentLocales.isEmpty() ? "" : currentLocales.get(0).getLanguage();

                if (currentLang.equals("ar")) {
                    // التبديل للإنجليزية
                    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("en"));
                } else {
                    // التبديل للعربية
                    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("ar"));
                }
            }
        });

        // حركة شريط التحميل
        runnableCode = new Runnable() {
            @Override
            public void run() {
                if (isRunning && progressStatus < 100) {
                    progressStatus++;
                    progressBar.setProgress(progressStatus);
                    progressText.setText(getString(R.string.progress_percent, progressStatus));
                    handler.postDelayed(this, 100);
                } else if (progressStatus >= 100) {
                    isRunning = false;
                    Toast.makeText(MainActivity.this, R.string.toast_loading_completed, Toast.LENGTH_SHORT).show();
                }
            }
        };

        // زر Show Loading
        showLoadingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "showLoadingButton onClick triggered");
                loadingLayout.setVisibility(View.VISIBLE);
                progressStatus = 0;
                progressBar.setProgress(0);
                progressText.setText(getString(R.string.progress_percent, 0));

                isRunning = true;
                handler.removeCallbacks(runnableCode);
                handler.post(runnableCode);
            }
        });

        // زر Pause
        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "pauseButton triggered");
                if (isRunning) {
                    isRunning = false;
                    handler.removeCallbacks(runnableCode);
                }
            }
        });

        // زر Resume
        resumeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "resumeButton triggered");
                if (!isRunning && progressStatus < 100 && loadingLayout.getVisibility() == View.VISIBLE) {
                    isRunning = true;
                    handler.post(runnableCode);
                }
            }
        });

        // زر Destroy
        destroyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "destroyButton triggered");
                isRunning = false;
                handler.removeCallbacks(runnableCode);
                progressStatus = 0;
                progressBar.setProgress(0);
                progressText.setText(getString(R.string.progress_percent, 0));
                loadingLayout.setVisibility(View.GONE);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart called");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume called");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause called");
        if (isRunning) {
            isRunning = false;
            handler.removeCallbacks(runnableCode);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop called");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy called");
        handler.removeCallbacks(runnableCode);
    }
}