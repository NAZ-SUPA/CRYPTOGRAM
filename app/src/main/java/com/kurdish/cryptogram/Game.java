/**
 * Package declaration for the Kurdish Cryptogram application.
 */
package com.kurdish.cryptogram;

/**
 * Android system and UI framework imports.
 */
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * AndroidX compatibility and Material UI imports.
 */
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * FlexboxLayout import for dynamic and wrapping UI layout.
 */
import com.google.android.flexbox.FlexboxLayout;

/**
 * Java utility imports for data structures and randomization.
 */
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Game activity handles the core gameplay logic, including puzzle generation,
 * user input validation, mistake tracking, and state persistence.
 */
public class Game extends AppCompatActivity {

    // List to store the actual characters that the user needs to guess.
    private List<Character> hiddenLetters = new ArrayList<>();
    // List to store the original indices of letters in the sentence that are hidden.
    private List<Integer> hiddenIndices = new ArrayList<>();
    // Map to link each character to its unique numerical cipher value.
    private java.util.Map<Character, Integer> cipherMap = new java.util.HashMap<>();
    // Random number generator for cipher generation and hint selection.
    private java.util.Random random = new java.util.Random();

    // Counter for incorrect guesses made in the current level.
    private int mistakeCount = 0;
    // Counter for correctly guessed characters.
    private int correctGuesses = 0;
    // Index of the current level being played.
    private int currentLevelIndex = 0;
    // Flag to track if the hint button has been used for the current level.
    private boolean isHintUsed = false;

    // NOTE ABOUT INDEXES:
    // - selected_level intent extra is one-based (level 1, level 2, ...).
    // - currentLevelIndex and hiddenIndices are zero-based because they map to String positions.
    // This distinction is important when moving between UI labels and internal list access.

