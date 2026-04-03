package com.kurdish.cryptogram;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


// Main menu hub of the app: it presents the game options and routes the user to
// Game, Levels, or Settings based on the selected button.
public class MainMenu extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Enable edge-to-edge drawing and inflate the custom main menu layout.
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main_menu);
        // Apply system bar insets so controls stay visible below status/navigation bars.
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // Play starts the actual game screen.
        ImageButton btnPlay = findViewById(R.id.btn_main_action);

        btnPlay.setOnClickListener(v -> {
            // Navigate from the menu to the game screen.
            Intent intent = new Intent(MainMenu.this, Game.class);
            startActivity(intent);
        });

        // Levels opens the level selection or progress screen.
        ImageButton btnLevels = findViewById(R.id.btn_levels);

        btnLevels.setOnClickListener(v -> {
            // Navigate from the menu to the levels screen.
            Intent intent = new Intent(MainMenu.this, Levels.class);
            startActivity(intent);
        });

        // Settings opens the configuration screen without leaving the app flow.
        ImageButton btnSettings = findViewById(R.id.btn_settings_main);

        btnSettings.setOnClickListener(v -> {
            // Navigate from the menu to the settings screen.
            Intent intent = new Intent(MainMenu.this, Settings.class);
            startActivity(intent);
        });

    }
}