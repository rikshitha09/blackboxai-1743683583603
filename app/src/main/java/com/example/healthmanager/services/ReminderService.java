package com.example.healthmanager.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.healthmanager.MainActivity;
import com.example.healthmanager.R;
import com.example.healthmanager.Reminder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

public class ReminderService extends Service {

    private static final String CHANNEL_ID = "health_reminders";
    private static final int NOTIFICATION_ID = 101;
    private DatabaseReference databaseReference;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        startForeground(NOTIFICATION_ID, createNotification());
        setupReminderListener();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Health Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    private Notification createNotification() {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Health Manager")
            .setContentText("Reminder service is running")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .build();
    }

    private void setupReminderListener() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        databaseReference = FirebaseDatabase.getInstance()
            .getReference("reminders")
            .child(currentUser.getUid());

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Reminder reminder = snapshot.getValue(Reminder.class);
                    if (reminder != null && reminder.isActive()) {
                        scheduleReminderNotification(reminder);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
            }
        });
    }

    private void scheduleReminderNotification(Reminder reminder) {
        Calendar reminderTime = Calendar.getInstance();
        reminderTime.setTimeInMillis(reminder.getTimeInMillis());

        // Check if reminder should trigger today
        if (shouldTriggerToday(reminder)) {
            // TODO: Implement actual notification scheduling
            // This will use AlarmManager or WorkManager in a real implementation
        }
    }

    private boolean shouldTriggerToday(Reminder reminder) {
        Calendar now = Calendar.getInstance();
        boolean[] repeatDays = reminder.getRepeatDays();
        
        if (repeatDays == null) {
            // One-time reminder - check if it's today
            return now.get(Calendar.DAY_OF_YEAR) == 
                   Calendar.getInstance().setTimeInMillis(reminder.getTimeInMillis()).get(Calendar.DAY_OF_YEAR);
        } else {
            // Repeating reminder - check if today is a repeat day
            return repeatDays[now.get(Calendar.DAY_OF_WEEK) - 1];
        }
    }
}