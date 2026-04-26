/**
 * Package declaration for the Kurdish Cryptogram application.
 */
package com.kurdish.cryptogram;

/**
 * Standard Android imports for navigation and state management.
 */
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * AndroidX components for modern UI and activity lifecycle.
 */
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * Levels Activity:
 * - Displays a grid of game stages for selection.
 * - Manages the visual state (locked/unlocked) of each level based on player progress.
 * - Handles level-specific navigation and life (heart) verification.
 */
public class Levels extends AppCompatActivity {

    // Store the index of the highest level the player has unlocked.
    private int unlockedLevel;

    /**
     * Initializes the levels screen and populates the grid with interactive cards.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Basic activity setup.
        super.onCreate(savedInstanceState);
        // Enable modern edge-to-edge UI.
        EdgeToEdge.enable(this);
        // Load the level selection layout.
        setContentView(R.layout.activity_levels);

        // Adjust layout padding to account for system status and navigation bars.
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // --- PROGRESS RETRIEVAL ---
        // Access persistent game progress to determine which levels are playable.
        SharedPreferences progressPrefs = getSharedPreferences("game_progress", MODE_PRIVATE);
        // Default to level 1 if no progress is found.
        unlockedLevel = progressPrefs.getInt("unlocked_level", 1);

        // --- NAVIGATION CONTROLS ---
        // Home Button: Return to the Main Menu.
        ImageButton btnHome = findViewById(R.id.btn_home);
        btnHome.setOnClickListener(v -> {
            Intent intent = new Intent(Levels.this, MainMenu.class);
            startActivity(intent);
        });

        // Settings Button: Open the configuration screen.
        ImageButton btnSettings = findViewById(R.id.btn_settings_main);
        btnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(Levels.this, Settings.class);
            startActivity(intent);
        });

        // --- GRID INITIALIZATION ---
        // The GridLayout contains CardViews representing individual stages.
        GridLayout levelsGrid = findViewById(R.id.levels_grid);

        // Iterate through all level cards in the grid.
        for (int i = 0; i < levelsGrid.getChildCount(); i++) {
            // Level ID is 1-based (i + 1).
            final int selectedLevel = i + 1;
            CardView card = (CardView) levelsGrid.getChildAt(i);

            // --- LOCK/UNLOCK VISUAL LOGIC ---
            if (selectedLevel <= unlockedLevel) {
                // UNLOCKED STATE: Full opacity and visible level number.
                card.setAlpha(1.0f);
                card.setCardBackgroundColor(android.graphics.Color.parseColor("#FFFFFF"));

                // Navigate the card's child hierarchy to find UI elements.
                View innerView = card.getChildAt(0);
                if (innerView instanceof FrameLayout) {
                    FrameLayout fl = (FrameLayout) innerView;
                    TextView tv = (TextView) fl.getChildAt(0);
                    ImageView lock = (ImageView) fl.getChildAt(1);

                    // Show the level number in a dark color and hide the lock icon.
                    tv.setTextColor(android.graphics.Color.parseColor("#333333"));
                    lock.setVisibility(View.GONE);
                }

            } else {
                // LOCKED STATE: Faded appearance and visible lock icon.
                card.setAlpha(0.4f);
                card.setCardBackgroundColor(android.graphics.Color.parseColor("#444654"));

                View innerView = card.getChildAt(0);
                if (innerView instanceof FrameLayout) {
                    FrameLayout fl = (FrameLayout) innerView;
                    TextView tv = (TextView) fl.getChildAt(0);
                    ImageView lock = (ImageView) fl.getChildAt(1);

                    // Hide the level number (transparent) and reveal the lock icon.
                    tv.setTextColor(android.graphics.Color.parseColor("#33FFFFFF"));
                    lock.setVisibility(View.VISIBLE);
                }
            }

            // --- CLICK HANDLING ---
            card.setOnClickListener(v -> {
                // Prevent entry if the level is locked.
                if (selectedLevel > unlockedLevel) {
                    Toast.makeText(Levels.this, "Level Locked 🔒", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Check heart count; players need at least one life to start a level.
                SharedPreferences heartPrefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                int hearts = heartPrefs.getInt("heart_count", 5);

                if (hearts > 0) {
                    // Start the Game activity for the chosen level.
                    Intent intent = new Intent(Levels.this, Game.class);
                    intent.putExtra("selected_level", selectedLevel);
                    startActivity(intent);
                } else {
                    // Inform the user that they must wait for heart regeneration.
                    Toast.makeText(Levels.this, "No hearts left! Please wait for regeneration.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
