plugins {
    // Applies Android application plugin from the centralized version catalog.
    alias(libs.plugins.android.application)
}

android {
    // Namespace controls generated R/package ownership for this app module.
    namespace = "com.kurdish.cryptogram"

    // Compile SDK defines the Android API used to compile project sources.
    // Using release(36) keeps API intent explicit for current coursework target.
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        // Application identity and compatibility window for installable APK.
        applicationId = "com.kurdish.cryptogram"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        // Instrumentation runner used by androidTest sources.
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            // Minification is disabled for now to simplify debugging during development stage.
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    // Java 11 toolchain compatibility for source and bytecode generation.
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    // Core UI stack for Activities, Material components, and Constraint-based layouts.
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // Test stack split by host-side unit tests and device-side instrumentation tests.
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}