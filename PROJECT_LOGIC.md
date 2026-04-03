# Cryptogram Project Logic

This document explains how the app is structured and how each major file participates in the runtime flow.

## 1) App Flow (High Level)

1. `MainActivity` launches first as the app entry point.
2. After a short splash delay, it opens `MainMenu`.
3. `MainMenu` routes to:
   - `Game` (play screen)
   - `Levels` (level chooser)
   - `Settings` (app options)
4. `Levels` opens `Game` with `selected_level` extra.
5. `Settings` currently updates visual toggle state only (not persisted yet).

## 2) Java Activity Logic

- `app/src/main/java/com/kurdish/cryptogram/MainActivity.java`
  - Shows intro branding screen.
  - Colors specific letters and matching cipher numbers.
  - Uses `Handler` delay before navigating to `MainMenu`.

- `app/src/main/java/com/kurdish/cryptogram/MainMenu.java`
  - Main navigation hub.
  - Buttons launch `Game`, `Levels`, and `Settings`.

- `app/src/main/java/com/kurdish/cryptogram/Game.java`
  - Hosts play UI and keyboard layout.
  - Currently handles top navigation only (Home and Settings).

- `app/src/main/java/com/kurdish/cryptogram/Levels.java`
  - Binds click listeners to each card in `levels_grid`.
  - Passes level number to `Game` as `selected_level`.

- `app/src/main/java/com/kurdish/cryptogram/Settings.java`
  - Handles ON/OFF toggle visual state.
  - Handles font size preset visual state (`x1`, `x1.5`, `x2`).
  - `HOME` button closes activity with `finish()`.

## 3) Layout Logic

- `activity_main.xml`: intro UI (welcome text, cryptogram title, cipher line).
- `activity_main_menu.xml`: life chip, branding card, category chips, play/levels buttons.
- `activity_game.xml`: top controls, mistakes indicators, hint action, custom keyboard.
- `activity_levels.xml`: home/settings shortcuts and 3x3 level grid.
- `activity_settings.xml`: notification segmented toggle, help button, font-size card, home button.

## 4) Resource Logic

- `res/values/strings.xml`
  - Central text source for labels, keyboard keys, button captions, and accessibility strings.

- `res/values/colors.xml`
  - Shared color palette for branding, highlights, and base UI tones.

- `res/values/themes.xml` and `res/values-night/themes.xml`
  - App-wide Material theme and custom text styles for repeated UI patterns.

- `res/drawable/*.xml`
  - Reusable shape/vector assets for cards, chips, buttons, and keyboard backgrounds.

## 5) Manifest + App Configuration

- `app/src/main/AndroidManifest.xml`
  - Registers all activities and marks `MainActivity` as launcher.
  - Declares app icon, label, backup/data extraction config, and theme.

## 6) Current Functional Gaps (Expected)

- Settings choices are not persisted yet.
- Game screen does not yet apply `selected_level` to gameplay state.
- How-to-play button is currently a TODO hook.

