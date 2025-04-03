package com.example.healthmanager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class HealthLogAdapter extends RecyclerView.Adapter<HealthLogAdapter.HealthLogViewHolder> {

    private final List<HealthLog> healthLogs;

    public HealthLogAdapter(List<HealthLog> healthLogs) {
        this.healthLogs = healthLogs;
    }

    @NonNull
    @Override
    public HealthLogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_health_log, parent, false);
        return new HealthLogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HealthLogViewHolder holder, int position) {
        HealthLog log = healthLogs.get(position);
        holder.bind(log);
    }

    @Override
    public int getItemCount() {
        return healthLogs.size();
    }

    static class HealthLogViewHolder extends RecyclerView.ViewHolder {
        private final TextView bloodPressureText;
        private final TextView bloodSugarText;
        private final TextView timestampText;

        public HealthLogViewHolder(@NonNull View itemView) {
            super(itemView);
            bloodPressureText = itemView.findViewById(R.id.bloodPressureText);
            bloodSugarText = itemView.findViewById(R.id.bloodSugarText);
            timestampText = itemView.findViewById(R.id.timestampText);
        }

        public void bind(HealthLog log) {
            bloodPressureText.setText(log.getBloodPressure());
            bloodSugarText.setText(log.getBloodSugar());
            timestampText.setText(log.getTimestamp());
        }
    }
}