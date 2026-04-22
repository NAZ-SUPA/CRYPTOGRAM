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
 * AndroidX lifecycle and Fragment imports.
 */
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * GameWon Fragment:
 * - This overlay is displayed when a player successfully completes a level.
 * - Displays a congratulatory message.
 * - Provides options to proceed to the next level or return to the main menu.
 * - Handles the "End of Season" logic when the maximum level is reached.
 */
public class GameWon extends Fragment {

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
        // Inflate the 'fragment_game_won' layout file into a View object.
        View view = inflater.inflate(R.layout.fragment_game_won, container, false);

        // --- UI BINDING ---
        // Locate the "Next Level" button within the inflated view.
        Button btnNextLevel = view.findViewById(R.id.btn_next_level);
        // Locate the "Home" button within the inflated view.
        Button btnHome = view.findViewById(R.id.btn_home_from_win);
        // Locate the TextView for the success message.
        TextView tvMessage = view.findViewById(R.id.tv_win_message);

        // --- LEVEL CONTEXT RETRIEVAL ---
        // Initialize current level index. Default to 0 if not provided.
        int currentLevelIndex = 0;
        // Retrieve the level index passed from the parent Game activity via Arguments.
        if (getArguments() != null) {
            currentLevelIndex = getArguments().getInt("current_level_index", 0);
        }

        // --- CONDITIONAL UI LOGIC BASED ON PROGRESS ---
        // Check if the user has reached the final level. 
        // currentLevelIndex of 9 represents Level 10 (since levels are 0-indexed in the data array).
        if (currentLevelIndex >= 9) {

            // CASE: Game Completed (Max Level Reached).
            // Update message to inform the user they have finished the current content.
            tvMessage.setText("You finished all levels!\nWait for the next season.");
            // Hide the "Next Level" button as there is no next level to load.
            btnNextLevel.setVisibility(View.GONE);

        } else {

            // CASE: Standard Level Won.
            // Display standard success text.
            tvMessage.setText("Great job! Ready for more?");

            // --- NEXT LEVEL TRANSITION LOGIC ---
            // Calculate the 1-based level number for the next stage.
            // If completed Level 1 (index 0), next level is 2.
            final int nextLevelToLoad = currentLevelIndex + 2;

            // Setup click listener for the "Next Level" button.
            btnNextLevel.setOnClickListener(v -> {
                if (getActivity() != null) {
                    // Create an explicit intent to restart the Game activity.
                    Intent intent = new Intent(getActivity(), Game.class);
                    // Pass the next level number to the intent.
                    intent.putExtra("selected_level", nextLevelToLoad);

                    // CLEAR TASK FLAG: Ensures the old game activity instance is removed and replaced by the new level.
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    // Start the new Game session.
                    startActivity(intent);
                    // Close the current activity.
                    getActivity().finish();
                }
            });
        }

        // --- HOME NAVIGATION LOGIC ---
        // Setup click listener for the "Home" button to return to the Main Menu.
        btnHome.setOnClickListener(v -> {
            if (getActivity() != null) {
                // Create intent to transition to the MainMenu activity.
                Intent intent = new Intent(getActivity(), MainMenu.class);
                // Clear the back stack so the user cannot navigate back to the 'Won' screen.
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                // Close the current activity.
                getActivity().finish();
            }
        });

        // Return the fully configured view to the Android system.
        return view;
    }
}
