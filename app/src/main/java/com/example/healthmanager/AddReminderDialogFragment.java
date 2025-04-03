package com.example.healthmanager;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

public class AddReminderDialogFragment extends DialogFragment {

    private EditText medicationNameInput;
    private EditText dosageInput;
    private Button timePickerButton;
    private Chip[] dayChips;
    private Calendar selectedTime;
    private DatabaseReference databaseReference;
    private Reminder existingReminder;

    public static AddReminderDialogFragment newInstance(Reminder reminder) {
        AddReminderDialogFragment fragment = new AddReminderDialogFragment();
        if (reminder != null) {
            Bundle args = new Bundle();
            args.putSerializable("reminder", reminder);
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            databaseReference = FirebaseDatabase.getInstance().getReference("reminders").child(currentUser.getUid());
        }
        
        if (getArguments() != null) {
            existingReminder = (Reminder) getArguments().getSerializable("reminder");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_add_reminder, null);

        initializeViews(view);
        setupTimePicker();
        setupDayChips();
        setupButtons();

        if (existingReminder != null) {
            populateExistingReminder();
        }

        builder.setView(view);
        return builder.create();
    }

    private void initializeViews(View view) {
        medicationNameInput = view.findViewById(R.id.medicationNameInput);
        dosageInput = view.findViewById(R.id.dosageInput);
        timePickerButton = view.findViewById(R.id.timePickerButton);
        
        dayChips = new Chip[]{
            view.findViewById(R.id.sundayChip),
            view.findViewById(R.id.mondayChip),
            view.findViewById(R.id.tuesdayChip),
            view.findViewById(R.id.wednesdayChip),
            view.findViewById(R.id.thursdayChip),
            view.findViewById(R.id.fridayChip),
            view.findViewById(R.id.saturdayChip)
        };
    }

    private void setupTimePicker() {
        selectedTime = Calendar.getInstance();
        timePickerButton.setOnClickListener(v -> {
            TimePickerDialog timePickerDialog = new TimePickerDialog(
                getContext(),
                (view, hourOfDay, minute) -> {
                    selectedTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    selectedTime.set(Calendar.MINUTE, minute);
                    timePickerButton.setText(String.format("%02d:%02d", hourOfDay, minute));
                },
                selectedTime.get(Calendar.HOUR_OF_DAY),
                selectedTime.get(Calendar.MINUTE),
                true
            );
            timePickerDialog.show();
        });
    }

    private void setupDayChips() {
        for (Chip chip : dayChips) {
            chip.setCheckable(true);
        }
    }

    private void setupButtons() {
        Button cancelButton = getDialog().findViewById(R.id.cancelButton);
        Button saveButton = getDialog().findViewById(R.id.saveButton);

        cancelButton.setOnClickListener(v -> dismiss());
        saveButton.setOnClickListener(v -> saveReminder());
    }

    private void populateExistingReminder() {
        medicationNameInput.setText(existingReminder.getMedicationName());
        dosageInput.setText(existingReminder.getDosage());
        
        // Set time
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(existingReminder.getTimeInMillis());
        selectedTime = calendar;
        timePickerButton.setText(existingReminder.getFormattedTime());
        
        // Set repeat days
        boolean[] repeatDays = existingReminder.getRepeatDays();
        if (repeatDays != null) {
            for (int i = 0; i < dayChips.length; i++) {
                dayChips[i].setChecked(repeatDays[i]);
            }
        }
    }

    private void saveReminder() {
        String medicationName = medicationNameInput.getText().toString().trim();
        String dosage = dosageInput.getText().toString().trim();

        if (TextUtils.isEmpty(medicationName)) {
            Toast.makeText(getContext(), "Please enter medication name", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean[] repeatDays = new boolean[7];
        for (int i = 0; i < dayChips.length; i++) {
            repeatDays[i] = dayChips[i].isChecked();
        }

        long timeInMillis = selectedTime.getTimeInMillis();
        String reminderId = existingReminder != null ? existingReminder.getId() : databaseReference.push().getKey();

        Reminder reminder = new Reminder(medicationName, dosage, timeInMillis, repeatDays);
        if (existingReminder != null) {
            reminder.setId(existingReminder.getId());
            reminder.setActive(existingReminder.isActive());
        }

        databaseReference.child(reminderId).setValue(reminder)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    dismiss();
                } else {
                    Toast.makeText(getContext(), "Failed to save reminder", Toast.LENGTH_SHORT).show();
                }
            });
    }
}