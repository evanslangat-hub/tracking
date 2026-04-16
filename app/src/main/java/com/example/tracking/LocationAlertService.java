package com.example.tracking;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.tracking.model.AlertModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class LocationAlertService extends Service {

    public static final String ACTION_ADD_ALERT = "ADD_ALERT";
    public static final String ACTION_STOP_ALERT = "STOP_ALERT";
    public static final String EXTRA_ALERT_ID = "ALERT_ID";
    
    private static final String CHANNEL_ID = "LocationAlertChannel";
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    
    public static final List<AlertModel> activeAlerts = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) return START_NOT_STICKY;

        String action = intent.getAction();
        if (ACTION_ADD_ALERT.equals(action)) {
            String name = intent.getStringExtra("name");
            double lat = intent.getDoubleExtra("lat", 0);
            double lng = intent.getDoubleExtra("lng", 0);
            float dist = intent.getFloatExtra("distance", 500);
            
            activeAlerts.add(new AlertModel(name, lat, lng, dist));
            updateForegroundNotification();
            startLocationUpdates();
        } else if (ACTION_STOP_ALERT.equals(action)) {
            String alertId = intent.getStringExtra(EXTRA_ALERT_ID);
            removeAlert(alertId);
        }

        if (activeAlerts.isEmpty()) {
            stopSelf();
        }

        return START_STICKY;
    }

    private void removeAlert(String alertId) {
        Iterator<AlertModel> iterator = activeAlerts.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().id.equals(alertId)) {
                iterator.remove();
                break;
            }
        }
        updateForegroundNotification();
        if (activeAlerts.isEmpty()) {
            stopSelf();
        }
    }

    private void updateForegroundNotification() {
        if (activeAlerts.isEmpty()) return;

        String text = activeAlerts.size() == 1 
            ? "Tracking 1 destination: " + activeAlerts.get(0).destinationName
            : "Tracking " + activeAlerts.size() + " destinations";

        Notification notification = getStickyNotification(text);
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION);
        } else {
            startForeground(1, notification);
        }
    }

    private void startLocationUpdates() {
        if (locationCallback != null) return; // Already running

        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
                .setMinUpdateIntervalMillis(2000)
                .build();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) return;
                for (Location location : locationResult.getLocations()) {
                    checkDistances(location);
                }
            }
        };

        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private void checkDistances(Location currentLocation) {
        for (AlertModel alert : activeAlerts) {
            if (alert.triggered) continue;

            float[] results = new float[1];
            Location.distanceBetween(currentLocation.getLatitude(), currentLocation.getLongitude(),
                    alert.lat, alert.lng, results);
            float distanceInMeters = results[0];

            if (distanceInMeters <= alert.alertDistance) {
                alert.triggered = true;
                sendAlertNotification(alert, distanceInMeters);
            }
        }
    }

    private void sendAlertNotification(AlertModel alert, float distance) {
        NotificationManager manager = getSystemService(NotificationManager.class);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Destination Reached!")
                .setContentText("You are " + (int)distance + "m from " + alert.destinationName)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build();
        manager.notify(alert.id.hashCode(), notification);
    }

    private Notification getStickyNotification(String text) {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Location Tracker Active")
                .setContentText(text)
                .setSmallIcon(android.R.drawable.ic_menu_mylocation)
                .setOngoing(true)
                .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Location Alert Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        activeAlerts.clear();
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
