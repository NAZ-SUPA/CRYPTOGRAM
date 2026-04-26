/**
 * Package declaration for the Kurdish Cryptogram application.
 */
package com.kurdish.cryptogram;

/**
 * Standard Android imports for navigation, data persistence, and UI handling.
 */
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

/**
 * AndroidX components for modern activity lifecycle and edge-to-edge UI.
 */
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * MainMenu:
 * - Serves as the primary navigation hub of the application.
 * - Handles the logic for the player's "Life" (Heart) regeneration system.
 * - Manages transitions to Game, Levels, and Settings activities.
 */
public class MainMenu extends AppCompatActivity {

    // --- SYSTEM CONSTANTS ---
    // Defines the duration for a single heart to regenerate (30 minutes).
    private static final long THIRTY_MINUTES_IN_MILLIS = 30 * 60 * 1000L;
    
    // --- STATE VARIABLES ---
    // A timer used to update the countdown UI on the main menu.
    private CountDownTimer heartTimer;
    // Cached value of the player's current heart count.
    private int latestHeartCount;
    // Cached timestamp of when the current regeneration cycle started.
    private long latestStartTime;

    /**
     * Called when the activity is first created.
     * Sets up click listeners and initial UI state.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Initialize activity state.
        super.onCreate(savedInstanceState);
        // Enable modern edge-to-edge display.
        EdgeToEdge.enable(this);
        // Load the XML layout.
        setContentView(R.layout.activity_main_menu);

        // Adjust layout padding to account for system bars (status/navigation).
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // --- PLAY BUTTON LOGIC ---
        // Transitions to the last unlocked level.
        ImageButton btnPlay = findViewById(R.id.btn_main_action);
        btnPlay.setOnClickListener(v -> {
            // Check current heart count before allowing gameplay.
            SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
            int hearts = prefs.getInt("heart_count", 5);

            if (hearts > 0) {
                // Determine the furthest level reached by the user.
                SharedPreferences progressPrefs = getSharedPreferences("game_progress", MODE_PRIVATE);
                int highestLevel = progressPrefs.getInt("unlocked_level", 1);

                // Start the Game activity with the calculated level.
                Intent gameIntent = new Intent(MainMenu.this, Game.class);
                gameIntent.putExtra("selected_level", highestLevel);
                startActivity(gameIntent);
            } else {
                // Inform user that they must wait for hearts to regenerate.
                Toast.makeText(MainMenu.this, "No hearts left! Please wait.", Toast.LENGTH_SHORT).show();
            }
        });

        // --- LEVELS BUTTON LOGIC ---
        // Navigates to the stage selection screen.
        ImageButton btnLevels = findViewById(R.id.btn_levels);
        btnLevels.setOnClickListener(v -> {
            startActivity(new Intent(MainMenu.this, Levels.class));
        });

        // --- SETTINGS BUTTON LOGIC ---
        // Navigates to the application configuration screen.
        ImageButton btnSettings = findViewById(R.id.btn_settings_main);
        btnSettings.setOnClickListener(v -> {
            startActivity(new Intent(MainMenu.this, Settings.class));
        });
    }

    /**
     * Triggered when the user returns to the Main Menu.
     * Ensures the heart count and timers are up to date.
     */
    @Override
    protected void onResume() {
        super.onResume();
        // Recalculate heart state and update UI.
        refreshHeartUI();
    }

    /**
     * Updates all UI elements related to the heart/life system.
     */
    private void refreshHeartUI() {
        // Perform backend calculations for time-based restoration.
        HeartCalculationLogic();
        
        // Find UI components.
        TextView tvTimer = findViewById(R.id.tv_full_lives);
        TextView tvLifeCount = findViewById(R.id.tv_life_count);

        // Update the numeric life counter.
        tvLifeCount.setText(String.valueOf(latestHeartCount));

        // Start a visible countdown if hearts are regenerating, otherwise show "FULL".
        if (latestStartTime != -1 && latestHeartCount < 5) {
            startUiTimer(latestStartTime, tvTimer);
        } else {
            tvTimer.setText(R.string.full);
        }
    }

