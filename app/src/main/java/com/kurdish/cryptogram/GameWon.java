package com.kurdish.cryptogram;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class GameWon extends Fragment {

    // Argument keys used by Game.java when opening this fragment.
    // Keeping keys documented here helps avoid mismatch bugs between sender/receiver.
    private static final String ARG_CURRENT_LEVEL_INDEX = "current_level_index";
    private static final String ARG_FULL_SENTENCE = "full_sentence";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the win overlay UI. This fragment is shown on top of Game activity content.
        View view = inflater.inflate(R.layout.fragment_game_won, container, false);

        Button btnNextLevel = view.findViewById(R.id.btn_next_level);
        Button btnHome = view.findViewById(R.id.btn_home_from_win);
        TextView tvMessage = view.findViewById(R.id.tv_win_message);
        TextView tvSentence = view.findViewById(R.id.tv_won_sentence); // TARGET TEXTVIEW

        // Default fallback values keep UI safe even if arguments are missing.
        int currentLevelIndex = 0;
        String fullSentence = "";

        // Read values passed from Game.checkWinCondition() -> showGameWonFragment().
        if (getArguments() != null) {
            currentLevelIndex = getArguments().getInt(ARG_CURRENT_LEVEL_INDEX, 0);
            fullSentence = getArguments().getString(ARG_FULL_SENTENCE, "");
        }

        // Show the solved sentence so the player can review the full phrase after winning.
        if (tvSentence != null) {
            tvSentence.setText(fullSentence);
        }

        // End-of-content gate:
        // currentLevelIndex is zero-based, so >= 8 means level 9 and above are treated as
        // "last available" content in this version. In this state we hide "Next Level".
        if (currentLevelIndex >= 8) {
            tvMessage.setText("You won!\nWait for the next season, we'll be back soon.");

            // Hide the "Next Level" button so they only see the "Go Home" button
            btnNextLevel.setVisibility(View.GONE);
        } else {
            // Normal flow: congratulate and allow moving to the immediate next level.
            tvMessage.setText("Great job!");
            // +2 because index is zero-based while selected_level extra is one-based.
            final int nextLevelToLoad = currentLevelIndex + 2;

            btnNextLevel.setOnClickListener(v -> {
                if (getActivity() != null) {
                    Intent intent = new Intent(getActivity(), Game.class);
                    intent.putExtra("selected_level", nextLevelToLoad);
                    // Clear back stack so Back does not return to a completed level screen.
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    getActivity().finish();
                }
            });
        }

        btnHome.setOnClickListener(v -> {
            if (getActivity() != null) {
                Intent intent = new Intent(getActivity(), MainMenu.class);
                // Same stack-reset behavior when returning home from the win overlay.
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                getActivity().finish();
            }
        });

        return view;
    }
}