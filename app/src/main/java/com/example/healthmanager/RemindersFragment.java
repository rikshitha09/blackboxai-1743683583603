package com.example.healthmanager;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class RemindersFragment extends Fragment implements ReminderAdapter.OnReminderClickListener {

    private RecyclerView remindersRecyclerView;
    private ReminderAdapter adapter;
    private List<Reminder> reminders = new ArrayList<>();
    private DatabaseReference databaseReference;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reminders, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            return;
        }

        databaseReference = FirebaseDatabase.getInstance().getReference("reminders").child(currentUser.getUid());

        initializeViews(view);
        setupRecyclerView();
        loadReminders();

        FloatingActionButton addReminderButton = view.findViewById(R.id.addReminderButton);
        addReminderButton.setOnClickListener(v -> showAddReminderDialog());
    }

    private void initializeViews(View view) {
        remindersRecyclerView = view.findViewById(R.id.remindersRecyclerView);
    }

    private void setupRecyclerView() {
        adapter = new ReminderAdapter(reminders, this);
        remindersRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        remindersRecyclerView.setAdapter(adapter);
    }

    private void loadReminders() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                reminders.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Reminder reminder = snapshot.getValue(Reminder.class);
                    if (reminder != null) {
                        reminder.setId(snapshot.getKey());
                        reminders.add(reminder);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "Failed to load reminders", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddReminderDialog() {
        AddReminderDialogFragment dialog = AddReminderDialogFragment.newInstance(null);
        dialog.show(getParentFragmentManager(), "AddReminderDialog");
    }

    @Override
    public void onReminderToggle(Reminder reminder, boolean isActive) {
        reminder.setActive(isActive);
        databaseReference.child(reminder.getId()).setValue(reminder);
    }

    @Override
    public void onReminderEdit(Reminder reminder) {
        AddReminderDialogFragment dialog = AddReminderDialogFragment.newInstance(reminder);
        dialog.show(getParentFragmentManager(), "EditReminderDialog");
    }

    @Override
    public void onReminderDelete(Reminder reminder) {
        new AlertDialog.Builder(getContext())
            .setTitle("Delete Reminder")
            .setMessage("Are you sure you want to delete this reminder?")
            .setPositiveButton("Delete", (dialog, which) -> {
                databaseReference.child(reminder.getId()).removeValue();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
}