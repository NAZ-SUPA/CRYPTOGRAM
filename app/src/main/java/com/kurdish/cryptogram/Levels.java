package com.kurdish.cryptogram;
import android.content.Intent;
import android.content.SharedPreferences; // ✅ added
import android.os.Bundle;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.Toast; // ✅ added

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

// Levels screen: handles navigation from top buttons and level selection cards.
public class Levels extends AppCompatActivity {

    private int unlockedLevel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Inflate the levels screen and keep it aligned with the rest of the app.
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_levels);
        // Preserve the content area below system bars on all devices.
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //  Load unlocked level
        SharedPreferences prefs = getSharedPreferences("game_progress", MODE_PRIVATE);
        unlockedLevel = prefs.getInt("unlocked_level", 1);

        // Home returns to the main menu.
        ImageButton btnHome = findViewById(R.id.btn_home);
        btnHome.setOnClickListener(v -> {
            Intent intent = new Intent(Levels.this, MainMenu.class);
            startActivity(intent);
        });

        // Settings opens app settings from the levels screen.
        ImageButton btnSettings = findViewById(R.id.btn_settings_main);
        btnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(Levels.this, Settings.class);
            startActivity(intent);
        });

        // Every card inside the 3x3 grid is treated as a level button.
        GridLayout levelsGrid = findViewById(R.id.levels_grid);
        for (int i = 0; i < levelsGrid.getChildCount(); i++) {
            final int selectedLevel = i + 1;
            View card = levelsGrid.getChildAt(i);

            //  show locked levels visually
            if (selectedLevel > unlockedLevel) {
                card.setAlpha(0.4f);
            }

            card.setOnClickListener(v -> {

                //  prevent opening locked levels
                if (selectedLevel > unlockedLevel) {
                    Toast.makeText(Levels.this, "Level Locked 🔒", Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent intent = new Intent(Levels.this, Game.class);
                intent.putExtra("selected_level", selectedLevel);
                startActivity(intent);
            });
        }
    }
}