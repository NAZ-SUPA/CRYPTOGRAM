package com.kurdish.cryptogram;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

// Game screen that keeps the custom keyboard visible and exposes quick navigation
// back to the menu or forward into the app settings.
public class Game extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Apply the shared edge-to-edge layout and load the game board screen.
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_game);
        // Keep the content clear of system bars on devices with gesture navigation.
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Home returns to the main menu so the player can pick another screen.
        ImageButton btnHome = findViewById(R.id.btn_home);

        btnHome.setOnClickListener(v -> {
            // Navigate back to the main menu from the game screen.
            Intent intent = new Intent(Game.this, MainMenu.class);
            startActivity(intent);
        });

        // Settings opens the app's configuration screen from inside the game.
        ImageButton btnSetting = findViewById(R.id.btn_settings_main);

        btnSetting.setOnClickListener(v -> {
            // Open the app settings screen.
            Intent intent = new Intent(Game.this, Settings.class);
            startActivity(intent);
        });

    }
}