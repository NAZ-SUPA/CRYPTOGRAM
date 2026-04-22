// Top-level Gradle script:
// Keeps plugin versions centralized and avoids duplicating plugin coordinates in each module. implementation("com.google.android.flexbox:flexbox:3.0.0")
plugins {
    alias(libs.plugins.android.application) apply false
}