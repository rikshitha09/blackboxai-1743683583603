package com.example.healthmanager;

import java.io.Serializable;
import java.util.Calendar;

public class Reminder implements Serializable {
    private String id;
    private String medicationName;
    private String dosage;
    private long timeInMillis;
    private boolean[] repeatDays;
    private boolean isActive;

    public Reminder() {
        // Default constructor required for Firebase
    }

    public Reminder(String medicationName, String dosage, long timeInMillis, boolean[] repeatDays) {
        this.medicationName = medicationName;
        this.dosage = dosage;
        this.timeInMillis = timeInMillis;
        this.repeatDays = repeatDays;
        this.isActive = true;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMedicationName() {
        return medicationName;
    }

    public String getDosage() {
        return dosage;
    }

    public long getTimeInMillis() {
        return timeInMillis;
    }

    public boolean[] getRepeatDays() {
        return repeatDays;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public String getFormattedTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeInMillis);
        return String.format("%02d:%02d", 
            calendar.get(Calendar.HOUR_OF_DAY), 
            calendar.get(Calendar.MINUTE));
    }

    public String getRepeatDaysString() {
        if (repeatDays == null) return "Never";
        
        String[] dayNames = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        StringBuilder builder = new StringBuilder();
        boolean hasRepeats = false;
        
        for (int i = 0; i < repeatDays.length; i++) {
            if (repeatDays[i]) {
                if (hasRepeats) builder.append(", ");
                builder.append(dayNames[i]);
                hasRepeats = true;
            }
        }
        
        return hasRepeats ? builder.toString() : "Never";
    }
}