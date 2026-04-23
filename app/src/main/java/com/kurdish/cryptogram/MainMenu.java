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
import android.os.CountDownTimer;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

/**
 * AndroidX compatibility imports.
 */
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * MainMenu Activity:
 * - Provides the primary navigation hub of the application.
 * - Manages the life (heart) regeneration system and its associated UI timer.
 * - Handles transitions to Game, Levels, and Settings screens.
 */
public class MainMenu extends AppCompatActivity {

    // Constant defining the heart regeneration interval (30 minutes).
    private static final long THIRTY_MINUTES_IN_MILLIS = 30 * 60 * 1000L;

    // Timer object to update the countdown UI in real-time.
    private CountDownTimer heartTimer;
    // Cached heart count for UI updates.
    private int latestHeartCount;
    // Cached start time of the current regeneration cycle.
    private long latestStartTime;

    // SharedPreferences file names and keys used by the heart/progress systems.
    // Centralizing these values as constants reduces typo risk across methods.
    private static final String PREF_HEARTS = "MyPrefs";
    private static final String KEY_HEART_COUNT = "heart_count";
    private static final String KEY_HEART_RENEW_START_TIME = "Heart_Renew_Start_Time";

    /**
     * Initializes the activity and sets up navigation button listeners.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Standard activity lifecycle setup.
        super.onCreate(savedInstanceState);

        // Enable edge-to-edge layout for immersive experience.
        EdgeToEdge.enable(this);
        // Set the layout for the main menu.
        setContentView(R.layout.activity_main_menu);

        // Adjust padding to accommodate system bars (status/navigation).
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // --- PLAY BUTTON SETUP ---
        ImageButton btnPlay = findViewById(R.id.btn_main_action);
        btnPlay.setOnClickListener(v -> {
            // Check current heart count before allowing the user to start a game.
            SharedPreferences prefs = getSharedPreferences(PREF_HEARTS, MODE_PRIVATE);
            int hearts = prefs.getInt(KEY_HEART_COUNT, 5);

            if (hearts > 0) {
                // Retrieve the highest unlocked level from progress SharedPreferences.
                SharedPreferences progressPrefs = getSharedPreferences("game_progress", MODE_PRIVATE);
                int highestLevel = progressPrefs.getInt("unlocked_level", 1);

                // Create the intent and pass the highest level to the Game activity.
                Intent gameIntent = new Intent(MainMenu.this, Game.class);
                gameIntent.putExtra("selected_level", highestLevel);

                // Proceed to Game activity if at least one heart is available.
                startActivity(gameIntent);
            } else {
                // Block entry and notify user if out of lives.
                Toast.makeText(MainMenu.this, "No hearts left! Please wait.", Toast.LENGTH_SHORT).show();
            }
        });

        // --- LEVELS BUTTON SETUP ---
        ImageButton btnLevels = findViewById(R.id.btn_levels);
        btnLevels.setOnClickListener(v -> {
            // Navigate to the Level selection screen.
            startActivity(new Intent(MainMenu.this, Levels.class));
        });

        // --- SETTINGS BUTTON SETUP ---
        ImageButton btnSettings = findViewById(R.id.btn_settings_main);
        btnSettings.setOnClickListener(v -> {
            // Navigate to the Settings screen.
            startActivity(new Intent(MainMenu.this, Settings.class));
        });
    }

    /**
     * Called when the activity returns to the foreground.
     * Ensures the heart count and timers are synchronized with current time.
     */
    @Override
    protected void onResume() {
        super.onResume();
        // Recalculate heart state and update UI.
        refreshHeartUI();
    }

    /**
     * Updates the heart count display and manages the visibility of the countdown timer.
     */
    private void refreshHeartUI() {
        // Execute the background logic to calculate regeneration progress.
        HeartCalculationLogic();

        // Locate UI components for life display.
        TextView tvTimer = findViewById(R.id.tv_full_lives);
        TextView tvLifeCount = findViewById(R.id.tv_life_count);

        // Set the numeric count of current hearts.
        tvLifeCount.setText(String.valueOf(latestHeartCount));

        // If hearts are not full, start or resume the visual countdown timer.
        if (latestStartTime != -1 && latestHeartCount < 5) {
            startUiTimer(latestStartTime, tvTimer);
        } else {
            // Show "FULL" text if player has maximum lives.
            tvTimer.setText(R.string.full);
        }
    }

