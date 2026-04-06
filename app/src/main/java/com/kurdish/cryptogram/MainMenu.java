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


// Main menu hub of the app: it presents the game options and routes the user to
// Game, Levels, or Settings based on the selected button.
public class MainMenu extends AppCompatActivity {

    // 30 minutes converted to milliseconds — used as the heart regeneration interval
    private static final long THIRTY_MINUTES_IN_MILLIS = 30 * 60 * 1000L;

    // Holds the active countdown timer so we can cancel it when the app goes to background
    private CountDownTimer heartTimer;

    // Shared fields: Heart() writes to these so onCreate can read
    // the results without opening SharedPreferences a second time
    private int latestHeartCount;
    private long latestStartTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Enable edge-to-edge drawing and inflate the main menu layout
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main_menu);

        // Push content down so it doesn't hide behind the status/navigation bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Play button — navigates to the Game screen
        ImageButton btnPlay = findViewById(R.id.btn_main_action);
        btnPlay.setOnClickListener(v -> {
            Intent intent = new Intent(MainMenu.this, Game.class);
            startActivity(intent);
        });

        // Levels button — navigates to the Levels screen
        ImageButton btnLevels = findViewById(R.id.btn_levels);
        btnLevels.setOnClickListener(v -> {
            Intent intent = new Intent(MainMenu.this, Levels.class);
            startActivity(intent);
        });

        // Settings button — navigates to the Settings screen
        ImageButton btnSettings = findViewById(R.id.btn_settings_main);
        btnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(MainMenu.this, Settings.class);
            startActivity(intent);
        });

        // ⬇️ TEMPORARY RESET — uncomment this line to wipe saved data and test from scratch
        // getSharedPreferences("MyPrefs", MODE_PRIVATE).edit().clear().apply();
        // ⬆️ REMOVE AFTER TESTING

        // Run the heart regeneration logic
        // This also fills latestHeartCount and latestStartTime so we don't read SharedPreferences twice
        Heart();

        TextView tvTimer = findViewById(R.id.tv_full_lives);
        TextView tvLifeCount = findViewById(R.id.tv_life_count);

        // Show the heart count that Heart() already calculated and stored in latestHeartCount
        tvLifeCount.setText(String.valueOf(latestHeartCount));

        // If a timer is active and hearts aren't full yet, start the visual countdown
        // Otherwise show "Full" since no regeneration is needed
        if (latestStartTime != -1 && latestHeartCount < 5) {
            startUiTimer(latestStartTime, tvTimer);
        } else {
            tvTimer.setText("Full");
        }
    }

    public void Heart() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Load the saved timer start time (-1 means no timer is currently running)
        long Heart_Renew_Start_Time = sharedPreferences.getLong("Heart_Renew_Start_Time", -1);

        // Load the saved heart count (defaults to 5 if the app was never opened before)
        int heart_number = sharedPreferences.getInt("heart_count", 5);

        TextView tv_life_count = findViewById(R.id.tv_life_count);

        // Hearts are already full — store the values for onCreate and exit early
        if (heart_number >= 5) {
            latestHeartCount = heart_number;
            latestStartTime = -1;
            return;
        }

        if (Heart_Renew_Start_Time != -1) {
            // Timer is running — calculate how many milliseconds have passed since it started
            long elapsed = System.currentTimeMillis() - Heart_Renew_Start_Time;

            // Catch-up loop: award 1 heart for every 30 mins the user was away
            // Example: away 90 mins with 2 hearts → gains 3 hearts (capped at 5)
            while (elapsed >= THIRTY_MINUTES_IN_MILLIS && heart_number < 5) {
                heart_number++;                                          // award 1 heart
                elapsed -= THIRTY_MINUTES_IN_MILLIS;                    // consume 30 mins from the bank
                Heart_Renew_Start_Time += THIRTY_MINUTES_IN_MILLIS;     // slide start time forward to preserve the remainder
            }

            // Update the heart count shown on screen
            tv_life_count.setText(String.valueOf(heart_number));

            // Save the updated heart count
            editor.putInt("heart_count", heart_number);

            // If hearts are now full, clear the timer (set to -1)
            // Otherwise save the slid-forward start time so the remainder carries over correctly
            editor.putLong("Heart_Renew_Start_Time", heart_number == 5 ? -1 : Heart_Renew_Start_Time);

            // Flag that a notification should fire because hearts were restored
            editor.putBoolean("notify_me", true);

            editor.apply();

        } else {
            // No timer is running yet — record the current time as the start of regeneration
            Heart_Renew_Start_Time = System.currentTimeMillis();
            editor.putLong("Heart_Renew_Start_Time", Heart_Renew_Start_Time);
            editor.apply();
        }

        // Write final values to fields so onCreate can read them without a second SharedPreferences call
        latestHeartCount = heart_number;
        latestStartTime = Heart_Renew_Start_Time;
    }

    private void startUiTimer(long targetTimestamp, TextView timerTextView) {
        // Cancel any existing timer first to prevent two timers ticking at the same time
        if (heartTimer != null) heartTimer.cancel();

        // Calculate how many milliseconds remain until the next heart is ready
        long millisInFuture = (targetTimestamp + THIRTY_MINUTES_IN_MILLIS) - System.currentTimeMillis();

        // If the deadline already passed (e.g. clock skew), skip the timer and show Full
        if (millisInFuture <= 0) {
            timerTextView.setText("Full");
            return;
        }

        // Start the visual countdown — ticks every 1 second
        heartTimer = new CountDownTimer(millisInFuture, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {
                // Convert remaining milliseconds into MM:SS display format
                long minutes = (millisUntilFinished / 1000) / 60;
                long seconds = (millisUntilFinished / 1000) % 60;
                timerTextView.setText(String.format("%02d:%02d", minutes, seconds));
            }

            @Override
            public void onFinish() {
                // 30 minutes are up — call Heart() to award the heart and update storage
                Heart();

                if (latestHeartCount < 5) {
                    // Hearts still not full — chain a new timer for the next heart
                    startUiTimer(latestStartTime, timerTextView);
                } else {
                    // Hearts are now full — stop the timer display
                    timerTextView.setText("Full");
                }
            }

        }.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Cancel the timer when the app goes to background to prevent memory leaks
        if (heartTimer != null) heartTimer.cancel();
    }
}