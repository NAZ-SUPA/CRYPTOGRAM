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

        // --- DATABASE LOGIC INITIALIZATION ---
        int selectedLevel = getIntent().getIntExtra("selected_level", 1);
        DatabaseHelper dbHelper = new DatabaseHelper(this);

        int totalDbLevels = dbHelper.getTotalLevelsCount();
        if (selectedLevel > totalDbLevels) {
            selectedLevel = totalDbLevels; // Cap at max available
        }
        currentLevelIndex = selectedLevel - 1;

        // Fetch sentence from DB (Single language)
        String levelSentence = dbHelper.getSentenceForLevel(selectedLevel);
        if (levelSentence == null || levelSentence.isEmpty()) levelSentence = "ERROR DB";

        CreateUI(levelSentence);
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
                if (et.isEnabled()) availableHints.add(i);
            }
        }

        // If no boxes are available to reveal, notify the user.
        if (availableHints.isEmpty()) {
            Toast.makeText(this, "No empty boxes left!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Mark hint as used.
        isHintUsed = true;
        findViewById(R.id.btn_hint).setAlpha(0.4f);
        findViewById(R.id.btn_hint).setEnabled(false);

        // Select a random index from the list of available hints.
        int randomListIndex = availableHints.get(random.nextInt(availableHints.size()));
        int globalIndexToReveal = hiddenIndices.get(randomListIndex);
        char correctChar = hiddenLetters.get(randomListIndex);

        // Find the specific EditText and reveal the correct character in UPPERCASE.
        View viewToReveal = container.findViewWithTag(globalIndexToReveal);
        if (viewToReveal instanceof EditText) {
            EditText et = (EditText) viewToReveal;
            // Set text as uppercase
            et.setText(String.valueOf(Character.toUpperCase(correctChar)));
            et.setTextColor(android.graphics.Color.parseColor("#9C27B0"));
            et.setBackgroundColor(android.graphics.Color.TRANSPARENT);
            et.setEnabled(false);
            et.setFocusable(false);
            et.setFocusableInTouchMode(false);

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
        if (mistakeCount >= 3 || correctGuesses == hiddenLetters.size()) return;

        SharedPreferences prefs = getSharedPreferences("GameState", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        // Reverted to single-language prefix
        String prefix = "level_" + currentLevelIndex + "_";

        editor.putInt(prefix + "mistakes", mistakeCount);
        editor.putInt(prefix + "correct", correctGuesses);
        editor.putBoolean(prefix + "hintUsed", isHintUsed);

        StringBuilder mapBuilder = new StringBuilder();
        for (Map.Entry<Character, Integer> entry : cipherMap.entrySet()) {
            mapBuilder.append(entry.getKey()).append(":").append(entry.getValue()).append(";");
        }
        editor.putString(prefix + "cipherMap", mapBuilder.toString());

        StringBuilder indicesBuilder = new StringBuilder();
        for (Integer index : hiddenIndices) {
            indicesBuilder.append(index).append(";");
        }
        editor.putString(prefix + "hiddenIndices", indicesBuilder.toString());

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

        iv1.setImageResource(R.drawable.circle_placeholder);
        iv2.setImageResource(R.drawable.circle_placeholder);
        iv3.setImageResource(R.drawable.circle_placeholder);

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

            if (currentHearts == 4) {
                long startTime = System.currentTimeMillis();
                editor.putLong("Heart_Renew_Start_Time", startTime);
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
        assignListenerToButtons(keyboard, v -> {
            String tag = (String) v.getTag();
            if ("DEL".equals(tag)) handleDelete();
            else if ("ENT".equals(tag)) handleEnter();
            else if (tag != null) handleKeyPress(tag);
        });
        updateEnterButtonState();
    }

    /**
     * Enables or disables the Enter key based on whether the current input box has a character.
     */
    private void updateEnterButtonState() {
        Button btnEnter = findViewById(R.id.btn_key_ent);
        if (btnEnter == null) return;

        View focusedView = getCurrentFocus();
        if (focusedView instanceof EditText && focusedView.isEnabled()) {
            EditText activeBox = (EditText) focusedView;
            if (activeBox.getText().toString().trim().length() == 1) {
                btnEnter.setEnabled(true);
                btnEnter.setAlpha(1.0f);
                return;
            }
        }
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

                    if (userGuess.equalsIgnoreCase(String.valueOf(correctChar))) {
                        activeBox.setEnabled(false);
                        activeBox.setFocusable(false);
                        activeBox.setFocusableInTouchMode(false);
                        activeBox.setTextColor(android.graphics.Color.GREEN);
                        activeBox.setBackgroundColor(android.graphics.Color.TRANSPARENT);

                        correctGuesses++;
                        checkWinCondition();

                    } else {
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
            clearGameState();

            SharedPreferences prefs = getSharedPreferences("game_progress", MODE_PRIVATE);
            int unlockedLevel = prefs.getInt("unlocked_level", 1);
            int playedLevel = currentLevelIndex + 1;

            if (unlockedLevel <= playedLevel) {
                prefs.edit().putInt("unlocked_level", playedLevel + 1).apply();
            }

            // Retrieve DB Sentence
            DatabaseHelper dbHelper = new DatabaseHelper(this);
            String dbSentence = dbHelper.getSentenceForLevel(currentLevelIndex + 1);
            showGameWonFragment(dbSentence);
        }
    }

    /**
     * Displays the Game Won fragment overlay, passing the full sentence, and disables input.
     */
    private void showGameWonFragment(String fullSentence) {
        LinearLayout keyboard = findViewById(R.id.custom_keyboard);
        for (int i = 0; i < keyboard.getChildCount(); i++) {
            keyboard.getChildAt(i).setEnabled(false);
        }

        GameWon gameWonFragment = new GameWon();
        Bundle args = new Bundle();
        args.putInt("current_level_index", currentLevelIndex);
        args.putString("full_sentence", fullSentence);
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
            // Ensure any character inputted by the keyboard is uppercase
            activeBox.setText(letter.toUpperCase());
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

        cipherMap.clear();
        hiddenLetters.clear();
        hiddenIndices.clear();

        List<Integer> availableCipherNumbers = new ArrayList<>();
        for (int i = 1; i <= 26; i++) {
            availableCipherNumbers.add(i);
        }

        SharedPreferences prefs = getSharedPreferences("GameState", MODE_PRIVATE);
        String prefix = "level_" + currentLevelIndex + "_";

        boolean hasSavedGame = prefs.contains(prefix + "cipherMap");

        if (hasSavedGame) {
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

            String mapStr = prefs.getString(prefix + "cipherMap", "");
            for (String pair : mapStr.split(";")) {
                if (pair.contains(":")) {
                    String[] kv = pair.split(":");
                    int number = Integer.parseInt(kv[1]);
                    // Maintain map consistency using uppercase keys
                    cipherMap.put(Character.toUpperCase(kv[0].charAt(0)), number);
                    availableCipherNumbers.remove(Integer.valueOf(number));
                }
            }

            String indicesStr = prefs.getString(prefix + "hiddenIndices", "");
            for (String idx : indicesStr.split(";")) {
                if (!idx.trim().isEmpty()) {
                    int index = Integer.parseInt(idx);
                    hiddenIndices.add(index);
                    // Add to hidden letters as uppercase for evaluation consistency
                    hiddenLetters.add(Character.toUpperCase(sentence.charAt(index)));
                }
            }
        } else {
            mistakeCount = 0;
            correctGuesses = 0;
            isHintUsed = false;
            btnHint.setAlpha(1.0f);
            btnHint.setEnabled(true);
            restoreMistakeUI();

            int totalLetters = sentence.replace(" ", "").length();
            int lettersToHide = (int) (totalLetters * 0.4);

            List<Integer> allLetterIndices = new ArrayList<>();
            for(int i = 0; i < sentence.length(); i++) {
                if(sentence.charAt(i) != ' ') allLetterIndices.add(i);
            }
            java.util.Collections.shuffle(allLetterIndices);
            List<Integer> indicesToHide = allLetterIndices.subList(0, Math.min(lettersToHide, allLetterIndices.size()));

            for (Integer idx : indicesToHide) {
                hiddenIndices.add(idx);
                // Ensure internal representation handles uppercase
                hiddenLetters.add(Character.toUpperCase(sentence.charAt(idx)));
            }
        }

        java.util.Collections.shuffle(availableCipherNumbers);

        String[] words = sentence.split(" ");
        int charPtr = 0;

        for (String word : words) {
            LinearLayout wordGroup = new LinearLayout(this);
            wordGroup.setOrientation(LinearLayout.HORIZONTAL);

            for (int i = 0; i < word.length(); i++) {
                // Read from sentence and force uppercase
                char c = Character.toUpperCase(sentence.charAt(charPtr));

                if (!cipherMap.containsKey(c)) {
                    if (!availableCipherNumbers.isEmpty()) {
                        cipherMap.put(c, availableCipherNumbers.remove(0));
                    } else {
                        cipherMap.put(c, random.nextInt(100) + 27);
                    }
                }
                int cipherNumber = cipherMap.get(c);

                LinearLayout boxContainer = new LinearLayout(this);
                boxContainer.setOrientation(LinearLayout.VERTICAL);
                boxContainer.setGravity(android.view.Gravity.CENTER);

                EditText letterInput = new EditText(this);
                letterInput.setId(View.generateViewId());
                letterInput.setTag(charPtr);
                letterInput.setGravity(android.view.Gravity.CENTER);
                letterInput.setTextColor(getResources().getColor(R.color.white, null));

                SharedPreferences fontPrefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                int fontSetting = fontPrefs.getInt("font_size", 0);
                float dynamicTextSize = 20f;
                if (fontSetting == 1) dynamicTextSize = 24f;
                else if (fontSetting == 2) dynamicTextSize = 28f;

                letterInput.setTextSize(dynamicTextSize);
                letterInput.setShowSoftInputOnFocus(false);
                // Enforce uppercase input formatting on the EditText itself
                letterInput.setFilters(new InputFilter[]{
                        new InputFilter.LengthFilter(1),
                        new InputFilter.AllCaps()
                });
                letterInput.setOnFocusChangeListener((v, hasFocus) -> updateEnterButtonState());

                TextView numberLabel = new TextView(this);
                numberLabel.setGravity(android.view.Gravity.CENTER);
                numberLabel.setTextColor(getResources().getColor(R.color.white, null));
                numberLabel.setTextSize(14);
                numberLabel.setText(String.valueOf(cipherNumber));

                if (hiddenIndices.contains(charPtr)) {
                    letterInput.setText("");
                } else {
                    // Populate revealed hints as uppercase
                    letterInput.setText(String.valueOf(c));
                    letterInput.setEnabled(false);
                    letterInput.setBackgroundColor(android.graphics.Color.TRANSPARENT);
                }

                boxContainer.addView(letterInput);
                boxContainer.addView(numberLabel);

                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(120, FlexboxLayout.LayoutParams.WRAP_CONTENT);
                lp.setMargins(5, 5, 5, 5);
                boxContainer.setLayoutParams(lp);

                wordGroup.addView(boxContainer);
                charPtr++;
            }

            charPtr++;

            FlexboxLayout.LayoutParams wordParams = new FlexboxLayout.LayoutParams(
                    FlexboxLayout.LayoutParams.WRAP_CONTENT, FlexboxLayout.LayoutParams.WRAP_CONTENT);
            wordParams.setMargins(0, 0, 25, 20);
            wordGroup.setLayoutParams(wordParams);

            container.addView(wordGroup);
        }

        if (hasSavedGame) {
            String inputsStr = prefs.getString(prefix + "inputs", "");
            for (String pair : inputsStr.split(";")) {
                if (pair.contains(":")) {
                    String[] kv = pair.split(":");
                    int idx = Integer.parseInt(kv[0]);
                    String letter = kv[1];

                    int color = getResources().getColor(R.color.white, null);
                    if (kv.length >= 3) color = Integer.parseInt(kv[2]);

                    View view = container.findViewWithTag(idx);
                    if (view instanceof EditText) {
                        EditText et = (EditText) view;
                        // Restore inputs as uppercase
                        et.setText(letter.toUpperCase());
                        et.setTextColor(color);

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