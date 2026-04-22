/**
 * Package declaration for the Kurdish Cryptogram application.
 */
package com.kurdish.cryptogram;

/**
 * Standard Android imports for permissions, intents, and data persistence.
 */
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;

/**
 * AndroidX compatibility imports for notifications and permissions.
 */
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

/**
 * HeartNotificationReceiver:
 * - This BroadcastReceiver is triggered by an alarm when a heart has regenerated.
 * - It handles the user-facing notification logic (sound, vibration, and display).
 * - It manages the chain of alarms to ensure sequential hearts are also notified until the limit is reached.
 */
public class HeartNotificationReceiver extends BroadcastReceiver {

    // Constant defining the heart regeneration interval (30 minutes). 
    // This must be synchronized with the logic in MainMenu.java.
    private static final long THIRTY_MINUTES_IN_MILLIS = 30 * 60 * 1000L; 

    /**
     * Triggered by the system AlarmManager when a scheduled alarm occurs.
     * @param context The application context.
     * @param intent The intent received from the alarm.
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        // Logging for debugging purposes in Logcat.
        android.util.Log.d("HeartTest", "Receiver Woke Up!");

        // --- STEP 1: PREFERENCE CHECK ---
        // Access shared preferences to check if the user has opted-in for notifications.
        SharedPreferences prefs = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        boolean notificationsOn = prefs.getBoolean("notifications_on", true);
        if (!notificationsOn) {
            android.util.Log.d("HeartTest", "Notifications disabled in settings");
            return; // Abort if user disabled notifications.
        }

        // --- STEP 2: PERMISSION CHECK ---
        // For Android 13 (API 33) and above, verify if the app still has permission to post notifications.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                android.util.Log.d("HeartTest", "No notification permission!");
                return; // Abort if permission was revoked by the user.
            }
        }

        // --- STEP 3: NOTIFICATION DISPATCH ---
        // Ensure the notification channel exists (required for Android 8.0+).
        NotificationHelper.createNotificationChannel(context);

        // Create an intent to open the app (MainActivity) when the notification is tapped.
        Intent mainIntent = new Intent(context, MainActivity.class);
        // Clear activity stack to ensure a clean launch.
        mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        
        // Wrap the intent in a PendingIntent for the notification builder.
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, mainIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Build the notification with branding and feedback settings.
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NotificationHelper.CHANNEL_ID)
                .setSmallIcon(android.R.drawable.stat_notify_chat) // Standard notification icon.
                .setContentTitle("Heart Regenerated! ❤️") // Notification Title.
                .setContentText("You have received a new heart.") // Notification Body.
                .setPriority(NotificationCompat.PRIORITY_HIGH) // High priority for heads-up display.
                .setDefaults(NotificationCompat.DEFAULT_ALL) // Enable default sound and vibration.
                .setContentIntent(pendingIntent) // Set the action when clicked.
                .setAutoCancel(true); // Remove notification automatically after click.

        // Get the notification manager and display the notification with a unique ID (1001).
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        android.util.Log.d("HeartTest", "Attempting to notify...");
        notificationManager.notify(1001, builder.build());

        // --- STEP 4: SEQUENTIAL ALARM MANAGEMENT ---
        // Retrieve current regeneration metadata.
        long startTime = prefs.getLong("Heart_Renew_Start_Time", -1);
        int currentHearts = prefs.getInt("heart_count", 5);

        // Check if there are more hearts remaining to be restored.
        if (startTime != -1 && currentHearts < 5) {
            // Calculate how much time has passed since the very first heart of this cycle was lost.
            long elapsed = System.currentTimeMillis() - startTime;
            // Determine how many heart units have been completed.
            int heartsRegenerated = (int) (elapsed / THIRTY_MINUTES_IN_MILLIS);
            // Predict if we still have gaps after this regeneration event.
            int projectedHearts = currentHearts + heartsRegenerated + 1;

            if (projectedHearts < 5) {
                // If the player will still be under the max (5), schedule an alarm for the NEXT heart.
                long nextTrigger = startTime + ((heartsRegenerated + 1) * THIRTY_MINUTES_IN_MILLIS);
                NotificationHelper.scheduleNextHeartNotification(context, nextTrigger);
                android.util.Log.d("HeartTest", "Next alarm scheduled.");
            } else {
                // Hearts are full or about to be full; no further alarms needed.
                android.util.Log.d("HeartTest", "Hearts will be full after this. No more alarms.");
            }
        }
    }
}
