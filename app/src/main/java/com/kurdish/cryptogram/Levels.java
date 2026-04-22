/**
 * Package declaration for the Kurdish Cryptogram application.
 */
package com.kurdish.cryptogram;

/**
 * Standard Android imports for navigation, data persistence, and UI components.
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
 * AndroidX compatibility and Material UI imports.
 */
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * Levels Activity:
 * - Manages the level selection screen for the game.
 * - Loads player progress from SharedPreferences to determine which levels are unlocked.
 * - Dynamically updates the UI to remove padlocks and change colors for unlocked levels.
 * - Enforces game rules regarding level access and heart consumption.
 */
public class Levels extends AppCompatActivity {

    // Tracks the highest level index the player has currently unlocked.
    private int unlockedLevel;

    /**
     * Initializes the activity, sets up the level grid, and manages level access logic.
     * @param savedInstanceState Saved instance state bundle.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Call parent constructor to perform standard setup.
        super.onCreate(savedInstanceState);

        // Enable edge-to-edge support for modern Android displays, utilizing the full screen area.
        EdgeToEdge.enable(this);
        // Set the layout resource for this activity.
        setContentView(R.layout.activity_levels);

        // Configure listener to handle system bar (status/navigation) insets for correct padding.
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            // Retrieve system bar dimensions.
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            // Apply padding so UI doesn't overlap with system bars.
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // --- PROGRESS RETRIEVAL ---
        // Load the 'unlocked_level' value from 'game_progress' SharedPreferences.
        // Defaults to 1 if the player is new.
        SharedPreferences prefs = getSharedPreferences("game_progress", MODE_PRIVATE);
        unlockedLevel = prefs.getInt("unlocked_level", 1);

        // --- NAVIGATION BUTTONS SETUP ---
        // Initialize Home button to return to the Main Menu.
        ImageButton btnHome = findViewById(R.id.btn_home);
        btnHome.setOnClickListener(v -> {
            Intent intent = new Intent(Levels.this, MainMenu.class);
            startActivity(intent);
        });

        // Initialize Settings button to open the settings screen.
        ImageButton btnSettings = findViewById(R.id.btn_settings_main);
        btnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(Levels.this, Settings.class);
            startActivity(intent);
        });

        // --- LEVEL GRID INITIALIZATION ---
        // Locate the GridLayout containing all the level CardViews.
        GridLayout levelsGrid = findViewById(R.id.levels_grid);

        // Iterate through each child View in the GridLayout to configure its appearance and behavior.
        for (int i = 0; i < levelsGrid.getChildCount(); i++) {
            // Current level index being processed (1-based index).
            final int selectedLevel = i + 1; 
            // Cast the child view to a CardView.
            CardView card = (CardView) levelsGrid.getChildAt(i);

            // --- DYNAMIC UI UPDATING BASED ON PROGRESS ---
            if (selectedLevel <= unlockedLevel) {
                // CASE: LEVEL IS UNLOCKED
                // Make the card fully opaque.
                card.setAlpha(1.0f); 
                // Set background color to white for active levels.
                card.setCardBackgroundColor(android.graphics.Color.parseColor("#FFFFFF"));

                // Access the inner layout components (TextView and ImageView) to modify their state.
                View innerView = card.getChildAt(0);
                if (innerView instanceof FrameLayout) {
                    FrameLayout fl = (FrameLayout) innerView;
                    TextView tv = (TextView) fl.getChildAt(0);
                    ImageView lock = (ImageView) fl.getChildAt(1);

                    // Set text color to a readable dark gray.
                    tv.setTextColor(android.graphics.Color.parseColor("#333333")); 
                    // Hide the padlock icon as the level is playable.
                    lock.setVisibility(View.GONE); 
                }

            } else {
                // CASE: LEVEL IS LOCKED
                // Dim the card to indicate it's not interactive.
                card.setAlpha(0.4f); 
                // Set a dark background color for locked levels.
                card.setCardBackgroundColor(android.graphics.Color.parseColor("#444654"));

                View innerView = card.getChildAt(0);
                if (innerView instanceof FrameLayout) {
                    FrameLayout fl = (FrameLayout) innerView;
                    TextView tv = (TextView) fl.getChildAt(0);
                    ImageView lock = (ImageView) fl.getChildAt(1);

                    // Dim the text color.
                    tv.setTextColor(android.graphics.Color.parseColor("#33FFFFFF")); 
                    // Ensure the padlock icon is visible.
                    lock.setVisibility(View.VISIBLE); 
                }
            }

            // --- CLICK INTERACTION HANDLING ---
            card.setOnClickListener(v -> {
                // 1. Check if the level is unlocked based on progression logic.
                if (selectedLevel > unlockedLevel) {
                    // Notify user if they try to enter a locked level.
                    Toast.makeText(Levels.this, "Level Locked 🔒", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 2. CHECK RESOURCE AVAILABILITY: Verify if player has enough lives (hearts) to play.
                SharedPreferences heartPrefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                int hearts = heartPrefs.getInt("heart_count", 5);

                if (hearts > 0) {
                    // If level is unlocked AND user has hearts, proceed to the Game activity.
                    Intent intent = new Intent(Levels.this, Game.class);
                    // Pass the selected level index to the Game activity.
                    intent.putExtra("selected_level", selectedLevel);
                    startActivity(intent);
                } else {
                    // Block entry if the player has 0 hearts.
                    Toast.makeText(Levels.this, "No hearts left! Please wait for regeneration.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
