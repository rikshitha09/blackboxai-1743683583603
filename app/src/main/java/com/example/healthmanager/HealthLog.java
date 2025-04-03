package com.example.healthmanager;

public class HealthLog {
    private String bloodPressure;
    private String bloodSugar;
    private String timestamp;

    public HealthLog() {
        // Default constructor required for Firebase
    }

    public HealthLog(String bloodPressure, String bloodSugar, String timestamp) {
        this.bloodPressure = bloodPressure;
        this.bloodSugar = bloodSugar;
        this.timestamp = timestamp;
    }

    public String getBloodPressure() {
        return bloodPressure;
    }

    public String getBloodSugar() {
        return bloodSugar;
    }

    public String getTimestamp() {
        return timestamp;
    }
}