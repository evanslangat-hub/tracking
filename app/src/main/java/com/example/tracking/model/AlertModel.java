package com.example.tracking.model;

import java.util.UUID;

public class AlertModel {
    public String id;
    public String destinationName;
    public double lat;
    public double lng;
    public float alertDistance;
    public boolean triggered;

    public AlertModel(String destinationName, double lat, double lng, float alertDistance) {
        this.id = UUID.randomUUID().toString();
        this.destinationName = destinationName;
        this.lat = lat;
        this.lng = lng;
        this.alertDistance = alertDistance;
        this.triggered = false;
    }
}
