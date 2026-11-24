package com.example.tayaran.services;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

import com.example.tayaran.R;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Unbound service: listens to the microphone with SpeechRecognizer, extracts travel
 * preferences from the sentence and broadcasts them (targeted broadcast) to RetourReceiver.
 * SpeechRecognizer must be created and used on the main thread, which is the case in onStartCommand().
 */
public class AnalyseService extends Service {

    private static final String TAG = "AnalyseService";

    // Must be identical to the action declared in the <receiver> of the manifest
    public static final String ACTION_PREFERENCES = "com.example.tayaran.PREFERENCES_ANALYSEES";

    // null = this application itself. For an external app, put its applicationId here.
    private static final String PACKAGE_DESTINATAIRE = null;

    private SpeechRecognizer recognizer;

    @Override
    public IBinder onBind(Intent intent) {
        return null; // Unbound service
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (recognizer == null) {
            lancerAnalyse();
        }
        return START_NOT_STICKY;
    }

    private void lancerAnalyse() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            Log.e(TAG, "Speech recognition unavailable");
            Toast.makeText(getApplicationContext(), R.string.toast_analysis_unavailable,
                    Toast.LENGTH_LONG).show();
            stopSelf();
            return;
        }

        recognizer = SpeechRecognizer.createSpeechRecognizer(this);
        recognizer.setRecognitionListener(new RecognitionListener() {
            @Override public void onReadyForSpeech(Bundle p) { }
            @Override public void onBeginningOfSpeech() { }
            @Override public void onRmsChanged(float rmsdB) { }
            @Override public void onBufferReceived(byte[] buffer) { }
            @Override public void onEndOfSpeech() { }
            @Override public void onPartialResults(Bundle partialResults) { }
            @Override public void onEvent(int type, Bundle p) { }

            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(
                        SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    diffuserResultats(extrairePreferences(matches.get(0)));
                }
                terminer();
            }

            @Override
            public void onError(int error) {
                Log.e(TAG, "SpeechRecognizer error: " + error);
                Toast.makeText(getApplicationContext(),
                        getString(R.string.toast_analysis_error, error),
                        Toast.LENGTH_SHORT).show();
                terminer();
            }
        });

        Intent reco = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        reco.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        String languageTag = getRecognitionLanguageTag();
        reco.putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageTag);
        reco.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, languageTag);
        recognizer.startListening(reco);
    }

    /** Select the speech language from the app locale, then fall back to the device locale. */
    private String getRecognitionLanguageTag() {
        LocaleListCompat appLocales = AppCompatDelegate.getApplicationLocales();
        Locale locale = appLocales.isEmpty() ? Locale.getDefault() : appLocales.get(0);
        String language = locale.getLanguage();
        if ("ar".equals(language)) return "ar-SA";
        if ("fr".equals(language)) return "fr-FR";
        return "en-US";
    }

    // Keyword -> preference. First entry of each row is the preference name.
    private Map<String, String> extrairePreferences(String texte) {
        Map<String, String> prefs = new LinkedHashMap<>();
        prefs.put("texte", texte);
        String t = normaliser(texte);

        String[][] categories = {
                {"classe_affaires", "affaires", "business", "premiere classe", "أعمال", "درجة رجال الأعمال"},
                {"classe_eco", "economique", "economy", "pas cher", "low cost", "اقتصادي", "رخيصة"},
                {"vol_direct", "direct", "sans escale", "مباشر", "بدون توقف"},
                {"plage", "plage", "beach", "bord de mer", "شاطئ", "البحر"},
                {"ville", "ville", "city", "visite", "musee", "culture", "مدينة", "زيارة", "متحف", "ثقافة"},
                {"famille", "famille", "enfant", "bebe", "family", "عائلة", "طفل", "أطفال"},
                {"bagages", "bagage", "valise", "luggage", "أمتعة", "حقيبة"},
                {"voyage", "voyage", "voyager", "vacances", "vacation", "holiday", "سفر", "سياحة", "عطلة", "إجازة"}
        };
        for (String[] cat : categories) {
            for (int i = 1; i < cat.length; i++) {
                if (t.contains(cat[i])) {
                    prefs.put(cat[0], "oui");
                    break;
                }
            }
        }
        return prefs;
    }

    // Lower case + remove accents so "économique" matches "economique"
    private static String normaliser(String s) {
        String n = Normalizer.normalize(s, Normalizer.Form.NFD);
        return n.replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .toLowerCase(Locale.ROOT);
    }

    private void diffuserResultats(Map<String, String> preferences) {
        Intent i = new Intent(ACTION_PREFERENCES);
        for (Map.Entry<String, String> e : preferences.entrySet()) {
            i.putExtra(e.getKey(), e.getValue());
        }
        // setPackage restricts the broadcast to the receiving app (no data leak)
        i.setPackage(PACKAGE_DESTINATAIRE != null ? PACKAGE_DESTINATAIRE : getPackageName());
        sendBroadcast(i);
    }

    private void terminer() {
        if (recognizer != null) {
            recognizer.destroy();
            recognizer = null;
        }
        stopSelf();
    }

    @Override
    public void onDestroy() {
        if (recognizer != null) {
            recognizer.destroy();
            recognizer = null;
        }
        super.onDestroy();
    }
}
