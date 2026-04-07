package com.kurdish.cryptogram;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * Settings Activity:
 * - Manages user preferences such as notifications and font sizes.
 * - Current Implementation: Focuses on visual toggle states (Local UI logic).
 * - Future Implementation: Will include SharedPreferences to persist these choices.
 */
public class Settings extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // UI SETUP LOGIC:
        // Enable edge-to-edge drawing to utilize the full screen area.
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);

        // SYSTEM INSET LOGIC:
        // Ensure that the settings content respects system bars (status/nav bars).
        // This prevents clickable elements from being overlapping by system gestures.
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // NOTIFICATION TOGGLE LOGIC:
        // This implements a "Segmented Control" behavior using two separate buttons.
        TextView tvOn = findViewById(R.id.toggle_on);
        TextView tvOff = findViewById(R.id.toggle_off);

        View.OnClickListener notifClick = v -> {
            // LOGIC: Only one state can be active. 
            // The 'isSelected' state is used by the background selector (XML) to change colors.
            boolean isOn = v.getId() == R.id.toggle_on;
            tvOn.setSelected(isOn);
            tvOff.setSelected(!isOn);

            // Note: Add SharedPreferences editor logic here to save the boolean state.
        };
        tvOn.setOnClickListener(notifClick);
        tvOff.setOnClickListener(notifClick);

        // FONT SIZE PRESET LOGIC:
        // Allows the user to select one of three specific font scaling factors.
        TextView f1 = findViewById(R.id.f_x1);
        TextView f15 = findViewById(R.id.f_x15);
        TextView f2 = findViewById(R.id.f_x2);

        View.OnClickListener fontClick = v -> {
            // LOGIC: Reset all presets to inactive, then activate the clicked one.
            f1.setSelected(v.getId() == R.id.f_x1);
            f15.setSelected(v.getId() == R.id.f_x15);
            f2.setSelected(v.getId() == R.id.f_x2);

            // Note: Add SharedPreferences logic here to save the selected scale factor (e.g., 1.0f, 1.5f, 2.0f).
        };
        f1.setOnClickListener(fontClick);
        f15.setOnClickListener(fontClick);
        f2.setOnClickListener(fontClick);

        // NAVIGATION LOGIC:
        // The HOME button in settings simply finishes the activity to return to the caller (MainMenu or Levels).
        findViewById(R.id.btn_back_home).setOnClickListener(v -> finish());

        // TUTORIAL HOOK:
        // Show HowToPlayFragment when button is clicked
        findViewById(R.id.btn_how_to_play).setOnClickListener(v -> {
            // Make fragment container visible
            findViewById(R.id.fragment_container).setVisibility(View.VISIBLE);

            // Open HowToPlayFragment
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new HowToPlayFragment())
                    .commit();
        });
    }
}