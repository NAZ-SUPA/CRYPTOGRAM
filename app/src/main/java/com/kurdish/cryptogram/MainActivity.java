/**
 * Package declaration for the Kurdish Cryptogram application.
 * This organizes the code into a specific namespace.
 */
package com.kurdish.cryptogram;

/**
 * Necessary imports for Android system classes, utilities, and UI components.
 */
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.Bundle;
import android.os.Looper;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * MainActivity serves as the entry point and splash screen for the Cryptogram game.
 * It handles initial data setup and visual branding transitions.
 */
public class MainActivity extends AppCompatActivity {

    /**
     * Called when the activity is first created.
     * This method initializes the UI, sets up game state, and manages the splash screen timing.
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down then this Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle).
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Call the superclass implementation to perform standard activity setup.
        super.onCreate(savedInstanceState);

        // Enable edge-to-edge display to utilize the full screen area including behind system bars.
        EdgeToEdge.enable(this);
        
        // Set the user interface layout for this activity from the XML resource.
        setContentView(R.layout.activity_main);

        // Access shared preferences to check if this is the application's first launch.
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        // Default to true if the "isFirstRun" key doesn't exist.
        boolean isFirstRun = prefs.getBoolean("isFirstRun", true);

        // Perform one-time initialization for a fresh installation.
        if (isFirstRun) {
            // Get an editor to modify the shared preferences.
            SharedPreferences.Editor editor = prefs.edit();
            // Initialize the player's starting heart (life) count to 5.
            editor.putInt("heart_count", 5);
            // Initialize heart renewal timer to -1 (inactive state).
            editor.putLong("Heart_Renew_Start_Time", -1);
            // Set first run flag to false so this block doesn't execute again.
            editor.putBoolean("isFirstRun", false);
            // Apply changes asynchronously to the persistent storage.
            editor.apply();
        }

        // Apply custom colors to specific characters in the title and subtitle for visual flair.
        colorLetters();

        // Create a splash screen effect using a Handler to delay the transition to the next screen.
        // Looper.getMainLooper() ensures the task runs on the main UI thread.
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // Create an explicit intent to transition from MainActivity to MainMenu.
            Intent intent = new Intent(MainActivity.this, MainMenu.class);
            // Start the MainMenu activity.
            startActivity(intent);
            // Finish MainActivity so the user cannot navigate back to the splash screen.
            finish();
        }, 2000); // 2000 milliseconds (2 seconds) duration for the splash screen.

        // Configure listener to handle system bar (status bar/navigation bar) insets for proper UI padding.
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            // Retrieve dimensions of system bars.
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            // Set view padding to ensure UI content doesn't overlap with system UI elements.
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            // Return the original insets to allow further processing by child views.
            return insets;
        });
    }

    /**
     * Enhances the visual appearance of the splash screen text by coloring specific letters.
     * Uses SpannableString for fine-grained control over text styling.
     */
    private void colorLetters() {
        // Find the TextView that displays the main app title "CRYPTOGRAM".
        TextView tv = findViewById(R.id.cryptogram);
        // Get the current text from the TextView.
        String text = tv.getText().toString();

        // Find the TextView that displays the subtitle or cipher number pattern.
        TextView tv2 = findViewById(R.id.tv_cipher_numbers);
        // Get the current text from the TextView.
        String cipher = tv2.getText().toString();

        // Create a SpannableString from the main title text to apply styles.
        SpannableString spannable = new SpannableString(text);
        // Apply black color to the second letter (index 1 to 2).
        spannable.setSpan(new ForegroundColorSpan(Color.parseColor("#000000")), 1, 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        // Apply black color to the eighth letter (index 7 to 8).
        spannable.setSpan(new ForegroundColorSpan(Color.parseColor("#000000")), 7, 8, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        // Set the styled spannable text back to the main title TextView.
        tv.setText(spannable);

        // Create a SpannableString from the cipher pattern text.
        spannable = new SpannableString(cipher);
        // Apply black color to the character at index 2.
        spannable.setSpan(new ForegroundColorSpan(Color.parseColor("#000000")), 2, 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        // Apply black color to the character at index 14.
        spannable.setSpan(new ForegroundColorSpan(Color.parseColor("#000000")), 14, 15, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        // Set the styled spannable text back to the subtitle TextView.
        tv2.setText(spannable);
    }
}
