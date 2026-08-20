// KMM build.gradle.kts — Kotlin Multiplatform library module for the Lexora engine.
// Targets Android (JNI bridge to the native C++ engine) and iOS (C-interop).
//
// Fixed in P3-FIX-05:
//   * Added explicit Kotlin Multiplatform plugin version (1.9.22) and
//     com.android.library plugin (required for the `android {}` block to compile).
//   * Corrected the CMake path: was `../../cpp/CMakeLists.txt` which resolved to
//     `<repo>/cpp/CMakeLists.txt` (missing). Now `../cpp/CMakeLists.txt`, anchored
//     to the kmm module root via `file(...)`, resolves to `native/cpp/CMakeLists.txt`.
//   * Companion settings.gradle.kts + minimal AndroidManifest.xml added alongside.

plugins {
    kotlin("multiplatform") version "1.9.22"
    id("com.android.library") version "8.2.2"
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "17"
            }
        }
    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
            }
        }
        val androidMain by getting {
            dependencies {
                implementation("androidx.core:core-ktx:1.13.1")
            }
        }
        val iosMain by creating {
            dependencies { }
        }
        val iosX64Main by getting { dependsOn(iosMain) }
        val iosArm64Main by getting { dependsOn(iosMain) }
        val iosSimulatorArm64Main by getting { dependsOn(iosMain) }
    }
}

android {
    namespace = "com.lexora.engine"
    compileSdk = 34

    defaultConfig {
        minSdk = 21
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    // The KMM library uses the source-set layout `src/androidMain/...`, so point
    // the Android plugin's main manifest at the KMM-style location.
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")

    externalNativeBuild {
        cmake {
            // `file(...)` resolves relative to this module's project dir (kmm root).
            // One level up from `native/kmm/` lands at `native/`, then into `cpp/`.
            path = file("../cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }
}
