pluginManagement {
    repositories {
        // Limit Google repo scan to Android/Google coordinates for faster and safer resolution.
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        // General plugin and artifact fallback repositories.
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    // Resolves JDK toolchains consistently across environments.
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    // Prevent per-module repositories to keep dependency origin centralized and predictable.
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

// Project naming and module inclusion graph.
rootProject.name = "CRYPTOGRAM"
include(":app")
