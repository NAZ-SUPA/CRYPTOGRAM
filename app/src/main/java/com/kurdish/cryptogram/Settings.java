package com.kurdish.cryptogram;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

// Settings screen controller:
// - keeps layout edge-to-edge safe with system inset padding
// - provides two local toggle groups (notifications and font scale presets)
// - returns to previous screen via HOME button
// Note: toggles currently update only visual selected state; persistence can be
// added later with SharedPreferences when settings need to survive app restarts.
public class Settings extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);

        // Apply system bar insets so the custom full-screen layout does not overlap
        // status/navigation bars on gesture-navigation devices.
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Notification toggle group behavior.
        // Clicking ON/OFF marks exactly one side as selected to mimic a segmented control.
        TextView tvOn = findViewById(R.id.toggle_on);
        TextView tvOff = findViewById(R.id.toggle_off);

        View.OnClickListener notifClick = v -> {
            // Keep single-selection state in sync with the tapped option.
            tvOn.setSelected(v.getId() == R.id.toggle_on);
            tvOff.setSelected(v.getId() == R.id.toggle_off);
        };
        tvOn.setOnClickListener(notifClick);
        tvOff.setOnClickListener(notifClick);

        // Font size preset group behavior.
        // Exactly one preset is visually active at a time (x1, x1.5, x2).
        TextView f1 = findViewById(R.id.f_x1);
        TextView f15 = findViewById(R.id.f_x15);
        TextView f2 = findViewById(R.id.f_x2);

        View.OnClickListener fontClick = v -> {
            // Reset all states, then activate only the selected size preset.
            f1.setSelected(v.getId() == R.id.f_x1);
            f15.setSelected(v.getId() == R.id.f_x15);
            f2.setSelected(v.getId() == R.id.f_x2);
        };
        f1.setOnClickListener(fontClick);
        f15.setOnClickListener(fontClick);
        f2.setOnClickListener(fontClick);

        // HOME closes Settings and returns to the previous screen in the stack.
        findViewById(R.id.btn_back_home).setOnClickListener(v -> finish());

        // Placeholder hook for tutorial/help flow.
        findViewById(R.id.btn_how_to_play).setOnClickListener(v -> {
            // TODO: Launch a dedicated How To Play screen or dialog.
        });
    }
}