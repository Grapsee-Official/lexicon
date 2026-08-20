// KMM build.gradle.kts
plugins {
    kotlin("multiplatform")
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
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    externalNativeBuild {
        cmake {
            path = file("../../cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }
}
