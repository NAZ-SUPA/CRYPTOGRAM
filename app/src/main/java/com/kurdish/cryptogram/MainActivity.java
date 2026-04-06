package com.kurdish.cryptogram;

import android.content.Intent;
import android.graphics.Color;
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
 * MainActivity:
 * - Acts as the entry point and splash screen for the application.
 * - Handles the transition from a branded intro to the main menu.
 * - Performs dynamic text styling for the "CRYPTOGRAM" branding.
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // UI INITIALIZATION:
        // EdgeToEdge allows the layout to expand behind system bars for a premium look.
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        
        // Apply dynamic color spans to specific characters in the title and cipher.
        colorLetters();

        // SPLASH SCREEN LOGIC:
        // A Handler is used to delay the transition to the MainMenu.
        // This gives the user time to see the branding (2000ms = 2 seconds).
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // Create an explicit intent to move from MainActivity to MainMenu.
            Intent intent = new Intent(MainActivity.this, MainMenu.class);
            startActivity(intent);
            // Finish MainActivity so the user cannot navigate back to the splash screen.
            finish();
        }, 2000);

        // SYSTEM INSET LOGIC:
        // Set a listener to adjust padding based on the device's system bars (status bar, navigation bar).
        // This ensures that the "Welcome To" text or other top elements are not clipped.
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    /**
     * colorLetters Logic:
     * - Uses SpannableString to apply multiple colors to a single TextView string.
     * - This is more efficient and flexible than using multiple separate TextViews.
     */
    private void colorLetters() {
        // Target the main branding title.
        TextView tv = findViewById(R.id.cryptogram);
        String text = tv.getText().toString(); // "CRYPTOGRAM"
        
        // Target the sub-line containing numbers.
        TextView tv2 = findViewById(R.id.tv_cipher_numbers);
        String cipher = tv2.getText().toString();

        // TITLE STYLING:
        // Create a spannable wrapper around the "CRYPTOGRAM" string.
        SpannableString spannable = new SpannableString(text);

        // LOGIC: Highlight the two 'R' characters in black (#000000).
        // The rest of the string inherits the color from the XML style (@style/cryptogram).
        // Index 1: The 'R' in "CRYPTOGRAM" (C=0, R=1, Y=2...)
        spannable.setSpan(new ForegroundColorSpan(Color.parseColor("#000000")), 1, 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        // Index 7: The 'R' in "CRYPTOGRAM" (...O=6, R=7, A=8...)
        spannable.setSpan(new ForegroundColorSpan(Color.parseColor("#000000")), 7, 8, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        tv.setText(spannable);

        // CIPHER STYLING:
        // Repeat the logic for the numbers to show the relationship between letters and numbers.
        spannable = new SpannableString(cipher);

        // LOGIC: Highlight the numbers corresponding to the 'R' positions.
        // In "1 8 5 7 9 6 3 8 2 4", the '8' values are at specific character indices.
        spannable.setSpan(new ForegroundColorSpan(Color.parseColor("#000000")), 2, 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannable.setSpan(new ForegroundColorSpan(Color.parseColor("#000000")), 14, 15, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        tv2.setText(spannable);
    }
}