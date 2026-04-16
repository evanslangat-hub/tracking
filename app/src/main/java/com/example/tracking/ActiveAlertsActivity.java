package com.example.tracking;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tracking.model.AlertModel;

import java.util.List;

public class ActiveAlertsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private View emptyState;
    private AlertsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_active_alerts);

        recyclerView = findViewById(R.id.alertsRecycler);
        emptyState = findViewById(R.id.emptyState);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        updateUI();
    }

    private void updateUI() {
        List<AlertModel> alerts = LocationAlertService.activeAlerts;
        if (alerts.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyState.setVisibility(View.GONE);
            adapter = new AlertsAdapter(alerts);
            recyclerView.setAdapter(adapter);
        }
    }

    private class AlertsAdapter extends RecyclerView.Adapter<AlertsAdapter.ViewHolder> {
        private List<AlertModel> alerts;

        AlertsAdapter(List<AlertModel> alerts) {
            this.alerts = alerts;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_active_alert, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            AlertModel alert = alerts.get(position);
            holder.tvName.setText(alert.destinationName);
            
            if (alert.alertDistance >= 1000) {
                holder.tvDistance.setText(String.format("%.1f km", alert.alertDistance / 1000.0));
            } else {
                holder.tvDistance.setText((int)alert.alertDistance + " m");
            }

            holder.btnStop.setOnClickListener(v -> {
                Intent intent = new Intent(ActiveAlertsActivity.this, LocationAlertService.class);
                intent.setAction(LocationAlertService.ACTION_STOP_ALERT);
                intent.putExtra(LocationAlertService.EXTRA_ALERT_ID, alert.id);
                startService(intent);
                
                Toast.makeText(ActiveAlertsActivity.this, "Alert removed", Toast.LENGTH_SHORT).show();
                updateUI();
            });
        }

        @Override
        public int getItemCount() {
            return alerts.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvDistance;
            View btnStop;

            ViewHolder(View view) {
                super(view);
                tvName = view.findViewById(R.id.tvAlertDestName);
                tvDistance = view.findViewById(R.id.tvAlertDistance);
                btnStop = view.findViewById(R.id.btnStopAlert);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUI();
    }
}
