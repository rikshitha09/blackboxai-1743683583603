package com.example.healthmanager.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.healthmanager.MainActivity;
import com.example.healthmanager.R;
import com.example.healthmanager.Reminder;

public class ReminderReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "health_reminders";
    private static final int NOTIFICATION_ID = 1001;

    @Override
    public void onReceive(Context context, Intent intent) {
        Reminder reminder = (Reminder) intent.getSerializableExtra("reminder");
        if (reminder == null) return;

        String action = intent.getStringExtra("action");
        if ("taken".equals(action)) {
            // Mark medication as taken in database
            DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                .getReference("reminders")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(reminder.getId());
            databaseReference.child("lastTaken").setValue(System.currentTimeMillis());
            return;
        } else if ("snooze".equals(action)) {
            // Reschedule reminder for 10 minutes later
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent snoozeIntent = new Intent(context, ReminderReceiver.class);
            snoozeIntent.putExtra("reminder", reminder);
            
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                reminder.getId().hashCode() + 3,
                snoozeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MINUTE, 10); // Snooze for 10 minutes

            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                pendingIntent
            );
            return;
        }

        NotificationManager notificationManager = 
            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Create notification channel for Android O+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Health Reminders",
                NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Medication reminder notifications");
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{0, 300, 200, 300});
            channel.enableLights(true);
            channel.setLightColor(Color.RED);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            notificationManager.createNotificationChannel(channel);
        }

        // Create intent to open app when notification is tapped
        Intent appIntent = new Intent(context, MainActivity.class);
        appIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context,
            0,
            appIntent,
            PendingIntent.FLAG_IMMUTABLE
        );

        // Build notification
        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Medication Reminder")
            .setContentText("Time to take " + reminder.getMedicationName())
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .addAction(
                R.drawable.ic_snooze,
                "Snooze",
                createSnoozeAction(context, reminder)
            )
            .addAction(
                R.drawable.ic_done,
                "Taken",
                createTakenAction(context, reminder)
            )
            .build();

        notificationManager.notify(reminder.getId().hashCode(), notification);
    }

    private PendingIntent createSnoozeAction(Context context, Reminder reminder) {
        Intent snoozeIntent = new Intent(context, ReminderReceiver.class);
        snoozeIntent.putExtra("reminder", reminder);
        return PendingIntent.getBroadcast(
            context,
            reminder.getId().hashCode() + 1, // Different request code
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }

    private PendingIntent createDeleteIntent(Context context, Reminder reminder) {
        Intent deleteIntent = new Intent(context, ReminderReceiver.class);
        deleteIntent.putExtra("reminder", reminder);
        deleteIntent.putExtra("action", "dismissed");
        return PendingIntent.getBroadcast(
            context,
            reminder.getId().hashCode() + 4, // Different request code
            deleteIntent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }

    private PendingIntent createTakenAction(Context context, Reminder reminder) {
        Intent takenIntent = new Intent(context, ReminderReceiver.class);
        takenIntent.putExtra("reminder", reminder);
        takenIntent.putExtra("action", "taken");
        return PendingIntent.getBroadcast(
            context,
            reminder.getId().hashCode() + 2, // Different request code
            takenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }
}