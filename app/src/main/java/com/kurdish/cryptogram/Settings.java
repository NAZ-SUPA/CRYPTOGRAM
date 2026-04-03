package com.kurdish.cryptogram;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Settings extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);

        // Handle System Bar Insets (Status bar/Navigation bar padding)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 1. Notification Toggle Logic
        TextView tvOn = findViewById(R.id.toggle_on);
        TextView tvOff = findViewById(R.id.toggle_off);

        View.OnClickListener notifClick = v -> {
            // Set highlighed color to the one clicked
            tvOn.setSelected(v.getId() == R.id.toggle_on);
            tvOff.setSelected(v.getId() == R.id.toggle_off);
        };
        tvOn.setOnClickListener(notifClick);
        tvOff.setOnClickListener(notifClick);

        // 2. Font Size Toggle Logic
        TextView f1 = findViewById(R.id.f_x1);
        TextView f15 = findViewById(R.id.f_x15);
        TextView f2 = findViewById(R.id.f_x2);

        View.OnClickListener fontClick = v -> {
            // Reset all and only select the clicked one
            f1.setSelected(v.getId() == R.id.f_x1);
            f15.setSelected(v.getId() == R.id.f_x15);
            f2.setSelected(v.getId() == R.id.f_x2);
        };
        f1.setOnClickListener(fontClick);
        f15.setOnClickListener(fontClick);
        f2.setOnClickListener(fontClick);

        // 3. Navigation Buttons
        findViewById(R.id.btn_back_home).setOnClickListener(v -> finish());

        findViewById(R.id.btn_how_to_play).setOnClickListener(v -> {
            // Add your "How to Play" logic or Intent here
        });
    }
}