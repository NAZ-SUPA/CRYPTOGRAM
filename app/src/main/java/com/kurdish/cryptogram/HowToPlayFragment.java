/**
 * Package declaration for the Kurdish Cryptogram application.
 */
package com.kurdish.cryptogram;

/**
 * Android system and UI framework imports.
 */
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * AndroidX Fragment and DialogFragment imports.
 */
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

/** .
 * HowToPlayFragment:
 * - Displays an instructional manual for the game in a modal popup (Dialog) style.
 * - This fragment overlay helps users understand the game mechanics without leaving the Settings screen.
 * - Utilizes a simplified dismissal logic and custom window layout parameters.
 */
public class HowToPlayFragment extends DialogFragment {

    /**
     * Standard Fragment lifecycle method to inflate and configure the UI.
     * @param inflater Used to inflate the XML layout.
     * @param container Parent view (usually null for Dialogs, but provided for layout context).
     * @param savedInstanceState Saved state from previous execution.
     * @return The View for the instruction manual UI.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // --- UI INFLATION ---
        // Inflate the 'fragment_how_to_play' layout. 
        // This layout typically contains a card-based UI with the text instructions.
        View view = inflater.inflate(R.layout.fragment_how_to_play, container, false);

        // --- INTERACTION LOGIC ---
        // Locate the "Close" or "Got it" button in the inflated view.
        Button btnClose = view.findViewById(R.id.btnClose);
        
        // Setup click listener to dismiss the dialog.
        // dismiss() is a DialogFragment method that removes the fragment and closes the overlay.
        btnClose.setOnClickListener(v -> dismiss());

        // Return the prepared view to the system.
        return view;
    }

    /**
     * Standard lifecycle method called when the fragment becomes visible.
     * Used here to override the default dialog size behavior.
     */
    @Override
    public void onStart() {
        super.onStart();
        // Check if the internal Dialog and Window objects exist.
        if (getDialog() != null && getDialog().getWindow() != null) {
            // --- UI DIMENSIONING ---
            // Set the width to MATCH_PARENT to ensure the dialog spans the full screen width.
            // Set the height to WRAP_CONTENT to ensure the dialog only takes up as much vertical space as its text requires.
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
}
