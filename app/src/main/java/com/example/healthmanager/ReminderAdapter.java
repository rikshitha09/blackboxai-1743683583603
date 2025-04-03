package com.example.healthmanager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder> {

    private final List<Reminder> reminders;
    private final OnReminderClickListener listener;

    public interface OnReminderClickListener {
        void onReminderToggle(Reminder reminder, boolean isActive);
        void onReminderEdit(Reminder reminder);
        void onReminderDelete(Reminder reminder);
    }

    public ReminderAdapter(List<Reminder> reminders, OnReminderClickListener listener) {
        this.reminders = reminders;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ReminderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_reminder, parent, false);
        return new ReminderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReminderViewHolder holder, int position) {
        Reminder reminder = reminders.get(position);
        holder.bind(reminder, listener);
    }

    @Override
    public int getItemCount() {
        return reminders.size();
    }

    static class ReminderViewHolder extends RecyclerView.ViewHolder {
        private final TextView medicationNameText;
        private final TextView dosageText;
        private final TextView timeText;
        private final TextView repeatText;
        private final Switch activeSwitch;
        private final ImageButton editButton;
        private final ImageButton deleteButton;

        public ReminderViewHolder(@NonNull View itemView) {
            super(itemView);
            medicationNameText = itemView.findViewById(R.id.medicationNameText);
            dosageText = itemView.findViewById(R.id.dosageText);
            timeText = itemView.findViewById(R.id.timeText);
            repeatText = itemView.findViewById(R.id.repeatText);
            activeSwitch = itemView.findViewById(R.id.activeSwitch);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }

        public void bind(Reminder reminder, OnReminderClickListener listener) {
            medicationNameText.setText(reminder.getMedicationName());
            dosageText.setText(reminder.getDosage());
            timeText.setText(reminder.getFormattedTime());
            repeatText.setText(reminder.getRepeatDaysString());
            activeSwitch.setChecked(reminder.isActive());

            activeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (listener != null) {
                    listener.onReminderToggle(reminder, isChecked);
                }
            });

            editButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onReminderEdit(reminder);
                }
            });

            deleteButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onReminderDelete(reminder);
                }
            });
        }
    }
}