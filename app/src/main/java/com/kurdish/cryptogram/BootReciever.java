/**
 * Package declaration for the Kurdish Cryptogram application.
 */
package com.kurdish.cryptogram;

/**
 * Android system imports for broadcast receiving and data persistence.
 */
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

/**
 * BootReceiver:
 * - This class acts as a system listener that wakes up automatically when the device finishes its boot process.
 * - It is responsible for rescheduling heart regeneration alarms that may have been cleared by a system reboot.
 * - Ensures that the background life-restoration logic remains consistent across device restarts.
 */
class BootReceiver extends BroadcastReceiver {

    /**
     * Triggered by the Android system when a broadcast event occurs.
     * @param context The Context in which the receiver is running.
     * @param intent The Intent being received (should be ACTION_BOOT_COMPLETED).
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        // --- EVENT VERIFICATION ---
        // Safety check: only proceed if the system broadcast is specifically the "boot completed" event.
        if (!Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) return;

        // --- STATE RETRIEVAL ---
        // Access the application's saved preferences to determine the state of player lives before the shutdown.
        SharedPreferences prefs = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        // Retrieve the last known heart count.
        int currentHearts = prefs.getInt("heart_count", 5);
        // Retrieve the timestamp when the current regeneration cycle started (-1 if no cycle is active).
        long startTime = prefs.getLong("Heart_Renew_Start_Time", -1);

        // --- RESCHEDULING LOGIC ---
        // We only need to re-register an alarm if the hearts were not already full and a timer was active.
        if (currentHearts < 5 && startTime != -1) {
            // Define the heart regeneration interval (30 minutes).
            final long REGEN_INTERVAL = 30 * 60 * 1000L; 
            
            // Calculate how much real-world time has passed since the timer first started.
            long elapsed = System.currentTimeMillis() - startTime; 
            
            // Determine how many heart increments should have occurred during the elapsed time (including downtime).
            int heartsRegenerated = (int) (elapsed / REGEN_INTERVAL); 
            
            // Calculate the exact timestamp when the NEXT incremental heart should be granted.
            long nextTrigger = startTime + ((heartsRegenerated + 1) * REGEN_INTERVAL); 

            // --- EDGE CASE HANDLING ---
            // If the calculated next heart time is in the past (e.g., it expired while the phone was off),
            // schedule the notification to trigger in 5 seconds to provide immediate feedback to the user.
            if (nextTrigger < System.currentTimeMillis()) {
                nextTrigger = System.currentTimeMillis() + 5000;
            }

            // --- ALARM REGISTRATION ---
            // Re-register the alarm with the system AlarmManager via the NotificationHelper utility.
            // This ensures the HeartNotificationReceiver will be triggered at the correct time.
            NotificationHelper.scheduleNextHeartNotification(context, nextTrigger);
        }
    }
}
