package com.example.tayaran.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.example.tayaran.R;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Unbound foreground service: records a 15 second "voice note" with MediaRecorder.
 * Corrected version of the workshop (section 9): the blocking MediaRecorder calls
 * (prepare/start) run on a single-thread executor, never on the main thread.
 */
public class EnregistrementService extends Service {

    private static final String TAG = "EnregistrementService";
    private static final String CHANNEL_ID = "service_audio";
    private static final int NOTIFICATION_ID = 1;
    private static final long RECORD_DURATION_MS = 15000;

    private MediaRecorder mRecorder;
    private Handler mHandler;
    private ExecutorService mExecutor;
    private volatile boolean mRecording = false;

    @Override
    public void onCreate() {
        super.onCreate();
        mHandler = new Handler(Looper.getMainLooper());
        mExecutor = Executors.newSingleThreadExecutor();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null; // Unbound service
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Must be called quickly after startForegroundService(), every time
        demarrerForeground();

        // Ignore a second start while a recording is already running
        if (!mRecording) {
            mRecording = true;
            mExecutor.execute(this::demarrerEnregistrement); // off the main thread
        }
        // A microphone recording must never be restarted automatically after the
        // process is killed. Restarting it could record without a new user action.
        return START_NOT_STICKY;
    }

    private void demarrerForeground() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    getString(R.string.notif_channel_name),
                    NotificationManager.IMPORTANCE_LOW);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }

        Notification notif = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(getString(R.string.notif_title))
                .setContentText(getString(R.string.notif_text))
                .setSmallIcon(android.R.drawable.ic_btn_speak_now)
                .setOngoing(true)
                .build();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notif, ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE);
        } else {
            startForeground(NOTIFICATION_ID, notif);
        }
    }

    // Runs on the executor thread
    private void demarrerEnregistrement() {
        try {
            File audioFile = new File(getExternalFilesDir(null), "voice_note.3gp");
            mRecorder = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                    ? new MediaRecorder(this)
                    : new MediaRecorder();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mRecorder.setOutputFile(audioFile.getAbsolutePath());

            mRecorder.prepare(); // synchronous, but on the secondary thread
            mRecorder.start();
            Log.d(TAG, "Recording started: " + audioFile.getAbsolutePath());

            // Automatic stop after 15 s (callback runs on the main thread)
            mHandler.postDelayed(() -> {
                mExecutor.execute(this::arreterEnregistrement);
                Toast.makeText(getApplicationContext(),
                        getString(R.string.toast_voice_saved, audioFile.getName()),
                        Toast.LENGTH_LONG).show();
                stopSelf();
            }, RECORD_DURATION_MS);
        } catch (IOException | RuntimeException e) {
            Log.e(TAG, "MediaRecorder error", e);
            mHandler.post(() -> {
                Toast.makeText(getApplicationContext(),
                        R.string.toast_recording_failed, Toast.LENGTH_LONG).show();
                stopSelf();
            });
        }
    }

    // Runs on the executor thread
    private void arreterEnregistrement() {
        if (mRecorder != null) {
            try {
                mRecorder.stop();
            } catch (RuntimeException ignored) {
                // stop() throws if nothing was recorded; the release below still must happen
            }
            mRecorder.release();
            mRecorder = null;
        }
        mRecording = false;
    }

    @Override
    public void onDestroy() {
        mHandler.removeCallbacksAndMessages(null);
        mExecutor.execute(this::arreterEnregistrement);
        mExecutor.shutdown();
        super.onDestroy();
    }
}
