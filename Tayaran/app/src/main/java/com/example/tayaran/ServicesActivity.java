package com.example.tayaran;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.tayaran.services.AnalyseService;
import com.example.tayaran.services.EnregistrementService;
import com.example.tayaran.services.RetourReceiver;
import com.example.tayaran.services.SimulationService;
import com.google.android.material.button.MaterialButton;

public class ServicesActivity extends AppCompatActivity {

    private static final long RECORD_DURATION = 15000;

    //المدة التي ننتظرها قبل تشغيل خدمة التحليل
    private static final long ANALYSIS_DELAY = 15500;

    //إنشاء Handler يستطيع جدولة المهام وتنفيذها على Main Thread
    //سنستعمله لاحقاً لتأجيل تشغيل AnalyseService
    private Handler handler = new Handler(Looper.getMainLooper());

    private TextView lastAnalysisText;

    private MaterialButton syncButton, voiceNoteButton,
            analyseButton, runAllButton, backButton;

    private Runnable pendingAction;

    // طلب صلاحية الميكروفون
    private ActivityResultLauncher<String> permissionLauncher =
            registerForActivityResult(  //تسجل طلباً لدى Android، وتسمح لنا باستقبال النتيجة عندما ينتهي الطلب
                    new ActivityResultContracts.RequestPermission(),
                    granted -> {
                        if (granted) {
                            if (pendingAction != null) {
                                pendingAction.run();
                                pendingAction = null;
                            }
                        } else {
                            Toast.makeText(this,
                                    "Microphone permission denied",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_services);

        syncButton = findViewById(R.id.syncButton);
        voiceNoteButton = findViewById(R.id.voiceNoteButton);
        analyseButton = findViewById(R.id.analyseButton);
        runAllButton = findViewById(R.id.runAllButton);
        backButton = findViewById(R.id.backButton);
        lastAnalysisText = findViewById(R.id.lastAnalysisText);

        //عند الضغط على زر المحاكاة، نشغّل SimulationService
        syncButton.setOnClickListener(v -> startSimulation());

        voiceNoteButton.setOnClickListener(v ->
                checkPermission(this::startRecording));
        //Method Reference
        //وهي طريقة مختصرة لتمرير دالة دون تنفيذها فوراً بدلا من تنفيذ الأسهم

        analyseButton.setOnClickListener(v ->
                checkPermission(this::startAnalysis));

        runAllButton.setOnClickListener(v ->
                checkPermission(() -> {
                    handler.removeCallbacksAndMessages(null);
                    //نريد منع وجود عملية تحليل قديمة مؤجلة قبل جدولة عملية جديدة

                    startSimulation(); //محاكاة مزامنة كاتالوج الرحلات في ثريد خلفي
                    startRecording();  //هي Foreground Service تسجل 15 ثانية من الميكروفون

                    handler.postDelayed(  //تسمح لنا بجدولة مهمة لتنفيذها بعد مدة محددة
                            this::startAnalysis, //يستمع عبر SpeechRecognizer ويستخرج تفضيلات السفر
                            ANALYSIS_DELAY
                    );
                }));

        backButton.setOnClickListener(v -> finish());
    }

    // التحقق من صلاحية الميكروفون
    private void checkPermission(Runnable action) { //تستقبل مهمة نريد تنفيذها بعد التأكد من وجود الصلاحية

        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED) {

            action.run();

        } else {
            pendingAction = action;  //نحفظ المهمة التي نريد تنفيذها لاحقاً
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO);
            //إطلاق طلب صلاحية الميكروفون
        }
    }

    // تشغيل الخدمات
    private void startSimulation() {
        startService(new Intent(this, SimulationService.class));
        //Explicit Intent

        Toast.makeText(this,
                R.string.toast_sync_started,
                Toast.LENGTH_SHORT).show();
    }

    private void startRecording() {  //خدمة تنفذ عملاً مهماً للمستخدم، مثل التسجيل الصوتي، ويجب أن تعرض إشعاراً للمستخدم أثناء عملها
        ContextCompat.startForegroundService(
                this,
                new Intent(this, EnregistrementService.class)
        );
        //ContextCompat يوفر وظائف مساعدة متوافقة مع إصدارات اندرويد المختلفة
        //سنستعمله للتحقق من الصلاحية وتشغيل Foreground Service

        Toast.makeText(this,
                R.string.toast_recording_started,
                Toast.LENGTH_SHORT).show();
    }

    private void startAnalysis() {
        startService(new Intent(this, AnalyseService.class));

        Toast.makeText(this,
                R.string.toast_analysis_started,
                Toast.LENGTH_SHORT).show();
    }

    // عرض آخر نتيجة للتحليل
    @Override
    protected void onResume() {
        super.onResume();
        //نريد عرض آخر نتيجة للتحليل كلما عاد المستخدم إلى هذه الشاشة

        //ينشئ اتصالاً بمكان تخزين البيانات الذي نستخدمه في التطبيق
        SharedPreferences prefs = getSharedPreferences(
                RetourReceiver.PREFS_NAME,
                MODE_PRIVATE
                //خدمة RetourReceiver تستقبل البودكاست داخليا من AnalyseService ويخزن النتيجة في SharedPreferences
        );

        String last = prefs.getString(
                RetourReceiver.KEY_LAST_ANALYSIS, ""
                //قراءة آخر نتيجة للتحليل وتخزينها في متغير last
        );

        if (last.isEmpty()) {
            lastAnalysisText.setText(R.string.last_analysis_none);
            //نعرض النص الافتراضي الموجود في strings.xml، والذي يخبر المستخدم بأنه لا توجد نتيجة سابقة
        } else {
            lastAnalysisText.setText(last);
        }
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}