package com.example.tracking;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

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

        findViewById(R.id.searchBar).setOnClickListener(v -> {
            Intent intent = new Intent(Home.this, SearchActivity.class);
            startActivityForResult(intent, 100);
        });
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
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocationName(name, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                mMap.clear();
                mMap.addMarker(new MarkerOptions().position(latLng).title(name));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}