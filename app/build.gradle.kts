/**
 * Android Application Module Build Script:
 * - This file configures the build settings for the 'app' module.
 * - It defines plugins, SDK versions, package names, build types, and dependencies.
 * - Uses Kotlin DSL (build.gradle.kts) for type-safe build configuration.
 */
plugins {
    // Apply the Android Application plugin using an alias from the version catalog.
    // This plugin enables Android-specific build tasks and configurations.
    alias(libs.plugins.android.application)
}

android {
    // Unique namespace used for R file and BuildConfig generation.
    namespace = "com.kurdish.cryptogram"

    // Specifies the API level used to compile the project.
    // Level 36 provides access to the latest Android platform features.
    compileSdk = 36

    defaultConfig {
        // Unique identifier for the application on the Google Play Store.
        applicationId = "com.kurdish.cryptogram"
        
        // Minimum Android version required to run the application (API 24 = Android 7.0).
        minSdk = 24
        
        // Target Android version for which the app is optimized.
        targetSdk = 36
        
        // Versioning for the application.
        versionCode = 1
        versionName = "1.0"

        // Specifies the test runner to use for instrumentation tests.
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        // Configuration for the 'release' build variant.
        release {
            // Disable code shrinking and obfuscation for simplified development builds.
            isMinifyEnabled = false
            // Define ProGuard/R8 rules for code optimization.
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    // Java version compatibility for source code and compiled bytecode.
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

/**
 * Dependency Management:
 * - Defines all libraries and frameworks required by the application.
 * - 'implementation' dependencies are used for compiling the main application.
 * - 'testImplementation' and 'androidTestImplementation' are for testing frameworks.
 */
dependencies {
    // Core AndroidX libraries for modern UI and activity management.
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // Unit Testing framework for logic verification on the JVM.
    testImplementation(libs.junit)
    // Instrumented testing libraries for UI and device-specific tests.
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Flexbox Layout: Provides a flexible box layout system similar to CSS Flexbox.
    implementation(libs.flexbox)
}
