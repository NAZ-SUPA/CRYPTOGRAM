# Cryptogram Project Logic

This document provides a detailed comment-style explanation of the current project structure and the recent changes that were made across the app.

## 1) Runtime Flow (Detailed)

1. `MainActivity` launches first and renders the intro branding screen.
2. `MainActivity` highlights specific letters/numbers and waits ~2 seconds.
3. App navigates to `MainMenu`.
4. `MainMenu` acts as the navigation hub (`Game`, `Levels`, `Settings`) and refreshes heart/timer UI in `onResume()`.
5. `Levels` reads unlocked progress from shared preferences and blocks locked levels.
6. `Game` currently handles top-level navigation while layout/UI scaffolding is prepared for gameplay logic.
7. `Settings` applies local visual toggle state (notification and font-size groups); persistence is a planned next step.

## 2) Detailed Change Notes by Java File

### `app/src/main/java/com/kurdish/cryptogram/MainActivity.java`
- Added clearer intro/splash flow comments around startup behavior.
- Added/kept delay-based navigation to `MainMenu` using `Handler(Looper.getMainLooper()).postDelayed(...)`.
- Added comments for the cryptogram text styling logic in `colorLetters()`.
- Highlighted two `R` characters and corresponding cipher positions via `SpannableString` spans.
- Clarified edge-to-edge inset handling to prevent status/nav bar overlap.

### `app/src/main/java/com/kurdish/cryptogram/MainMenu.java`
- Main navigation paths are documented in detail (`Play`, `Levels`, `Settings`).
- Heart regeneration logic is fully documented:
  - reads `heart_count` + `Heart_Renew_Start_Time` from `SharedPreferences`
  - awards missed hearts while app was closed
  - caps hearts at 5
  - saves updated values and resets timer marker when full
- UI refresh flow is centralized in `refreshHeartUI()`.
- Countdown logic is documented in `startUiTimer(...)`, including safety refresh when timer reaches/passes zero.
- Timer cancellation in `onStop()` is documented to avoid background leaks/duplicate timers.

### `app/src/main/java/com/kurdish/cryptogram/Levels.java`
- Added comments for level-grid selection behavior.
- Added progress read from `SharedPreferences("game_progress")` (`unlocked_level`).
- Locked levels are visually dimmed and prevented from opening.
- Allowed levels navigate to `Game` and pass `selected_level` as intent extra.
- Home/settings button routes are documented.

### `app/src/main/java/com/kurdish/cryptogram/Game.java`
- Added comments for screen purpose and current scope.
- Added comments around edge-to-edge/system inset handling.
- Documented navigation actions for home and settings buttons.

### `app/src/main/java/com/kurdish/cryptogram/Settings.java`
- Added comments for segmented notification toggle behavior (single selected option).
- Added comments for font-size preset selection group (`x1`, `x1.5`, `x2`).
- Added comments for HOME return flow (`finish()`) and how-to-play placeholder hook.

## 3) Detailed Change Notes by Layout File

### `app/src/main/res/layout/activity_main.xml`
- Added structural comments for intro screen composition.
- Added yellow background block views behind the two highlighted `R` positions in the title.
- Documented relationship between static layout highlights and dynamic text coloring from `MainActivity`.

### `app/src/main/res/layout/activity_main_menu.xml`
- Added section comments for heart display, branding card, category chips, and action buttons.
- Documented top-right settings action and primary/secondary CTA flow.

### `app/src/main/res/layout/activity_levels.xml`
- Added comments for home/settings shortcuts and title.
- Documented how the 3x3 `GridLayout` is consumed by `Levels.java`.
- Clarified level-card purpose and interaction mapping.

### `app/src/main/res/layout/activity_game.xml`
- Added comments for top controls, mistakes indicators, hint action, and keyboard area.
- Documented the custom keyboard as a staged input UI scaffold for gameplay.

### `app/src/main/res/layout/activity_settings.xml`
- Added detailed comments for screen sections:
  - notification segmented toggle
  - how-to-play button
  - font-size card/presets
  - home button
- Documented layout tuning updates (font-size card width behavior and ON/OFF block horizontal position adjustments).

## 4) Detailed Change Notes by Resource File

### `app/src/main/res/values/strings.xml`
- Added grouping comments for intro labels, menu labels, game keys, accessibility text, and settings labels.
- Clarified usage intent for important strings used by multiple screens.

### `app/src/main/res/values/colors.xml`
- Added palette comments to explain role by group (base neutrals, accent colors, brand purples, supporting greens).

### `app/src/main/res/values/themes.xml`
- Added style-level comments describing where each text/button style is used.
- Clarified base theme aliasing and keyboard key style intent.

### `app/src/main/res/layout/fragment_how_to_play.xml`
- Added structure comments for overlay/card layering and close-action intent.
- Clarified that instructions are sourced from `strings.xml` for maintainability.

## 5) Manifest and Navigation Safety

### `app/src/main/AndroidManifest.xml`
- Added comments explaining app-level config and why activity declarations are structured as they are.
- Clarified launcher responsibility (`MainActivity`) and secondary activity roles.

## 6) Build and Test Configuration Comment Coverage

### `app/build.gradle.kts`
- Added section comments for plugin usage, SDK targeting, build types, Java compatibility, and dependency roles.

### `settings.gradle.kts`
- Added comments for repository filtering, centralized dependency resolution mode, and module inclusion graph.

### `gradle/libs.versions.toml`
- Added grouping comments for version keys, runtime libraries, test libraries, and plugin aliases.

### `app/src/test/java/com/kurdish/cryptogram/ExampleUnitTest.java`
- Added comments to explain host-side test purpose and why the placeholder assertion exists.

### `app/src/androidTest/java/com/kurdish/cryptogram/ExampleInstrumentedTest.java`
- Added comments clarifying instrumentation context lookup and package-identity validation.

## 7) Known Functional Status (Current)

- `Levels` lock/unlock gating is active based on stored progress.
- `MainMenu` heart timer/catch-up logic is active.
- `Settings` currently updates visual state only (not yet persisted).
- `Game` currently focuses on navigation and UI structure; gameplay wiring remains incremental.

## 8) Why the Levels Button Previously Could Close the App

Typical causes in this project context:
- `Levels` activity was not declared correctly in manifest (now declared).
- Crash from missing/mismatched view IDs in `activity_levels.xml` or `Levels.java`.
- Runtime exceptions in level-grid iteration or intent navigation paths.

The current code and manifest structure include the required declarations and IDs for normal navigation flow.
