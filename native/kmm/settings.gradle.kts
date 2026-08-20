// settings.gradle.kts — KMM library build root.
// Standalone Gradle build for the Lexora Kotlin Multiplatform engine module.
// (For composite builds, this module can also be `includeBuild`-ed from the
//  Android app project at ../android.)

pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "LexoraEngine"
