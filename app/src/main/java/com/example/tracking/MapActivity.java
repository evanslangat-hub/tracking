package com.example.tracking;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private String destinationName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        destinationName = getIntent().getStringExtra("destination");

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        updateLocationUI();

        if (destinationName != null && !destinationName.isEmpty()) {
            searchDestination(destinationName);
        } else {
            getDeviceLocation();
        }
    }

    private void searchDestination(String name) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocationName(name, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                mMap.clear();
                mMap.addMarker(new MarkerOptions().position(latLng).title(name));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f));
            } else {
                Toast.makeText(this, "Destination not found", Toast.LENGTH_SHORT).show();
                getDeviceLocation();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error finding destination", Toast.LENGTH_SHORT).show();
            getDeviceLocation();
        }
    }

    private void updateLocationUI() {
        if (mMap == null) return;
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private void getDeviceLocation() {
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                fusedLocationClient.getLastLocation()
                        .addOnSuccessListener(this, location -> {
                            if (location != null) {
                                LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                                mMap.clear();
                                mMap.addMarker(new MarkerOptions().position(currentLatLng).title("Current Location"));
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f));
                            } else {
                                // Default to Nairobi if location is null
                                LatLng nairobi = new LatLng(-1.286389, 36.817223);
                                mMap.addMarker(new MarkerOptions().position(nairobi).title("Nairobi (Default)"));
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(nairobi, 14f));
                            }
                        });
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                updateLocationUI();
                getDeviceLocation();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}