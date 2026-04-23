/**
 * Package declaration for the Kurdish Cryptogram application.
 */
package com.kurdish.cryptogram;

/**
 * Android system and UI framework imports.
 */
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

/**
 * AppCompat and Material Design imports.
 */
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;

/**
 * Settings Activity:
 * - Allows users to toggle notifications (handling Android 13+ permission requirements).
 * - Changes font scaling for the game board.
 * - Displays the How To Play fragment.
 * - Provides an interface to contact support via email.
 * - Handles navigation back to the main menu.
 */
public class Settings extends AppCompatActivity {
    // UI components for notification toggle (On/Off buttons).
    private MaterialButton btnOn;
    private MaterialButton btnOff;
    // Object for persistent data storage of user preferences.
    private SharedPreferences prefs;

    /**
     * Called when the activity is first created.
     * Initializes UI elements and sets up event listeners.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Call the superclass constructor.
        super.onCreate(savedInstanceState);
        // Set the layout for the settings screen.
        setContentView(R.layout.activity_settings);

        // Bind UI components to their respective XML IDs.
        btnOn = findViewById(R.id.toggle_on);
        btnOff = findViewById(R.id.toggle_off);
        // Initialize SharedPreferences with private mode (accessible only by this app).
        prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);

        // ==========================================
        // 1. NOTIFICATION LOGIC
        // ==========================================
        boolean isNotifOn;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            boolean permissionGranted = checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                    == android.content.pm.PackageManager.PERMISSION_GRANTED;
            isNotifOn = prefs.getBoolean("notifications_on", false) && permissionGranted;
        } else {
            isNotifOn = prefs.getBoolean("notifications_on", true);
        }
        prefs.edit().putBoolean("notifications_on", isNotifOn).apply();
        updateNotifUI(isNotifOn);

        View.OnClickListener notifClick = v -> {
            boolean isOn = (v.getId() == R.id.toggle_on);

            if (isOn && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                        != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
                    return;
                }
            }
            prefs.edit().putBoolean("notifications_on", isOn).apply();
            updateNotifUI(isOn);
        };
        btnOn.setOnClickListener(notifClick);
        btnOff.setOnClickListener(notifClick);

        // ==========================================
        // 2. FONT SIZE LOGIC
        // ==========================================
        MaterialButton f1 = findViewById(R.id.f_x1);
        MaterialButton f15 = findViewById(R.id.f_x15);
        MaterialButton f2 = findViewById(R.id.f_x2);

        if (f1 != null && f15 != null && f2 != null) {
            // Load saved font size (Default is 0)
            int savedFont = prefs.getInt("font_size", 0);
            updateFontUI(savedFont, f1, f15, f2);

            View.OnClickListener fontClick = v -> {
                int selectedIndex = 0;
                if (v.getId() == R.id.f_x15) selectedIndex = 1;
                else if (v.getId() == R.id.f_x2) selectedIndex = 2;

                // Save to SharedPreferences and update UI colors
                prefs.edit().putInt("font_size", selectedIndex).apply();
                updateFontUI(selectedIndex, f1, f15, f2);
            };

            f1.setOnClickListener(fontClick);
            f15.setOnClickListener(fontClick);
            f2.setOnClickListener(fontClick);
        }

        // ==========================================
        // 3. HOW TO PLAY FRAGMENT LOGIC
        // ==========================================
        View btnHowToPlay = findViewById(R.id.btn_how_to_play);
        if (btnHowToPlay != null) {
            btnHowToPlay.setOnClickListener(v -> {
                View container = findViewById(R.id.fragment_container);
                if (container != null) {
                    // Make the container visible
                    container.setVisibility(View.VISIBLE);

                    // Click listener to dismiss the fragment when tapping anywhere outside
                    container.setOnClickListener(closeView -> container.setVisibility(View.GONE));

                    // Inflate the fragment
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, new HowToPlayFragment())
                            .commit();
                }
            });
        }

        // ==========================================
        // 4. EMAIL SUPPORT LOGIC
        // ==========================================
        findViewById(R.id.btn_email).setOnClickListener(v -> {
            String recipient = "naz.feng0438@koyauniversity.org";
            String subject = "From Cryptogram App";
            String mailto = "mailto:" + recipient + "?subject=" + Uri.encode(subject);
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
            emailIntent.setData(Uri.parse(mailto));
            try {
                startActivity(emailIntent);
            } catch (Exception e) {
                Toast.makeText(this, "No email app found", Toast.LENGTH_SHORT).show();
            }
        });

        // ==========================================
        // 5. NAVIGATION LOGIC
        // ==========================================
        findViewById(R.id.btn_back_home).setOnClickListener(v -> {
            Intent intent = new Intent(Settings.this, MainMenu.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }

    /**
     * Callback for the result of the runtime permission request.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101) {
            boolean granted = (grantResults.length > 0 && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED);
            prefs.edit().putBoolean("notifications_on", granted).apply();
            updateNotifUI(granted);
        }
    }

    /**
     * Updates the visual state (colors) of the On/Off buttons.
     */
    private void updateNotifUI(boolean isOn) {
        int pink = Color.parseColor("#D1C4E9");
        int gray = Color.parseColor("#4A4D58");

        btnOn.setBackgroundTintList(ColorStateList.valueOf(isOn ? pink : gray));
        btnOff.setBackgroundTintList(ColorStateList.valueOf(!isOn ? pink : gray));

        btnOn.setTextColor(isOn ? Color.parseColor("#303240") : Color.WHITE);
        btnOff.setTextColor(!isOn ? Color.parseColor("#303240") : Color.WHITE);
    }

    /**
     * Updates the visual state (colors) of the Font Size buttons.
     */
    private void updateFontUI(int selectedIndex, MaterialButton f1, MaterialButton f15, MaterialButton f2) {
        if (f1 == null || f15 == null || f2 == null) return;

        int activeColor = Color.parseColor("#D1C4E9"); // Pink/Active
        int inactiveColor = Color.TRANSPARENT;         // Inactive

        f1.setBackgroundTintList(ColorStateList.valueOf(selectedIndex == 0 ? activeColor : inactiveColor));
        f15.setBackgroundTintList(ColorStateList.valueOf(selectedIndex == 1 ? activeColor : inactiveColor));
        f2.setBackgroundTintList(ColorStateList.valueOf(selectedIndex == 2 ? activeColor : inactiveColor));
    }
}