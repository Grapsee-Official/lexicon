// settings.gradle.kts — Lexora Ktor Cloud Sync Server
// Pinned to Gradle 8.5+ (compatible with Kotlin 1.9.22 and Ktor 2.3.7).

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        google()
    }
}

rootProject.name = "lexora-sync-server"
