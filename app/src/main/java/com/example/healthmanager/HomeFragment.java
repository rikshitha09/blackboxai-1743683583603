package com.example.healthmanager;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class HomeFragment extends Fragment {

    private TextView userNameText;
    private TextView bloodPressureValue;
    private TextView bloodSugarValue;
    private FirebaseAuth mAuth;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        initializeViews(view);
        loadUserData();
        loadHealthData();
    }

    private void initializeViews(View view) {
        userNameText = view.findViewById(R.id.userNameText);
        bloodPressureValue = view.findViewById(R.id.bloodPressureValue);
        bloodSugarValue = view.findViewById(R.id.bloodSugarValue);
    }

    private void loadUserData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String displayName = currentUser.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                userNameText.setText(displayName);
            } else {
                userNameText.setText(currentUser.getEmail());
            }
        }
    }

    private void loadHealthData() {
        // TODO: Replace with actual data from database
        // For now, we'll use sample data
        bloodPressureValue.setText("120/80");
        bloodSugarValue.setText("98");
    }

    public void updateHealthData(String bloodPressure, String bloodSugar) {
        if (bloodPressure != null) {
            bloodPressureValue.setText(bloodPressure);
        }
        if (bloodSugar != null) {
            bloodSugarValue.setText(bloodSugar);
        }
    }
}