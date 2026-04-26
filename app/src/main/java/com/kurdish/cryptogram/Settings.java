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
 * AndroidX compatibility and Material UI imports.
 */
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;

/**
 * Settings Activity:
 * - Allows users to configure application preferences such as notifications and font sizes.
 * - Provides access to the "How to Play" tutorial and developer support via email.
 */
public class Settings extends AppCompatActivity {
    
    // --- UI COMPONENTS ---
    // Toggle buttons for enabling or disabling notifications.
    private MaterialButton btnOn;
    private MaterialButton btnOff;
    
    // --- DATA PERSISTENCE ---
    // Reference to application-wide preferences.
    private SharedPreferences prefs;

    /**
     * Initializes the activity and sets up event listeners for settings controls.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Standard activity lifecycle initialization.
        super.onCreate(savedInstanceState);
        // Bind the activity to its layout resource.
        setContentView(R.layout.activity_settings);

        // Link Java objects to XML views.
        btnOn = findViewById(R.id.toggle_on);
        btnOff = findViewById(R.id.toggle_off);
        // Initialize preferences access.
        prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);

        // --- NOTIFICATION LOGIC ---
        // Determines the initial state of the notification toggle.
        boolean isNotifOn;
        // Check for specific permission handling on Android 13 (Tiramisu) and above.
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            // Notifications are only 'ON' if the user opted in AND the system permission is granted.
            boolean permissionGranted = checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                    == android.content.pm.PackageManager.PERMISSION_GRANTED;
            isNotifOn = prefs.getBoolean("notifications_on", false) && permissionGranted;
        } else {
            // For older versions, default to TRUE unless the user previously disabled them.
            isNotifOn = prefs.getBoolean("notifications_on", true);
        }
        // Sync the preference with the actual permission state.
        prefs.edit().putBoolean("notifications_on", isNotifOn).apply();
        // Update the visual state of the ON/OFF buttons.
        updateNotifUI(isNotifOn);

        /**
         * Unified click listener for notification toggle buttons.
         */
        View.OnClickListener notifClick = v -> {
            // Check which button was clicked.
            boolean isOn = (v.getId() == R.id.toggle_on);

            // Handle system permission request if user tries to turn notifications ON.
            if (isOn && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                        != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    // Trigger the system permission dialog.
                    requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
                    return;
                }
            }
            // Save the user's choice and update the UI.
            prefs.edit().putBoolean("notifications_on", isOn).apply();
            updateNotifUI(isOn);
        };
        // Assign the listener to both buttons.
        btnOn.setOnClickListener(notifClick);
        btnOff.setOnClickListener(notifClick);

        // --- FONT SIZE LOGIC ---
        // Find font size selector buttons.
        MaterialButton f1 = findViewById(R.id.f_x1);
        MaterialButton f15 = findViewById(R.id.f_x15);
        MaterialButton f2 = findViewById(R.id.f_x2);

        if (f1 != null && f15 != null && f2 != null) {
            // Retrieve and apply the saved font size setting (0, 1, or 2).
            int savedFont = prefs.getInt("font_size", 0);
            updateFontUI(savedFont, f1, f15, f2);

            /**
             * Click listener for font size selection.
             */
            View.OnClickListener fontClick = v -> {
                int selectedIndex = 0; // Default: small
                if (v.getId() == R.id.f_x15) selectedIndex = 1; // Medium
                else if (v.getId() == R.id.f_x2) selectedIndex = 2; // Large

                // Persist the choice and update highlights.
                prefs.edit().putInt("font_size", selectedIndex).apply();
                updateFontUI(selectedIndex, f1, f15, f2);
            };

            f1.setOnClickListener(fontClick);
            f15.setOnClickListener(fontClick);
            f2.setOnClickListener(fontClick);
        }

        // --- HOW TO PLAY FRAGMENT LOGIC ---
        // Handles the interactive tutorial overlay.
        View btnHowToPlay = findViewById(R.id.btn_how_to_play);
        if (btnHowToPlay != null) {
            btnHowToPlay.setOnClickListener(v -> {
                View container = findViewById(R.id.fragment_container);
                if (container != null) {
                    // Reveal the fragment container.
                    container.setVisibility(View.VISIBLE);
                    // Close the tutorial if the user taps the background area.
                    container.setOnClickListener(closeView -> container.setVisibility(View.GONE));
                    // Inflate and show the tutorial fragment.
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, new HowToPlayFragment())
                            .commit();
                }
            });
        }

        // --- EMAIL SUPPORT LOGIC ---
        // Triggers the device's mail client to contact support.
        findViewById(R.id.btn_email).setOnClickListener(v -> {
            String recipient = "naz.feng0438@koyauniversity.org";
            String subject = "From Cryptogram App";
            // Construct a mailto URI with recipient and pre-filled subject.
            String mailto = "mailto:" + recipient + "?subject=" + Uri.encode(subject);
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
            emailIntent.setData(Uri.parse(mailto));
            try {
                // Launch the system email picker.
                startActivity(emailIntent);
            } catch (Exception e) {
                // Error handling if no email app is installed (e.g., in some emulators).
                Toast.makeText(this, "No email app found", Toast.LENGTH_SHORT).show();
            }
        });

        // --- NAVIGATION LOGIC ---
        // Returns the user to the Main Menu.
        findViewById(R.id.btn_back_home).setOnClickListener(v -> {
            Intent intent = new Intent(Settings.this, MainMenu.class);
            // Ensure we don't create multiple instances of the Main Menu.
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            // Close the Settings activity.
            finish();
        });
    }

    /**
     * Callback for the result of system permission requests.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101) {
            // Check if the user granted notification permission.
            boolean granted = (grantResults.length > 0 && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED);
            // Update preference and UI based on the user's response.
            prefs.edit().putBoolean("notifications_on", granted).apply();
            updateNotifUI(granted);
        }
    }

    /**
     * Updates the colors and text styling of the notification toggle buttons.
     * @param isOn TRUE if notifications are currently enabled.
     */
    private void updateNotifUI(boolean isOn) {
        // Define colors based on application theme.
        int pink = Color.parseColor("#D1C4E9"); // Active accent
        int gray = Color.parseColor("#4A4D58"); // Inactive background

        // Apply background tints.
        btnOn.setBackgroundTintList(ColorStateList.valueOf(isOn ? pink : gray));
        btnOff.setBackgroundTintList(ColorStateList.valueOf(!isOn ? pink : gray));

        // Adjust text color for readability against the background.
        btnOn.setTextColor(isOn ? Color.parseColor("#303240") : Color.WHITE);
        btnOff.setTextColor(!isOn ? Color.parseColor("#303240") : Color.WHITE);
    }

    /**
     * Updates the visual highlight for the selected font size.
     * @param selectedIndex The index of the selected size (0, 1, or 2).
     */
    private void updateFontUI(int selectedIndex, MaterialButton f1, MaterialButton f15, MaterialButton f2) {
        if (f1 == null || f15 == null || f2 == null) return;

        int activeColor = Color.parseColor("#D1C4E9"); // Pink/Active highlight
        int inactiveColor = Color.TRANSPARENT;         // Invisible background for inactive items

        // Update background colors for all three buttons.
        f1.setBackgroundTintList(ColorStateList.valueOf(selectedIndex == 0 ? activeColor : inactiveColor));
        f15.setBackgroundTintList(ColorStateList.valueOf(selectedIndex == 1 ? activeColor : inactiveColor));
        f2.setBackgroundTintList(ColorStateList.valueOf(selectedIndex == 2 ? activeColor : inactiveColor));
    }
}
