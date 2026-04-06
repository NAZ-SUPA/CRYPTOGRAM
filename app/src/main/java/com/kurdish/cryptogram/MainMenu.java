package com.kurdish.cryptogram;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * MainMenu Activity:
 * - Serves as the primary navigation hub for the application.
 * - Manages the complex "Heart Regeneration System" including background catch-up logic
 *   and real-time UI countdowns.
 */
public class MainMenu extends AppCompatActivity {

    // REGENERATION CONSTANTS:
    // One heart is awarded every 30 minutes.
    private static final long THIRTY_MINUTES_IN_MILLIS = 30 * 60 * 1000L;

    // TIMER STATE:
    // Reference to the active countdown timer to allow for cancellation and prevent memory leaks.
    private CountDownTimer heartTimer;

    // BRIDGE FIELDS:
    // Temporary storage for calculated values to avoid redundant SharedPreferences reads.
    private int latestHeartCount;
    private long latestStartTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // UI INITIALIZATION:
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main_menu);

        // SYSTEM INSET LOGIC:
        // Adjust padding to avoid overlap with status/navigation bars.
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // NAVIGATION LOGIC:
        // Setup explicit intents for the three main navigation paths.
        
        // 1. Play -> Direct to Game
        ImageButton btnPlay = findViewById(R.id.btn_main_action);
        btnPlay.setOnClickListener(v -> {
            startActivity(new Intent(MainMenu.this, Game.class));
        });

        // 2. Levels -> Level Selection Grid
        ImageButton btnLevels = findViewById(R.id.btn_levels);
        btnLevels.setOnClickListener(v -> {
            startActivity(new Intent(MainMenu.this, Levels.class));
        });

        // 3. Settings -> App Configuration
        ImageButton btnSettings = findViewById(R.id.btn_settings_main);
        btnSettings.setOnClickListener(v -> {
            startActivity(new Intent(MainMenu.this, Settings.class));
        });

//        // ⬇️ This runs EVERY time the app opens — resets the timer to -1 every single time!
//        getSharedPreferences("MyPrefs", MODE_PRIVATE).edit()
//                .putInt("heart_count", 3)
//                .putLong("Heart_Renew_Start_Time", -1)  // ← THIS resets the timer every open
//                .apply();

    }

    /**
     * LIFE-CYCLE LOGIC:
     * onResume is the central trigger for heart logic. 
     * It ensures values are updated whenever the user returns to the menu 
     * (from another activity or from background).
     */
    @Override
    protected void onResume() {
        super.onResume();
        refreshHeartUI();
    }

    /**
     * UI SYNC LOGIC:
     * Orchestrates the calculation of hearts and the updating of the text displays.
     */
    private void refreshHeartUI() {
        // Run the math engine.
        HeartCalculationLogic();

        TextView tvTimer = findViewById(R.id.tv_full_lives);
        TextView tvLifeCount = findViewById(R.id.tv_life_count);

        // Update the numeric display.
        tvLifeCount.setText(String.valueOf(latestHeartCount));

        // UI TIMER LOGIC:
        // If hearts < 5 and a timer is active, start/resume the visual countdown.
        if (latestStartTime != -1 && latestHeartCount < 5) {
            startUiTimer(latestStartTime, tvTimer);
        } else {
            // Otherwise, set to a static "Full" state.
            tvTimer.setText(R.string.full);
        }
    }

    /**
     * HEART REGENERATION ENGINE (Logic Detail):
     * 1. Reads current heart count and the start time of the last regeneration cycle.
     * 2. Calculates elapsed time since the last update.
     * 3. Performs a "Catch-up Loop" to award hearts for time passed while the app was closed.
     * 4. Updates SharedPreferences with the new state.
     */
    public void HeartCalculationLogic() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        long heartRenewStartTime = sharedPreferences.getLong("Heart_Renew_Start_Time", -1);
        int heartCount = sharedPreferences.getInt("heart_count", 5);

        // EXIT CONDITION: If lives are full, no regeneration logic is needed.
        if (heartCount >= 5) {
            latestHeartCount = heartCount;
            latestStartTime = -1;
            return;
        }

        if (heartRenewStartTime != -1) {
            // CALCULATION: How many full 30-minute intervals have passed?
            long elapsed = System.currentTimeMillis() - heartRenewStartTime;

            // REGENERATION LOOP:
            while (elapsed >= THIRTY_MINUTES_IN_MILLIS && heartCount < 5) {
                heartCount++;                                      // Award 1 life.
                elapsed -= THIRTY_MINUTES_IN_MILLIS;                // Consume 30 mins from elapsed bank.
                heartRenewStartTime += THIRTY_MINUTES_IN_MILLIS; // Shift the "start" of the next cycle forward.
            }

            // PERSISTENCE: Save updated values.
            editor.putInt("heart_count", heartCount);
            // If full, stop the timer (-1). If not, save the adjusted start time for the next partial heart.
            editor.putLong("Heart_Renew_Start_Time", heartCount == 5 ? -1 : heartRenewStartTime);
            editor.putBoolean("notify_me", true); // Trigger for potential in-app notifications.
            editor.apply();

        } else {
            // INITIALIZATION LOGIC: 
            // If lives are missing but no timer is set, start a new 30-minute cycle starting NOW.
            heartRenewStartTime = System.currentTimeMillis();
            editor.putInt("heart_count", heartCount);
            editor.putLong("Heart_Renew_Start_Time", heartRenewStartTime);
            editor.apply();
        }

        // Shared bridge state for refreshHeartUI().
        latestHeartCount = heartCount;
        latestStartTime = heartRenewStartTime;
    }

    /**
     * VISUAL COUNTDOWN LOGIC:
     * - Uses a CountDownTimer to update the MM:SS display every second.
     * - targetTimestamp: The absolute time the current heart-regeneration interval started.
     */
    private void startUiTimer(long targetTimestamp, TextView timerTextView) {
        if (heartTimer != null) heartTimer.cancel(); // Prevent overlapping timers.

        // Calculate exact remaining milliseconds.
        long millisInFuture = (targetTimestamp + THIRTY_MINUTES_IN_MILLIS) - System.currentTimeMillis();

        if (millisInFuture <= 0) {
            // Safety: If time already passed, re-run logic to award the heart.
            refreshHeartUI();
            return;
        }

        heartTimer = new CountDownTimer(millisInFuture, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                // Formatting: Convert raw MS to MM:SS format.
                long minutes = (millisUntilFinished / 1000) / 60;
                long seconds = (millisUntilFinished / 1000) % 60;
                timerTextView.setText(String.format("%02d:%02d", minutes, seconds));
            }

            @Override
            public void onFinish() {
                // When 30 mins is up, re-trigger the sync logic to award the heart and potentially start the next timer.
                refreshHeartUI();
            }
        }.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Stop the visual ticker when the activity is not visible to save resources.
        if (heartTimer != null) heartTimer.cancel();
    }
}