    /**
     * Initializes the activity, sets up the UI components, and loads the level data.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Enable edge-to-edge system UI layout.
        EdgeToEdge.enable(this);
        // Set the content view to the game layout.
        setContentView(R.layout.activity_game);

        // Apply window insets to handle padding for status and navigation bars.
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize and set click listener for the Home button.
        ImageButton btnHome = findViewById(R.id.btn_home);
        btnHome.setOnClickListener(v -> {
            // Navigate back to the Main Menu.
            startActivity(new Intent(Game.this, MainMenu.class));
            // Close the current game activity.
            finish();
        });

        // Initialize and set click listener for the Settings button.
        ImageButton btnSetting = findViewById(R.id.btn_settings_main);
        btnSetting.setOnClickListener(v -> {
            // Open the Settings activity.
            startActivity(new Intent(Game.this, Settings.class));
        });

        // Initialize and set click listener for the Hint button.
        ImageButton btnHint = findViewById(R.id.btn_hint);
        btnHint.setOnClickListener(v -> useHint());

        // Retrieve the selected level index from the intent. Defaults to level 1.
        int selectedLevel = getIntent().getIntExtra("selected_level", 1);
        currentLevelIndex = selectedLevel - 1;

        // Load the array of sentences/words used for the game levels.
        String[] levels = getResources().getStringArray(R.array.game_words);

        // Safety check to ensure the level index is within bounds.
        if (currentLevelIndex >= levels.length) {
            currentLevelIndex = levels.length - 1;
        }

        // Programmatically create the game UI based on the selected level's sentence.
        CreateUI(levels[currentLevelIndex]);
        // Initialize the custom on-screen keyboard.
        setupKeyboard();
    }

    /**
     * Provides a hint to the player by revealing one random hidden letter.
     */
    private void useHint() {
        // Prevent multiple hint usages if the flag is already set.
        if (isHintUsed) {
            Toast.makeText(this, "Hint already used for this level!", Toast.LENGTH_SHORT).show();
            return;
        }

        List<Integer> availableHints = new ArrayList<>();
        FlexboxLayout container = findViewById(R.id.area);

        // Scan all hidden input boxes to find those that are still enabled (not yet correctly guessed).
        for (int i = 0; i < hiddenIndices.size(); i++) {
            int index = hiddenIndices.get(i);
            View view = container.findViewWithTag(index);

            if (view instanceof EditText) {
                EditText et = (EditText) view;
                if (et.isEnabled()) {
                    availableHints.add(i);
                }
            }
        }

        // If no boxes are available to reveal, notify the user.
        if (availableHints.isEmpty()) {
            Toast.makeText(this, "No empty boxes left!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Mark hint as used.
        isHintUsed = true;

        // Visual feedback: Disable and dim the hint button.
        ImageButton btnHint = findViewById(R.id.btn_hint);
        btnHint.setAlpha(0.4f);
        btnHint.setEnabled(false);

        // Select a random index from the list of available hints.
        int randomListIndex = availableHints.get(random.nextInt(availableHints.size()));
        int globalIndexToReveal = hiddenIndices.get(randomListIndex);
        char correctChar = hiddenLetters.get(randomListIndex);

        // Find the specific EditText and reveal the correct character.
        View viewToReveal = container.findViewWithTag(globalIndexToReveal);
        if (viewToReveal instanceof EditText) {
            EditText et = (EditText) viewToReveal;

            et.setText(String.valueOf(correctChar));
            // Style hint text differently (Purple).
            et.setTextColor(android.graphics.Color.parseColor("#9C27B0"));
            et.setBackgroundColor(android.graphics.Color.TRANSPARENT);

            // Disable the input box since it's now solved.
            et.setEnabled(false);
            et.setFocusable(false);
            et.setFocusableInTouchMode(false);

            // Increment correct guesses and check if the player won.
            correctGuesses++;
            checkWinCondition();
        }
    }

    /**
     * Called when the activity is going into the background.
     * Saves the current game state to prevent progress loss.
     */
    @Override
    protected void onPause() {
        super.onPause();
        saveGameState();
    }

    /**
     * Persists the current level progress (mistakes, guesses, inputs) into SharedPreferences.
     */
    private void saveGameState() {
        // Don't save if the level is already finished (won or lost).
        if (mistakeCount >= 3 || correctGuesses == hiddenLetters.size()) {
            return;
        }

        SharedPreferences prefs = getSharedPreferences("GameState", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        // Prefix scopes all saved values to the currently open level.
        // Example key: level_2_mistakes, level_2_inputs, etc.
        String prefix = "level_" + currentLevelIndex + "_";

        // Save numeric stats.
        editor.putInt(prefix + "mistakes", mistakeCount);
        editor.putInt(prefix + "correct", correctGuesses);
        editor.putBoolean(prefix + "hintUsed", isHintUsed);

        // Serialize the cipher map into a string.
        StringBuilder mapBuilder = new StringBuilder();
        for (Map.Entry<Character, Integer> entry : cipherMap.entrySet()) {
            mapBuilder.append(entry.getKey()).append(":").append(entry.getValue()).append(";");
        }
        editor.putString(prefix + "cipherMap", mapBuilder.toString());

        // Serialize hidden indices.
        StringBuilder indicesBuilder = new StringBuilder();
        for (Integer index : hiddenIndices) {
            indicesBuilder.append(index).append(";");
        }
        editor.putString(prefix + "hiddenIndices", indicesBuilder.toString());

        // Serialize user inputs and their current text colors.
        // Color persistence allows UI restore to preserve validation state:
        // - white = pending, red = incorrect, green = correct, purple = hint-revealed.
        StringBuilder inputsBuilder = new StringBuilder();
        FlexboxLayout container = findViewById(R.id.area);
        for (Integer index : hiddenIndices) {
            View view = container.findViewWithTag(index);
            if (view instanceof EditText) {
                EditText et = (EditText) view;
                String typedLetter = et.getText().toString();
                if (!typedLetter.isEmpty()) {
                    int textColor = et.getCurrentTextColor();
                    inputsBuilder.append(index).append(":").append(typedLetter).append(":").append(textColor).append(";");
                }
            }
        }
        editor.putString(prefix + "inputs", inputsBuilder.toString());
        editor.apply();
    }

    /**
     * Removes saved state for the current level from SharedPreferences.
     */
    private void clearGameState() {
        SharedPreferences prefs = getSharedPreferences("GameState", MODE_PRIVATE);
        String prefix = "level_" + currentLevelIndex + "_";
        prefs.edit()
                .remove(prefix + "mistakes")
                .remove(prefix + "correct")
                .remove(prefix + "hintUsed")
                .remove(prefix + "cipherMap")
                .remove(prefix + "hiddenIndices")
                .remove(prefix + "inputs")
                .apply();
    }

    /**
     * Updates the UI to show the current number of mistakes using cross icons.
     */
    private void restoreMistakeUI() {
        ImageView iv1 = findViewById(R.id.iv_mistake_1);
        ImageView iv2 = findViewById(R.id.iv_mistake_2);
        ImageView iv3 = findViewById(R.id.iv_mistake_3);

        // Reset all icons to default placeholders.
        iv1.setImageResource(R.drawable.circle_placeholder);
        iv2.setImageResource(R.drawable.circle_placeholder);
        iv3.setImageResource(R.drawable.circle_placeholder);

        // Show crosses based on mistake count.
        if (mistakeCount >= 1) iv1.setImageResource(R.drawable.cross);
        if (mistakeCount >= 2) iv2.setImageResource(R.drawable.cross);
        if (mistakeCount >= 3) iv3.setImageResource(R.drawable.cross);
    }

    /**
     * Deducts a life (heart) from the player's total and manages the renewal timer.
     */
    private void deductHeart() {
        android.content.SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        int currentHearts = prefs.getInt("heart_count", 5);

        if (currentHearts > 0) {
            currentHearts--;
            android.content.SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("heart_count", currentHearts);

            // If this was the first heart lost from a full set, start the renewal timer.
            if (currentHearts == 4) {
                long startTime = System.currentTimeMillis();
                editor.putLong("Heart_Renew_Start_Time", startTime);

                // Log the scheduled time for debugging.
                android.util.Log.d("HeartTest", "Alarm scheduled for: " + (startTime + (30 * 60 * 1000L)));

                // Schedule a notification to remind the user when a heart is restored.
                NotificationHelper.scheduleNextHeartNotification(this, startTime + (30 * 60 * 1000L));
            }
            editor.apply();
        }
    }

    /**
     * Configures the click listeners for the custom on-screen keyboard buttons.
     */
    private void setupKeyboard() {
        LinearLayout keyboard = findViewById(R.id.custom_keyboard);

        // Recursively assign listeners to all buttons in the keyboard layout.
        assignListenerToButtons(keyboard, v -> {
            String tag = (String) v.getTag();
            if ("DEL".equals(tag)) {
                handleDelete();
            } else if ("ENT".equals(tag)) {
                handleEnter();
            } else if (tag != null) {
                handleKeyPress(tag);
            }
        });

        // Initialize state of the Enter button.
        updateEnterButtonState();
    }

    /**
     * Enables or disables the Enter key based on whether the current input box has a character.
     */
    private void updateEnterButtonState() {
        Button btnEnter = findViewById(R.id.btn_key_ent);
        if (btnEnter == null) return;

        View focusedView = getCurrentFocus();

        // Check if an enabled EditText is currently focused and has exactly one character.
        if (focusedView instanceof EditText && focusedView.isEnabled()) {
            EditText activeBox = (EditText) focusedView;
            if (activeBox.getText().toString().trim().length() == 1) {
                btnEnter.setEnabled(true);
                btnEnter.setAlpha(1.0f);
                return;
            }
        }

        // Disable and dim the Enter button if conditions are not met.
        btnEnter.setEnabled(false);
        btnEnter.setAlpha(0.4f);
    }

    /**
     * Processes the submission of a guessed letter.
     */
    private void handleEnter() {
        View focusedView = getCurrentFocus();

        if (focusedView instanceof EditText && focusedView.isEnabled()) {
            EditText activeBox = (EditText) focusedView;
            String userGuess = activeBox.getText().toString().trim();

            if (userGuess.length() == 1) {
                int boxIndex = (int) activeBox.getTag();
                int listIndex = hiddenIndices.indexOf(boxIndex);

                if (listIndex != -1) {
                    char correctChar = hiddenLetters.get(listIndex);

                    // Check if the user's guess matches the target letter.
                    if (userGuess.equalsIgnoreCase(String.valueOf(correctChar))) {
                        // Mark as correct: disable input and change color to Green.
                        activeBox.setEnabled(false);
                        activeBox.setFocusable(false);
                        activeBox.setFocusableInTouchMode(false);

                        activeBox.setTextColor(android.graphics.Color.GREEN);
                        activeBox.setBackgroundColor(android.graphics.Color.TRANSPARENT);

                        correctGuesses++;
                        checkWinCondition();

                    } else {
                        // Mark as incorrect: change color to Red and increment mistake count.
                        activeBox.setTextColor(android.graphics.Color.RED);
                        handleMistake();
                    }
                }
            }
        }
        updateEnterButtonState();
    }

    /**
     * Checks if all hidden letters have been correctly identified.
     */
    private void checkWinCondition() {
        if (correctGuesses == hiddenLetters.size()) {
            // Clean up saved state for this level.
            clearGameState();

            // Progress tracking: unlock the next level in SharedPreferences.
            android.content.SharedPreferences prefs = getSharedPreferences("game_progress", MODE_PRIVATE);
            int unlockedLevel = prefs.getInt("unlocked_level", 1);
            int playedLevel = currentLevelIndex + 1;

            // Unlock the next level only if user just reached their frontier.
            // This avoids lowering progress when replaying older levels.
            if (unlockedLevel <= playedLevel) {
                prefs.edit().putInt("unlocked_level", playedLevel + 1).apply();
            }

            // Retrieve the full sentence and pass it to the Win overlay.
            String[] levels = getResources().getStringArray(R.array.game_words);
            showGameWonFragment(levels[currentLevelIndex]);
        }
    }

    /**
     * Displays the Game Won fragment overlay, passing the full sentence, and disables input.
     */
    private void showGameWonFragment(String fullSentence) {
        LinearLayout keyboard = findViewById(R.id.custom_keyboard);
        for (int i = 0; i < keyboard.getChildCount(); i++) {
            // Freeze keyboard to prevent edits behind the overlay fragment.
            keyboard.getChildAt(i).setEnabled(false);
        }

        GameWon gameWonFragment = new GameWon();
        Bundle args = new Bundle();
        args.putInt("current_level_index", currentLevelIndex);
        args.putString("full_sentence", fullSentence); // Passes the sentence to the fragment
        gameWonFragment.setArguments(args);

        getSupportFragmentManager().beginTransaction()
                .add(R.id.main, gameWonFragment)
                .addToBackStack(null)
                .commit();
    }

    /**
     * Handles logic for incorrect guesses.
     */
    private void handleMistake() {
        mistakeCount++;
        restoreMistakeUI();

        // Check for Game Over condition (3 mistakes).
        if (mistakeCount >= 3) {
            clearGameState();
            deductHeart();
            showGameOverFragment();
        }
    }

    /**
     * Displays the Game Over fragment overlay and disables input.
     */
    private void showGameOverFragment() {
        LinearLayout keyboard = findViewById(R.id.custom_keyboard);
        for (int i = 0; i < keyboard.getChildCount(); i++) {
            keyboard.getChildAt(i).setEnabled(false);
        }

        GameOver gameOverFragment = new GameOver();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.main, gameOverFragment)
                .addToBackStack(null)
                .commit();
    }

    /**
     * Utility method to recursively assign a click listener to all buttons within a view hierarchy.
     */
    private void assignListenerToButtons(View view, View.OnClickListener listener) {
        if (view instanceof android.view.ViewGroup) {
            android.view.ViewGroup group = (android.view.ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                assignListenerToButtons(group.getChildAt(i), listener);
            }
        } else if (view instanceof android.widget.Button) {
            view.setOnClickListener(listener);
        }
    }

    /**
     * Inserts a character into the currently focused EditText.
     */
    private void handleKeyPress(String letter) {
        View focusedView = getCurrentFocus();
        if (focusedView instanceof EditText && focusedView.isEnabled()) {
            EditText activeBox = (EditText) focusedView;
            activeBox.setText(letter);
            activeBox.setSelection(1);
            activeBox.setTextColor(getResources().getColor(R.color.white, null));
            updateEnterButtonState();
        }
    }

    /**
     * Clears the character from the currently focused EditText.
     */
    private void handleDelete() {
        View focusedView = getCurrentFocus();
        if (focusedView instanceof EditText && focusedView.isEnabled()) {
            EditText activeBox = (EditText) focusedView;
            activeBox.setText("");
            activeBox.setTextColor(getResources().getColor(R.color.white, null));
            updateEnterButtonState();
        }
    }

    /**
     * Dynamically constructs the game puzzle UI based on the given sentence.
     * Handles layout generation, cipher mapping, and state restoration.
     */
    public void CreateUI(String sentence) {
        FlexboxLayout container = findViewById(R.id.area);
        container.removeAllViews();
        ImageButton btnHint = findViewById(R.id.btn_hint);

        // Reset data structures for the new level.
        cipherMap.clear();
        hiddenLetters.clear();
        hiddenIndices.clear();

        // --- NEW LOGIC: Pool of unique cipher numbers ---
        // Create a list of available numbers (1-26) to act like a shuffled deck of cards.
        List<Integer> availableCipherNumbers = new ArrayList<>();
        for (int i = 1; i <= 26; i++) {
            availableCipherNumbers.add(i);
        }

        // Load saved state if it exists.
        SharedPreferences prefs = getSharedPreferences("GameState", MODE_PRIVATE);
        String prefix = "level_" + currentLevelIndex + "_";
        // "cipherMap" presence is used as restore marker for this level state snapshot.
        boolean hasSavedGame = prefs.contains(prefix + "cipherMap");

        if (hasSavedGame) {
            // Restore progress from SharedPreferences.
            mistakeCount = prefs.getInt(prefix + "mistakes", 0);
            correctGuesses = prefs.getInt(prefix + "correct", 0);

            isHintUsed = prefs.getBoolean(prefix + "hintUsed", false);
            if (isHintUsed) {
                btnHint.setAlpha(0.4f);
                btnHint.setEnabled(false);
            } else {
                btnHint.setAlpha(1.0f);
                btnHint.setEnabled(true);
            }

            restoreMistakeUI();

            // Deserialize the cipher map string.
            String mapStr = prefs.getString(prefix + "cipherMap", "");
            for (String pair : mapStr.split(";")) {
                if (pair.contains(":")) {
                    String[] kv = pair.split(":");
                    int number = Integer.parseInt(kv[1]);
                    cipherMap.put(kv[0].charAt(0), number);

                    // Remove already used numbers from our pool so we do not duplicate ciphers.
                    availableCipherNumbers.remove(Integer.valueOf(number));
                }
            }

            // Deserialize hidden indices and identify target letters.
            String indicesStr = prefs.getString(prefix + "hiddenIndices", "");
            for (String idx : indicesStr.split(";")) {
                if (!idx.trim().isEmpty()) {
                    int index = Integer.parseInt(idx);
                    hiddenIndices.add(index);
                    hiddenLetters.add(Character.toLowerCase(sentence.charAt(index)));
                }
            }
        } else {
            // Initialize fresh level state.
            mistakeCount = 0;
            correctGuesses = 0;
            isHintUsed = false;
            btnHint.setAlpha(1.0f);
            btnHint.setEnabled(true);
            restoreMistakeUI();

            // Determine which letters to hide (approx. 40% of total letters).
            int totalLetters = sentence.replace(" ", "").length();
            int lettersToHide = (int) (totalLetters * 0.4);

            List<Integer> allLetterIndices = new ArrayList<>();
            for(int i = 0; i < sentence.length(); i++) {
                if(sentence.charAt(i) != ' ') allLetterIndices.add(i);
            }
            java.util.Collections.shuffle(allLetterIndices);
            // Pick a randomized subset of positions to hide; the rest stay visible as anchors.
            List<Integer> indicesToHide = allLetterIndices.subList(0, Math.min(lettersToHide, allLetterIndices.size()));

            for (Integer idx : indicesToHide) {
                hiddenIndices.add(idx);
                hiddenLetters.add(Character.toLowerCase(sentence.charAt(idx)));
            }
        }

        // Shuffle the remaining available numbers in our "deck"
        java.util.Collections.shuffle(availableCipherNumbers);

        // Split sentence into words and create UI groups.
        String[] words = sentence.split(" ");
        int charPtr = 0;

        for (String word : words) {
            LinearLayout wordGroup = new LinearLayout(this);
            wordGroup.setOrientation(LinearLayout.HORIZONTAL);

            for (int i = 0; i < word.length(); i++) {
                char c = Character.toLowerCase(sentence.charAt(charPtr));

                // Assign a UNIQUE random cipher number if the character doesn't have one yet.
                if (!cipherMap.containsKey(c)) {
                    if (!availableCipherNumbers.isEmpty()) {
                        // Draw the next available unique number from the top of the shuffled deck
                        cipherMap.put(c, availableCipherNumbers.remove(0));
                    } else {
                        // Fallback in case the sentence has more than 26 unique characters (e.g. punctuation)
                        cipherMap.put(c, random.nextInt(100) + 27);
                    }
                }
                int cipherNumber = cipherMap.get(c);

                // Create a vertical container for the letter input and its cipher number label.
                LinearLayout boxContainer = new LinearLayout(this);
                boxContainer.setOrientation(LinearLayout.VERTICAL);
                boxContainer.setGravity(android.view.Gravity.CENTER);

                EditText letterInput = new EditText(this);
                letterInput.setId(View.generateViewId());
                // Tag stores absolute character index in the sentence so keyboard handlers
                // can map this box back to hiddenIndices/hiddenLetters quickly.
                letterInput.setTag(charPtr);
                letterInput.setGravity(android.view.Gravity.CENTER);
                letterInput.setTextColor(getResources().getColor(R.color.white, null));

                // Apply dynamic font size based on user settings.
                SharedPreferences fontPrefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                int fontSetting = fontPrefs.getInt("font_size", 0);
                float dynamicTextSize = 20f;

                if (fontSetting == 1) {
                    dynamicTextSize = 24f;
                } else if (fontSetting == 2) {
                    dynamicTextSize = 28f;
                }
                letterInput.setTextSize(dynamicTextSize);

                // Disable default soft keyboard as we use a custom one.
                letterInput.setShowSoftInputOnFocus(false);
                // Limit input to a single character.
                letterInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(1)});

                // Listener to update Enter button state when focus changes.
                letterInput.setOnFocusChangeListener((v, hasFocus) -> updateEnterButtonState());

                // Create and style the cipher number label.
                TextView numberLabel = new TextView(this);
                numberLabel.setGravity(android.view.Gravity.CENTER);
                numberLabel.setTextColor(getResources().getColor(R.color.white, null));
                numberLabel.setTextSize(14);
                numberLabel.setText(String.valueOf(cipherNumber));

                // Decide if this box should be an input or a pre-filled hint letter.
                if (hiddenIndices.contains(charPtr)) {
                    letterInput.setText("");
                } else {
                    letterInput.setText(String.valueOf(c));
                    letterInput.setEnabled(false);
                    letterInput.setBackgroundColor(android.graphics.Color.TRANSPARENT);
                }

                boxContainer.addView(letterInput);
                boxContainer.addView(numberLabel);

                // Layout parameters for the individual box.
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(120, FlexboxLayout.LayoutParams.WRAP_CONTENT);
                lp.setMargins(5, 5, 5, 5);
                boxContainer.setLayoutParams(lp);

                wordGroup.addView(boxContainer);
                charPtr++;
            }

            charPtr++; // Account for space between words.

            // Layout parameters for the word group within the Flexbox container.
            FlexboxLayout.LayoutParams wordParams = new FlexboxLayout.LayoutParams(
                    FlexboxLayout.LayoutParams.WRAP_CONTENT, FlexboxLayout.LayoutParams.WRAP_CONTENT);
            wordParams.setMargins(0, 0, 25, 20);
            wordGroup.setLayoutParams(wordParams);

            container.addView(wordGroup);
        }

        // Restore user's previous inputs and their validation status.
        if (hasSavedGame) {
            String inputsStr = prefs.getString(prefix + "inputs", "");
            for (String pair : inputsStr.split(";")) {
                if (pair.contains(":")) {
                    String[] kv = pair.split(":");
                    int idx = Integer.parseInt(kv[0]);
                    String letter = kv[1];

                    int color = getResources().getColor(R.color.white, null);
                    if (kv.length >= 3) {
                        color = Integer.parseInt(kv[2]);
                    }

                    View view = container.findViewWithTag(idx);
                    if (view instanceof EditText) {
                        EditText et = (EditText) view;
                        et.setText(letter);
                        et.setTextColor(color);

                        // If the letter was already correctly guessed or revealed by hint, lock it.
                        if (color == android.graphics.Color.GREEN || color == android.graphics.Color.parseColor("#9C27B0")) {
                            et.setEnabled(false);
                            et.setFocusable(false);
                            et.setFocusableInTouchMode(false);
                            et.setBackgroundColor(android.graphics.Color.TRANSPARENT);
                        }
                    }
                }
            }
        }
    }
}