    /**
     * Core logic for calculating heart regeneration based on elapsed time.
     * Updates SharedPreferences and manages system-wide notifications.
     */
    public void HeartCalculationLogic() {
        // Reads and mutates persistent heart state so it survives app restarts.
        SharedPreferences sharedPreferences = getSharedPreferences(PREF_HEARTS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Retrieve saved regeneration metadata.
        long heartRenewStartTime = sharedPreferences.getLong(KEY_HEART_RENEW_START_TIME, -1);
        int heartCount = sharedPreferences.getInt(KEY_HEART_COUNT, 5);

        // CASE 1: Hearts are already full.
        if (heartCount >= 5) {
            latestHeartCount = heartCount;
            latestStartTime = -1;
            // Cancel any pending "heart restored" notifications.
            NotificationHelper.cancelNotification(this);
            return;
        }

        // CASE 2: No timer is active, but hearts aren't full. Start a new cycle.
        if (heartRenewStartTime == -1) {
            heartRenewStartTime = System.currentTimeMillis();
            editor.putLong(KEY_HEART_RENEW_START_TIME, heartRenewStartTime);
            editor.apply();

            // Schedule the first notification alarm for 30 minutes from now.
            long firstAlarm = heartRenewStartTime + THIRTY_MINUTES_IN_MILLIS;
            NotificationHelper.scheduleNextHeartNotification(this, firstAlarm);

            latestHeartCount = heartCount;
            latestStartTime = heartRenewStartTime;
            return;
        }

        // CASE 3: Timer is active. Calculate how many 30-minute intervals have passed since last check.
        long elapsed = System.currentTimeMillis() - heartRenewStartTime;
        while (elapsed >= THIRTY_MINUTES_IN_MILLIS && heartCount < 5) {
            // Catch-up loop: grants all hearts that should have regenerated while app was away.
            heartCount++;
            elapsed -= THIRTY_MINUTES_IN_MILLIS;
            heartRenewStartTime += THIRTY_MINUTES_IN_MILLIS; // Move start time forward by intervals.
        }

        // Handle final state after catch-up calculation.
        if (heartCount >= 5) {
            // All hearts successfully restored. Reset state.
            editor.putInt(KEY_HEART_COUNT, 5);
            editor.putLong(KEY_HEART_RENEW_START_TIME, -1);
            editor.apply();
            NotificationHelper.cancelNotification(this);
            latestHeartCount = 5;
            latestStartTime = -1;
        } else {
            // Hearts still regenerating. Save partial progress.
            editor.putInt(KEY_HEART_COUNT, heartCount);
            editor.putLong(KEY_HEART_RENEW_START_TIME, heartRenewStartTime);
            editor.apply();

            // Reschedule notification for the next incremental heart restoration.
            long nextAlarm = heartRenewStartTime + THIRTY_MINUTES_IN_MILLIS;
            NotificationHelper.scheduleNextHeartNotification(this, nextAlarm);

            latestHeartCount = heartCount;
            latestStartTime = heartRenewStartTime;
        }
    }

    /**
     * Starts a CountDownTimer to update the "mm:ss" display on the main menu.
     * @param targetTimestamp The timestamp when the current regeneration cycle started.
     * @param timerTextView The TextView where the countdown should be displayed.
     */
    private void startUiTimer(long targetTimestamp, TextView timerTextView) {
        // Cancel existing timer to avoid overlaps.
        if (heartTimer != null) heartTimer.cancel();

        // Calculate remaining milliseconds until the next heart is restored.
        long millisInFuture = (targetTimestamp + THIRTY_MINUTES_IN_MILLIS) - System.currentTimeMillis();

        // If time already passed, refresh the whole UI state.
        if (millisInFuture <= 0) {
            refreshHeartUI();
            return;
        }

        // Initialize and start the Android CountDownTimer.
        heartTimer = new CountDownTimer(millisInFuture, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                // Convert remaining milliseconds to minutes and seconds.
                long minutes = (millisUntilFinished / 1000) / 60;
                long seconds = (millisUntilFinished / 1000) % 60;
                // Update the text view with formatted time.
                timerTextView.setText(String.format("%02d:%02d", minutes, seconds));
            }

            @Override
            public void onFinish() {
                // Refresh UI when the timer hits zero to increment heart count.
                refreshHeartUI();
            }
        }.start();
    }

    /**
     * Called when the activity is no longer visible.
     * Stops the timer to save system resources.
     */
    @Override
    protected void onStop() {
        super.onStop();
        if (heartTimer != null) heartTimer.cancel();
    }
}