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

        // --- NOTIFICATION STATE INITIALIZATION ---
        // Determine the actual ON/OFF state based on both saved preferences and system-level permissions.
        boolean isNotifOn;
        // Android 13 (Tiramisu, API 33) introduced a runtime permission for notifications.
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            // Check if the POST_NOTIFICATIONS permission is currently granted by the user.
            boolean permissionGranted = checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                    == android.content.pm.PackageManager.PERMISSION_GRANTED;
            // Notifications are considered "ON" only if the user enabled them in settings AND granted system permission.
            isNotifOn = prefs.getBoolean("notifications_on", false) && permissionGranted;
        } else {
            // For older Android versions, default to TRUE if no preference is saved.
            isNotifOn = prefs.getBoolean("notifications_on", true);
        }
        // Save the verified state back to preferences to ensure consistency.
        prefs.edit().putBoolean("notifications_on", isNotifOn).apply();
        // Update the visual appearance of the toggle buttons.
        updateNotifUI(isNotifOn);

        // --- NOTIFICATION TOGGLE LOGIC ---
        // Define a shared click listener for both the 'ON' and 'OFF' notification buttons.
        View.OnClickListener notifClick = v -> {
            // Determine which button was clicked.
            boolean isOn = (v.getId() == R.id.toggle_on);
            
            // If the user is trying to turn notifications ON on Android 13+.
            if (isOn && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                // If permission is not yet granted, request it from the user.
                if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                        != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
                    return; // Exit and wait for permission result.
                }
            }
            // Save the user's choice to SharedPreferences.
            prefs.edit().putBoolean("notifications_on", isOn).apply();
            // Refresh the UI to reflect the change.
            updateNotifUI(isOn);
        };
        // Attach the listener to the buttons.
        btnOn.setOnClickListener(notifClick);
        btnOff.setOnClickListener(notifClick);

        // --- EMAIL SUPPORT LOGIC ---
        // Setup listener for the contact support button.
        findViewById(R.id.btn_email).setOnClickListener(v -> {
            // Define email details.
            String recipient = "naz.feng0438@koyauniversity.org";
            String subject = "From Cryptogram App";
            // Construct a mailto URI string.
            String mailto = "mailto:" + recipient + "?subject=" + Uri.encode(subject);
            // Create an implicit intent to send an email.
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
            // Set the URI data for the intent.
            emailIntent.setData(Uri.parse(mailto));
            try {
                // Launch the system's email application chooser.
                startActivity(emailIntent);
            } catch (Exception e) {
                // Notify the user if no email application is available on the device.
                Toast.makeText(this, "No email app found", Toast.LENGTH_SHORT).show();
            }
        });

        // --- NAVIGATION LOGIC ---
        // Setup listener for the back button.
        findViewById(R.id.btn_back_home).setOnClickListener(v -> {
            // Create intent to go back to the Main Menu.
            Intent intent = new Intent(Settings.this, MainMenu.class);
            // Ensure the MainMenu activity is not duplicated in the stack.
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            // Finish this settings activity.
            finish();
        });
    }

    /**
     * Callback for the result of the runtime permission request.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Check if the request code matches our notification permission request.
        if (requestCode == 101) {
            // Verify if the user granted the permission.
            boolean granted = (grantResults.length > 0 && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED);
            // Update preferences and UI based on user's decision.
            prefs.edit().putBoolean("notifications_on", granted).apply();
            updateNotifUI(granted);
        }
    }

    /**
     * Updates the visual state (colors) of the On/Off buttons to reflect the current setting.
     * @param isOn True if notifications are enabled, false otherwise.
     */
    private void updateNotifUI(boolean isOn) {
        // Define color hex codes.
        int pink = Color.parseColor("#D1C4E9"); // Color for active state.
        int gray = Color.parseColor("#4A4D58"); // Color for inactive state.
        
        // Apply background tint colors based on the state.
        btnOn.setBackgroundTintList(ColorStateList.valueOf(isOn ? pink : gray));
        btnOff.setBackgroundTintList(ColorStateList.valueOf(!isOn ? pink : gray));
        
        // Apply text colors based on the state for better readability.
        btnOn.setTextColor(isOn ? Color.parseColor("#303240") : Color.WHITE);
        btnOff.setTextColor(!isOn ? Color.parseColor("#303240") : Color.WHITE);
    }
}
