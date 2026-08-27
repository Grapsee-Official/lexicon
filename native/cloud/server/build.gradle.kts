// build.gradle.kts — Lexicon Ktor Cloud Sync Server
// Ktor 2.3.7 / Kotlin 1.9.22 / PostgreSQL JDBC 42.7.2 / JRE 17
// Layout: flat (LexiconSyncServer.kt lives in project root, not under src/main/kotlin).

plugins {
    kotlin("jvm") version "1.9.22"
    application
}

group = "com.lexicon.cloud"
version = "1.0.0"

application {
    // The Kotlin compiler suffixes the file-level main() class with `Kt`.
    mainClass.set("com.lexicon.cloud.server.LexiconSyncServerKt")
}

kotlin {
    jvmToolchain(17)
}

// Flat source layout: compile LexiconSyncServer.kt from the project root
// while excluding Gradle's own build/ output directory.
sourceSets {
    main {
        kotlin {
            srcDir(".")
            exclude("build/**", "**/build/**")
        }
    }
}

dependencies {
    // --- Ktor 2.3.7 server stack ---
    implementation("io.ktor:ktor-server-core:2.3.7")
    implementation("io.ktor:ktor-server-netty:2.3.7")
    implementation("io.ktor:ktor-server-content-negotiation:2.3.7")
    implementation("io.ktor:ktor-serialization-gson:2.3.7")
    implementation("io.ktor:ktor-server-cors:2.3.7")
    implementation("io.ktor:ktor-server-compression:2.3.7")
    implementation("io.ktor:ktor-server-default-headers:2.3.7")
    implementation("io.ktor:ktor-server-call-logging:2.3.7")
    implementation("io.ktor:ktor-server-status-pages:2.3.7")

    // --- Serialization / JSON ---
    implementation("com.google.code.gson:gson:2.10.1")

    // --- Logging (Ktor call-logging expects SLF4J on the classpath) ---
    implementation("ch.qos.logback:logback-classic:1.4.14")

    // --- PostgreSQL JDBC driver 42.7.2 ---
    implementation("org.postgresql:postgresql:42.7.2")

    // --- Connection pooling (HikariCP) — used implicitly by future refactors
    // that wrap raw DriverManager.getConnection in a pool. Optional but cheap. ---
    implementation("com.zaxxer:HikariCP:5.1.0")
}

tasks.withType<JavaExec> {
    // Forward runtime env vars (DATABASE_URL etc.) to the JVM
    systemProperty("DATABASE_URL", System.getenv("DATABASE_URL") ?: "")
    systemProperty("DATABASE_USER", System.getenv("DATABASE_USER") ?: "")
    systemProperty("DATABASE_PASSWORD", System.getenv("DATABASE_PASSWORD") ?: "")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs = listOf("-Xjsr305=strict")
    }
}

// Produce a runnable distribution (build/install/lexicon-sync-server/) via
// `gradle installDist` — used by the Dockerfile's runtime stage.
tasks.named<CreateStartScripts>("startScripts") {
    applicationName = "lexicon-sync-server"
}
