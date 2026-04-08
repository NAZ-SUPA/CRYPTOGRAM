package com.kurdish.cryptogram;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

/**
 * HowToPlayFragment:
 * - Shows instructions for the game in a modal popup style.
 * - Fully uses string resources from strings.xml.
 * - Close button dismisses the fragment.
 */
public class HowToPlayFragment extends DialogFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the popup layout as a modal-like card over a dimmed background.
        // `container` is provided by the host FrameLayout in Settings.
        View view = inflater.inflate(R.layout.fragment_how_to_play, container, false);

        // Interaction logic:
        // Dismiss only this fragment instance and reveal the underlying settings screen.
        Button btnClose = view.findViewById(R.id.btnClose);
        btnClose.setOnClickListener(v -> dismiss());

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            // Size logic:
            // Use full available width so the dim overlay feels native,
            // while keeping content height wrap-content to avoid oversized blank space.
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
}