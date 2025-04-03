package com.example.healthmanager;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HealthLogsFragment extends Fragment {

    private TextInputEditText bloodPressureInput;
    private TextInputEditText bloodSugarInput;
    private Button saveButton;
    private RecyclerView logsRecyclerView;
    private HealthLogAdapter adapter;
    private List<HealthLog> healthLogs = new ArrayList<>();

    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_health_logs, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("healthLogs");

        initializeViews(view);
        setupRecyclerView();
        loadHealthLogs();

        saveButton.setOnClickListener(v -> saveHealthData());
    }

    private void initializeViews(View view) {
        bloodPressureInput = view.findViewById(R.id.bloodPressureInput);
        bloodSugarInput = view.findViewById(R.id.bloodSugarInput);
        saveButton = view.findViewById(R.id.saveButton);
        logsRecyclerView = view.findViewById(R.id.logsRecyclerView);
    }

    private void setupRecyclerView() {
        adapter = new HealthLogAdapter(healthLogs);
        logsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        logsRecyclerView.setAdapter(adapter);
    }

    private void loadHealthLogs() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            databaseReference.child(userId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    healthLogs.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        HealthLog log = snapshot.getValue(HealthLog.class);
                        if (log != null) {
                            healthLogs.add(log);
                        }
                    }
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(getContext(), "Failed to load logs: " + databaseError.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void saveHealthData() {
        String bloodPressure = bloodPressureInput.getText().toString().trim();
        String bloodSugar = bloodSugarInput.getText().toString().trim();

        if (bloodPressure.isEmpty() && bloodSugar.isEmpty()) {
            Toast.makeText(getContext(), "Please enter at least one health metric", 
                Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            String logId = databaseReference.child(userId).push().getKey();

            if (logId != null) {
                String currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                    .format(new Date());

                HealthLog healthLog = new HealthLog(
                    bloodPressure.isEmpty() ? "N/A" : bloodPressure,
                    bloodSugar.isEmpty() ? "N/A" : bloodSugar,
                    currentDate
                );

                databaseReference.child(userId).child(logId).setValue(healthLog)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            bloodPressureInput.setText("");
                            bloodSugarInput.setText("");
                            Toast.makeText(getContext(), "Health data saved", 
                                Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "Failed to save data", 
                                Toast.LENGTH_SHORT).show();
                        }
                    });
            }
        }
    }
}