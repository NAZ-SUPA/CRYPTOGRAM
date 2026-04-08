// Top-level Gradle script:
// Keeps plugin versions centralized and avoids duplicating plugin coordinates in each module.
plugins {
    alias(libs.plugins.android.application) apply false
}