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

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

public class LocationAlertService extends Service {

    private static final String CHANNEL_ID = "LocationAlertChannel";
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    
    private double destLat;
    private double destLng;
    private float alertDistance; // in meters
    private String destinationName;

    @Override
    public void onCreate() {
        super.onCreate();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            stopSelf();
            return START_NOT_STICKY;
        }

        destLat = intent.getDoubleExtra("lat", 0);
        destLng = intent.getDoubleExtra("lng", 0);
        alertDistance = intent.getFloatExtra("distance", 500);
        destinationName = intent.getStringExtra("name");

        Notification notification = getStickyNotification("Tracking location for " + destinationName);
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION);
        } else {
            startForeground(1, notification);
        }

        startLocationUpdates();
        
        return START_STICKY;
    }

    private void startLocationUpdates() {
        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
                .setMinUpdateIntervalMillis(2000)
                .build();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) return;
                for (Location location : locationResult.getLocations()) {
                    checkDistance(location);
                }
            }
        };

        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private void checkDistance(Location currentLocation) {
        float[] results = new float[1];
        Location.distanceBetween(currentLocation.getLatitude(), currentLocation.getLongitude(),
                destLat, destLng, results);
        float distanceInMeters = results[0];

        if (distanceInMeters >= alertDistance) {
            sendAlertNotification(distanceInMeters);
            // Optionally stop service after alert
            // stopSelf();
        }
    }

    private void sendAlertNotification(float distance) {
        NotificationManager manager = getSystemService(NotificationManager.class);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Distance Alert!")
                .setContentText("You are " + (int)distance + "m away from " + destinationName)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build();
        manager.notify(2, notification);
    }

    private Notification getStickyNotification(String text) {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Location Tracker Active")
                .setContentText(text)
                .setSmallIcon(android.R.drawable.ic_menu_mylocation)
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
            manager.createNotificationChannel(serviceChannel);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
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