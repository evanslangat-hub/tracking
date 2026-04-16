package com.example.tracking;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ActiveAlertsActivity extends AppCompatActivity {

    private View activeAlertCard;
    private View emptyState;
    private TextView tvDestName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_active_alerts);

        activeAlertCard = findViewById(R.id.activeAlertCard);
        emptyState = findViewById(R.id.emptyState);
        tvDestName = findViewById(R.id.tvAlertDestName);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        findViewById(R.id.btnStopActiveAlert).setOnClickListener(v -> {
            stopAlertService();
        });

        updateUI();
    }

    private void updateUI() {
        if (isServiceRunning(LocationAlertService.class)) {
            activeAlertCard.setVisibility(View.VISIBLE);
            emptyState.setVisibility(View.GONE);
            // In a real app, you'd get the name from a database or shared preferences
            // For now, we'll use a placeholder or check if we can pass it
        } else {
            activeAlertCard.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);
        }
    }

    private void stopAlertService() {
        Intent serviceIntent = new Intent(this, LocationAlertService.class);
        stopService(serviceIntent);
        Toast.makeText(this, "Alert stopped", Toast.LENGTH_SHORT).show();
        updateUI();
    }

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUI();
    }
}
