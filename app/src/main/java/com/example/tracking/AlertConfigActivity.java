package com.example.tracking;

import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class AlertConfigActivity extends AppCompatActivity {

    private TextView tvSelectedDistance;
    private SeekBar distanceSeekBar;
    private String unit = "m";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert_config);

        tvSelectedDistance = findViewById(R.id.tvSelectedDistance);
        distanceSeekBar = findViewById(R.id.distanceSeekBar);

        String destinationName = getIntent().getStringExtra("destination_name");
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
            // Logic to activate alert
            finish();
        });
    }

    private void updateDistanceText() {
        int progress = distanceSeekBar.getProgress();
        int value = (progress + 1) * 10;
        tvSelectedDistance.setText(value + unit);
    }
}