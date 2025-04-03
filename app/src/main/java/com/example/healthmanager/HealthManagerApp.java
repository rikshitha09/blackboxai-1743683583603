package com.example.healthmanager;

import android.app.Application;
import androidx.work.WorkManager;
import com.google.firebase.FirebaseApp;

public class HealthManagerApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Initialize Firebase
        FirebaseApp.initializeApp(this);
        // Initialize WorkManager for background tasks
        WorkManager.initialize(this, new androidx.work.Configuration.Builder().build());
        
        // Start reminder service
        Intent serviceIntent = new Intent(this, ReminderService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
    }
}