package com.kurdish.cryptogram;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * Levels Activity:
 * - Manages the level selection screen.
 * - Loads player progress from SharedPreferences to determine which levels are unlocked.
 * - Visually distinguishes locked levels and prevents user interaction with them.
 */
public class Levels extends AppCompatActivity {

    // Tracks the highest level index the player has currently unlocked.
    private int unlockedLevel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Enable edge-to-edge support for modern Android displays.
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_levels);
        
        // Handle window insets (status bar, navigation bar) to ensure UI elements 
        // are not obscured by system UI.
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // PROGRESS LOGIC:
        // Load the 'unlocked_level' value from 'game_progress' SharedPreferences.
        // If it doesn't exist, default to 1 (first level unlocked).
        SharedPreferences prefs = getSharedPreferences("game_progress", MODE_PRIVATE);
        unlockedLevel = prefs.getInt("unlocked_level", 1);

        // NAVIGATION LOGIC:
        // Home button returns to the MainMenu activity.
        ImageButton btnHome = findViewById(R.id.btn_home);
        btnHome.setOnClickListener(v -> {
            Intent intent = new Intent(Levels.this, MainMenu.class);
            startActivity(intent);
        });

        // Settings button opens the Settings activity.
        ImageButton btnSettings = findViewById(R.id.btn_settings_main);
        btnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(Levels.this, Settings.class);
            startActivity(intent);
        });

        // GRID INITIALIZATION LOGIC:
        // Iterate through all children of the GridLayout to assign behavior based on level status.
        GridLayout levelsGrid = findViewById(R.id.levels_grid);
        for (int i = 0; i < levelsGrid.getChildCount(); i++) {
            final int selectedLevel = i + 1; // Level indices are 1-based.
            View card = levelsGrid.getChildAt(i);

            // VISUAL LOCKING LOGIC:
            // If the card's level is higher than the unlocked level, dim it to 40% opacity.
            // Note: Individual lock icons in activity_levels.xml are also handled here or statically.
            if (selectedLevel > unlockedLevel) {
                card.setAlpha(0.4f);
            }

            // INTERACTION LOGIC:
            // Define what happens when a level card is clicked.
            card.setOnClickListener(v -> {
                // If level is locked, show a feedback message and block navigation.
                if (selectedLevel > unlockedLevel) {
                    Toast.makeText(Levels.this, "Level Locked 🔒", Toast.LENGTH_SHORT).show();
                    return;
                }

                // If level is unlocked, navigate to the Game activity and pass the level ID.
                Intent intent = new Intent(Levels.this, Game.class);
                intent.putExtra("selected_level", selectedLevel);
                startActivity(intent);
            });
        }
    }
}