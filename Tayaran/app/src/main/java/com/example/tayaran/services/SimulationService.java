package com.example.tayaran.services;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.example.tayaran.R;

/**
 * Unbound (started) service that simulates a long task: the "flight catalogue sync".
 * The work runs in a separate thread and the service stops itself with stopSelf().
 */
public class SimulationService extends Service {

    private static final String TAG = "SimulationService";
    private static final int STEPS = 10;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private Thread workerThread;

    @Override
    public IBinder onBind(Intent intent) {
        return null; // Unbound service
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service started");

        // Ignore a second start while a sync is already running
        if (workerThread != null && workerThread.isAlive()) {
            return START_STICKY;
        }

        workerThread = new Thread(() -> {
            boolean completed = true;
            for (int i = 0; i < STEPS; i++) {
                Log.d(TAG, "Sync step " + (i + 1) + "/" + STEPS);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    completed = false;
                    break;
                }
            }
            if (completed) {
                mainHandler.post(() -> Toast.makeText(getApplicationContext(),
                        R.string.toast_sync_done, Toast.LENGTH_SHORT).show());
            }
            stopSelf();
        });
        workerThread.start();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "Service destroyed");
        if (workerThread != null) workerThread.interrupt();
        super.onDestroy();
    }
}
