package com.example.tracking;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class AlertConfigActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 1001;

    private TextView tvSelectedDistance;
    private SeekBar distanceSeekBar;
    private String unit = "m";
    private double lat, lng;
    private String destinationName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert_config);

        tvSelectedDistance = findViewById(R.id.tvSelectedDistance);
        distanceSeekBar = findViewById(R.id.distanceSeekBar);

        destinationName = getIntent().getStringExtra("destination_name");
        lat = getIntent().getDoubleExtra("lat", 0);
        lng = getIntent().getDoubleExtra("lng", 0);

        if (destinationName != null && !destinationName.isEmpty()) {
            TextView tvName = findViewById(R.id.tvDestinationName);
            tvName.setText(destinationName);
            
            // Optionally update details if you passed them
            TextView tvDetails = findViewById(R.id.tvDestinationDetails);
            tvDetails.setText("Active Alert for " + destinationName);
        }

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        distanceSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int value = (progress + 1) * 10; // Simple scaling
                tvSelectedDistance.setText(value + unit);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        findViewById(R.id.btnMetres).setOnClickListener(v -> {
            unit = "m";
            updateDistanceText();
        });

        findViewById(R.id.btnKilometres).setOnClickListener(v -> {
            unit = "km";
            updateDistanceText();
        });

        findViewById(R.id.btnActivateAlert).setOnClickListener(v -> {
            if (checkAndRequestPermissions()) {
                startAlertService();
            }
        });

        findViewById(R.id.btnStopAlert).setOnClickListener(v -> {
            Intent serviceIntent = new Intent(this, LocationAlertService.class);
            stopService(serviceIntent);
            Toast.makeText(this, "Alert stopped", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private boolean checkAndRequestPermissions() {
        List<String> permissionsNeeded = new ArrayList<>();
        
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.POST_NOTIFICATIONS);
            }
        }

        if (!permissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsNeeded.toArray(new String[0]), PERMISSION_REQUEST_CODE);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            if (allGranted) {
                startAlertService();
            } else {
                Toast.makeText(this, "Permissions required to activate alert", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void startAlertService() {
        int progress = distanceSeekBar.getProgress();
        float distanceMeters = (progress + 1) * 10;
        if (unit.equals("km")) {
            distanceMeters *= 1000;
        }

        Intent serviceIntent = new Intent(this, LocationAlertService.class);
        serviceIntent.setAction(LocationAlertService.ACTION_ADD_ALERT);
        serviceIntent.putExtra("lat", lat);
        serviceIntent.putExtra("lng", lng);
        serviceIntent.putExtra("distance", distanceMeters);
        serviceIntent.putExtra("name", destinationName);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }

        Toast.makeText(this, "Alert activated for " + (int) distanceMeters + "m", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void updateDistanceText() {
        int progress = distanceSeekBar.getProgress();
        int value = (progress + 1) * 10;
        tvSelectedDistance.setText(value + unit);
    }
}