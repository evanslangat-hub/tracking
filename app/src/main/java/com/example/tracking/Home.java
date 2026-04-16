package com.example.tracking;

import android.content.Intent;
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

public class Home extends AppCompatActivity {

    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        recyclerView = findViewById(R.id.recentRecycler);

        List<Destination> list = new ArrayList<>();
        list.add(new Destination("Nairobi CBD", "City Centre · 3.2 km", "🏢"));
        list.add(new Destination("Kenyatta Hospital", "Upper Hill · 5.8 km", "🏥"));
        list.add(new Destination("Junction Mall", "Ngong Rd · 7.1 km", "🛒"));

        DestinationAdapter adapter = new DestinationAdapter(list, this, item -> {
            // NAVIGATION TO SEARCH SCREEN
            Intent intent = new Intent(Home.this, SearchActivity.class);
            intent.putExtra("name", item.name);
            startActivity(intent);
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        findViewById(R.id.searchBar).setOnClickListener(v -> {
            Intent intent = new Intent(Home.this, SearchActivity.class);
            startActivity(intent);
        });
    }
}