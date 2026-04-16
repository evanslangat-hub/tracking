package com.example.tracking;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tracking.adapters.DestinationAdapter;
import com.example.tracking.model.Destination;

import java.util.ArrayList;
import java.util.List;

import android.location.Address;
import android.location.Geocoder;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
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

public class Home extends AppCompatActivity implements OnMapReadyCallback {

    RecyclerView recyclerView;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private String currentDestinationName = "Nairobi Hospital"; // Default or last searched
    private double currentDestLat = -1.2921; // Default Nairobi
    private double currentDestLng = 36.8219;
    private View btnViewActiveAlerts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.homeMap);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        recyclerView = findViewById(R.id.recentRecycler);

        List<Destination> list = new ArrayList<>();
        list.add(new Destination("Nairobi CBD", "City Centre · 3.2 km", "🏢"));
        list.add(new Destination("Kenyatta Hospital", "Upper Hill · 5.8 km", "🏥"));
        list.add(new Destination("Junction Mall", "Ngong Rd · 7.1 km", "🛒"));

        DestinationAdapter adapter = new DestinationAdapter(list, this, item -> {
            // Updated: Instead of just going to search, show on home map
            showOnMap(item.name);
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        EditText searchEditText = findViewById(R.id.searchEditText);
        findViewById(R.id.btnSearchNow).setOnClickListener(v -> {
            String query = searchEditText.getText().toString();
            if (!query.isEmpty()) {
                showOnMap(query);
            }
        });

        searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String query = searchEditText.getText().toString();
                if (!query.isEmpty()) {
                    showOnMap(query);
                }
                return true;
            }
            return false;
        });

        findViewById(R.id.searchContainer).setOnClickListener(v -> {
            // Optional: still allow going to search activity if clicking the background
            Intent intent = new Intent(Home.this, SearchActivity.class);
            startActivityForResult(intent, 100);
        });

        findViewById(R.id.btnSetAlert).setOnClickListener(v -> {
            Intent intent = new Intent(Home.this, AlertConfigActivity.class);
            intent.putExtra("destination_name", currentDestinationName);
            intent.putExtra("lat", currentDestLat);
            intent.putExtra("lng", currentDestLng);
            startActivity(intent);
        });

        btnViewActiveAlerts = findViewById(R.id.btnViewActiveAlerts);
        btnViewActiveAlerts.setOnClickListener(v -> {
            Intent intent = new Intent(Home.this, ActiveAlertsActivity.class);
            startActivity(intent);
        });
    }

    private void updateActiveAlertVisibility() {
        if (isServiceRunning(LocationAlertService.class)) {
            btnViewActiveAlerts.setVisibility(View.VISIBLE);
        } else {
            btnViewActiveAlerts.setVisibility(View.GONE);
        }
    }

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (manager != null) {
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (serviceClass.getName().equals(service.service.getClassName())) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateActiveAlertVisibility();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    LatLng current = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(current, 14f));
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            String destination = data.getStringExtra("destination");
            if (destination != null) {
                showOnMap(destination);
            }
        }
    }

    private void showOnMap(String name) {
        if (mMap == null) return;
        currentDestinationName = name;
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocationName(name, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                currentDestLat = address.getLatitude();
                currentDestLng = address.getLongitude();
                mMap.clear();
                mMap.addMarker(new MarkerOptions().position(latLng).title(name));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f));
            } else {
                Toast.makeText(this, "Location not found: " + name, Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error finding location", Toast.LENGTH_SHORT).show();
        }
    }
}