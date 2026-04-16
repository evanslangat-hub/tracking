package com.example.tracking;

import android.content.Intent;
import android.os.Bundle;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class SearchActivity extends AppCompatActivity {

    EditText selectedPlace;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        selectedPlace = findViewById(R.id.selectedPlace);

        String name = getIntent().getStringExtra("name");
        if (name != null) {
            selectedPlace.setText(name);
        }

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        findViewById(R.id.btnClearSearch).setOnClickListener(v -> selectedPlace.setText(""));

        // HANDLE SEARCH ACTION FROM KEYBOARD
        selectedPlace.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String query = selectedPlace.getText().toString();
                if (!query.isEmpty()) {
                    navigateToMap(query);
                }
                return true;
            }
            return false;
        });

        findViewById(R.id.btnSelect).setOnClickListener(v -> {
            String query = selectedPlace.getText().toString();
            if (!query.isEmpty()) {
                navigateToMap(query);
            }
        });
    }

    private void navigateToMap(String destination) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("destination", destination);
        setResult(RESULT_OK, resultIntent);
        finish();
    }
}