/**
 * Package declaration for the Kurdish Cryptogram application.
 */
package com.kurdish.cryptogram;

/**
 * Android system and UI framework imports.
 */
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

/**
 * AndroidX lifecycle and Fragment imports.
 */
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * GameOver Fragment:
 * - This fragment is displayed when the player makes too many mistakes in a level.
 * - Provides options to either replay the level or return to the main menu.
 * - Includes logic to prevent replaying if the player has no lives (hearts) remaining.
 */
public class GameOver extends Fragment {

    /**
     * Standard Fragment lifecycle method to inflate the fragment's UI.
     * @param inflater Used to inflate the XML layout.
     * @param container Parent view that the fragment UI will be attached to.
     * @param savedInstanceState Saved state from previous execution.
     * @return The View for the fragment's UI.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // --- UI INFLATION ---
        // Inflate the 'fragment_game_over' layout file into a View object.
        // This layout contains the "GAME OVER" message and navigation buttons.
        View view = inflater.inflate(R.layout.fragment_game_over, container, false);

        // --- REPLAY BUTTON SETUP ---
        // Locate the "Replay" button in the fragment layout.
        Button btnReplay = view.findViewById(R.id.btn_replay);
        
        // Define click behavior for the Replay button with resource check (Gatekeeper logic).
        btnReplay.setOnClickListener(v -> {
            // Ensure the fragment is currently attached to an Activity before accessing Context.
            if (getActivity() != null) {
                // Access shared preferences to check the current heart count.
                SharedPreferences prefs = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                int hearts = prefs.getInt("heart_count", 5);

                // --- LIFE VERIFICATION ---
                // Check if the player has at least one heart to start a new session.
                if (hearts > 0) {
                    // CASE: Enough hearts available.
                    // FIX: Get the Intent that originally started this game to find the current level
                    int currentLevel = getActivity().getIntent().getIntExtra("selected_level", 1);

                    Intent intent = new Intent(getActivity(), Game.class);
                    // FIX: Pass the level back into the new intent!
                    intent.putExtra("selected_level", currentLevel);

                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    getActivity().finish();
                } else {
                    // CASE: No hearts left.
                    // Notify the user of the resource shortage via a Toast message.
                    Toast.makeText(getActivity(), "Out of hearts!", Toast.LENGTH_SHORT).show();
                    // Forced navigation to the Main Menu since the game cannot be played.
                    Intent intent = new Intent(getActivity(), MainMenu.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    getActivity().finish();
                }
            }
        });

        // --- HOME BUTTON SETUP ---
        // Locate the "Home" button in the fragment layout.
        Button btnHome = view.findViewById(R.id.btn_home_from_game_over);
        
        // Define click behavior to return to the main menu.
        btnHome.setOnClickListener(v -> {
            if (getActivity() != null) {
                // Create intent to transition to the MainMenu activity.
                Intent intent = new Intent(getActivity(), MainMenu.class);
                // Clear the back stack to prevent navigating back to this failure screen.
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                // Close the current activity.
                getActivity().finish();
            }
        });

        // Return the fully configured view to the system.
        return view;
    }
}

