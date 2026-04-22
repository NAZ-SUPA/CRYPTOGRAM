/**
 * Package declaration for the Kurdish Cryptogram application.
 */
package com.kurdish.cryptogram;

/**
 * Android system imports for alarm management and notifications.
 */
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

/**
 * NotificationHelper:
 * - A utility class responsible for managing the application's notification system.
 * - Handles the creation of notification channels for Android O and above.
 * - Manages scheduling and canceling alarms that trigger heart regeneration notifications.
 * - Uses AlarmManager to ensure notifications fire even when the app is not actively running.
 */
public class NotificationHelper {

    // Unique identifier for the heart regeneration notification channel.
    public static final String CHANNEL_ID = "HeartRegenChannel";

    /**
     * Creates a notification channel required for Android 8.0 (Oreo) and higher.
     * This defines how notifications from this app are grouped and their priority.
     * @param context The application context.
     */
    public static void createNotificationChannel(Context context) {
        // Notification channels are only available on Android Oreo and above.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // User-visible name for the channel (shown in system settings).
            CharSequence name = "Heart Regeneration";
            // User-visible description for the channel.
            String description = "Notifications when your hearts regenerate";

            // Importance level determines how intrusive the notification is.
            // IMPORTANCE_HIGH allows for head-up notifications and sound/vibration.
            int importance = NotificationManager.IMPORTANCE_HIGH;

            // Initialize the NotificationChannel object.
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Enable vibration for this channel to ensure user awareness.
            channel.enableVibration(true); 
            // Ensure notifications can be seen on the lock screen.
            channel.setLockscreenVisibility(android.app.Notification.VISIBILITY_PUBLIC);

            // Register the channel with the system's NotificationManager.
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    /**
     * Schedules a future event using AlarmManager to notify the user when a heart is restored.
     * @param context The application context.
     * @param timeInMillis The exact system time (in milliseconds) when the notification should fire.
     */
    public static void scheduleNextHeartNotification(Context context, long timeInMillis) {
        // Get the system AlarmManager service.
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        // Create an intent pointing to the HeartNotificationReceiver class.
        Intent intent = new Intent(context, HeartNotificationReceiver.class);

        // Wrap the intent in a PendingIntent. 
        // FLAG_IMMUTABLE is required for security on modern Android versions.
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        if (alarmManager != null) {
            try {
                // Attempt to schedule an exact alarm that works even when the device is idle (Doze mode).
                // This requires the SCHEDULE_EXACT_ALARM permission on newer Android versions.
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
            } catch (SecurityException e) {
                // Fallback to a non-exact alarm if the system denies exact alarm scheduling.
                alarmManager.set(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
            }
        }
    }

    /**
     * Cancels any pending heart regeneration alarms.
     * Usually called when the player's hearts become full.
     * @param context The application context.
     */
    public static void cancelNotification(Context context) {
        // Get the system AlarmManager service.
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        // Create an intent identical to the one used for scheduling.
        Intent intent = new Intent(context, HeartNotificationReceiver.class);
        // Retrieve the corresponding PendingIntent.
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        
        // If the alarm manager exists, cancel the specific PendingIntent.
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
    }
}