    /**
     * CORE LOGIC: Heart Restoration System
     * - Calculates how many hearts should have been restored based on real-world time.
     * - Updates persistent storage (SharedPreferences) with new counts.
     * - Manages regeneration start times and background notifications.
     */
    public void HeartCalculationLogic() {
        // Access shared preferences for storage.
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Retrieve current state.
        long heartRenewStartTime = sharedPreferences.getLong("Heart_Renew_Start_Time", -1);
        int heartCount = sharedPreferences.getInt("heart_count", 5);

        // CASE 1: Hearts are already full.
        if (heartCount >= 5) {
            latestHeartCount = heartCount;
            latestStartTime = -1;
            // Clear any lingering restoration notifications.
            NotificationHelper.cancelNotification(this);
            return;
        }

        // CASE 2: Restoration timer was not active, start it now.
        if (heartRenewStartTime == -1) {
            heartRenewStartTime = System.currentTimeMillis();
            editor.putLong("Heart_Renew_Start_Time", heartRenewStartTime);
            editor.apply();

            // Schedule a notification for 30 minutes from now.
            long firstAlarm = heartRenewStartTime + THIRTY_MINUTES_IN_MILLIS;
            NotificationHelper.scheduleNextHeartNotification(this, firstAlarm);

            latestHeartCount = heartCount;
            latestStartTime = heartRenewStartTime;
            return;
        }

        // CASE 3: Timer is active, calculate how much time has passed since start.
        long elapsed = System.currentTimeMillis() - heartRenewStartTime;
        // Increment hearts for every 30-minute block that has passed.
        while (elapsed >= THIRTY_MINUTES_IN_MILLIS && heartCount < 5) {
            heartCount++;
            elapsed -= THIRTY_MINUTES_IN_MILLIS;
            heartRenewStartTime += THIRTY_MINUTES_IN_MILLIS;
        }

        // FINAL STATE UPDATE:
        if (heartCount >= 5) {
            // Cap at 5 hearts and stop the timer.
            editor.putInt("heart_count", 5);
            editor.putLong("Heart_Renew_Start_Time", -1);
            editor.apply();
            NotificationHelper.cancelNotification(this);
            latestHeartCount = 5;
            latestStartTime = -1;
        } else {
            // Save current count and the updated (incremented) start time.
            editor.putInt("heart_count", heartCount);
            editor.putLong("Heart_Renew_Start_Time", heartRenewStartTime);
            editor.apply();

            // Reschedule the notification for the next incremental heart.
            long nextAlarm = heartRenewStartTime + THIRTY_MINUTES_IN_MILLIS;
            NotificationHelper.scheduleNextHeartNotification(this, nextAlarm);

            latestHeartCount = heartCount;
            latestStartTime = heartRenewStartTime;
        }
    }

    /**
     * Initializes a visible countdown timer for the user interface.
     * @param targetTimestamp The timestamp when the current regeneration cycle started.
     * @param timerTextView The TextView where the countdown (MM:SS) will be displayed.
     */
    private void startUiTimer(long targetTimestamp, TextView timerTextView) {
        // Cancel any existing UI timer to prevent overlaps.
        if (heartTimer != null) heartTimer.cancel();

        // Calculate how much time is left in the current 30-minute cycle.
        long millisInFuture = (targetTimestamp + THIRTY_MINUTES_IN_MILLIS) - System.currentTimeMillis();

        // If the cycle should have already finished, refresh the whole UI logic.
        if (millisInFuture <= 0) {
            refreshHeartUI();
            return;
        }

        // Start a 1-second interval countdown timer.
        heartTimer = new CountDownTimer(millisInFuture, 1000) {
            /**
             * Triggered on every tick (1 second).
             * @param millisUntilFinished Remaining time in milliseconds.
             */
            @Override
            public void onTick(long millisUntilFinished) {
                // Convert total milliseconds to minutes and seconds format.
                long minutes = (millisUntilFinished / 1000) / 60;
                long seconds = (millisUntilFinished / 1000) % 60;
                // Update the UI label.
                timerTextView.setText(String.format("%02d:%02d", minutes, seconds));
            }

            /**
             * Triggered when the 30-minute cycle finishes.
             */
            @Override
            public void onFinish() {
                // Re-trigger the whole calculation logic to grant the heart and restart the cycle if needed.
                refreshHeartUI();
            }
        }.start();
    }

    /**
     * Triggered when the activity is no longer visible.
     * Stops the UI timer to conserve battery and CPU resources.
     */
    @Override
    protected void onStop() {
        super.onStop();
        if (heartTimer != null) heartTimer.cancel();
    }
}
