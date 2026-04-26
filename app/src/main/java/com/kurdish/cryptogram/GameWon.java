/**
 * Package declaration for the Kurdish Cryptogram application.
 */
package com.kurdish.cryptogram;

/**
 * Android system and UI framework imports.
 */
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

/**
 * AndroidX compatibility and Fragment imports.
 */
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * GameWon Fragment:
 * - This fragment is displayed as an overlay when the player successfully solves a level.
 * - Shows the full solved sentence to the user.
 * - Provides navigation to the next level or back to the main menu.
 */
public class GameWon extends Fragment {

    // --- ARGUMENT KEYS ---
    // Keys used to extract data from the Bundle passed by the Game activity.
    private static final String ARG_CURRENT_LEVEL_INDEX = "current_level_index";
    private static final String ARG_FULL_SENTENCE = "full_sentence";

    /**
     * Standard Fragment lifecycle method to create and configure the UI.
     * @param inflater Used to inflate the XML layout.
     * @param container Parent view that the fragment UI will be attached to.
     * @param savedInstanceState Saved state from previous execution.
     * @return The View for the fragment's UI.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // --- UI INFLATION ---
        // Inflate the 'fragment_game_won' layout. This overlay dims the game screen.
        View view = inflater.inflate(R.layout.fragment_game_won, container, false);

        // --- UI BINDING ---
        // Locate buttons and labels within the inflated layout.
        Button btnNextLevel = view.findViewById(R.id.btn_next_level);
        Button btnHome = view.findViewById(R.id.btn_home_from_win);
        TextView tvMessage = view.findViewById(R.id.tv_win_message);
        TextView tvSentence = view.findViewById(R.id.tv_won_sentence);

        // --- DATA EXTRACTION ---
        // Default values in case arguments are missing.
        int currentLevelIndex = 0;
        String fullSentence = "";

        // Read arguments provided by Game.java during fragment transaction.
        if (getArguments() != null) {
            currentLevelIndex = getArguments().getInt(ARG_CURRENT_LEVEL_INDEX, 0);
            fullSentence = getArguments().getString(ARG_FULL_SENTENCE, "");
        }

        // --- CONTENT POPULATION ---
        // Display the fully solved sentence so the player can read the complete phrase.
        if (tvSentence != null) {
            tvSentence.setText(fullSentence);
        }

        // --- NAVIGATION LOGIC ---
        // Handle logic for the end-of-game scenario.
        // Index is zero-based; if it's the 9th level (index 8) or above, hide the 'Next Level' button.
        if (currentLevelIndex >= 8) {
            // Update message to inform the user that they have completed all current content.
            tvMessage.setText("You won!\nWait for the next season, we'll be back soon.");
            // Hide the primary navigation button as there is no next stage.
            btnNextLevel.setVisibility(View.GONE);
        } else {
            // Normal Level Progression:
            tvMessage.setText("Great job!");
            // Calculate the 1-based ID for the next stage (index + 2).
            final int nextLevelToLoad = currentLevelIndex + 2;

            // Setup the "Next Level" button listener.
            btnNextLevel.setOnClickListener(v -> {
                if (getActivity() != null) {
                    // Create an intent to reload the Game activity with the next level data.
                    Intent intent = new Intent(getActivity(), Game.class);
                    intent.putExtra("selected_level", nextLevelToLoad);
                    // Reset the activity task stack for a clean transition.
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    // Close the current activity.
                    getActivity().finish();
                }
            });
        }

        // --- HOME NAVIGATION ---
        // Return the user to the central Main Menu.
        btnHome.setOnClickListener(v -> {
            if (getActivity() != null) {
                Intent intent = new Intent(getActivity(), MainMenu.class);
                // Clear the back stack to prevent navigating back to this win screen.
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                // Close the current activity.
                getActivity().finish();
            }
        });

        // Return the fully prepared view to the system.
        return view;
    }
}
