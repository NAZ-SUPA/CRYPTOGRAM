package com.kurdish.cryptogram;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * Game Activity:
 * - This is the core screen where the cryptogram solving happens.
 * - Current Scope: Manages the game board UI, custom keyboard display, and navigation.
 * - Future Logic: Will include the cipher-to-letter mapping, input validation, 
 *   and mistake tracking state.
 */
public class Game extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // UI INITIALIZATION LOGIC:
        // Enable Edge-to-Edge to provide an immersive gameplay experience.
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_game);
        
        // SYSTEM INSET LOGIC:
        // Ensure the game UI (especially the top controls and bottom keyboard) 
        // does not overlap with system status bars or the navigation "pill".
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // NAVIGATION LOGIC - HOME:
        // The player can exit the game session and return to the main menu at any time.
        ImageButton btnHome = findViewById(R.id.btn_home);
        btnHome.setOnClickListener(v -> {
            // Explicitly return to MainMenu.
            Intent intent = new Intent(Game.this, MainMenu.class);
            startActivity(intent);
            // Optional: Add a confirmation dialog here in future to prevent accidental exit.
        });

        // NAVIGATION LOGIC - SETTINGS:
        // Allows the player to adjust settings (like font size) mid-game.
        ImageButton btnSetting = findViewById(R.id.btn_settings_main);
        btnSetting.setOnClickListener(v -> {
            Intent intent = new Intent(Game.this, Settings.class);
            startActivity(intent);
        });

    }
}