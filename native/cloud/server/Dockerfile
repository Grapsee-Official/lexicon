# Dockerfile — Lexicon Ktor Cloud Sync Server
# Multi-stage build:
#   Stage 1 (builder)  : gradle:8.5-jdk17  → compiles + assembles runnable dist via `installDist`
#   Stage 2 (runtime)  : eclipse-temurin:17-jre-jammy (slim JRE only)

# ---------- Stage 1: Build ----------
FROM gradle:8.5-jdk17-jammy AS builder

WORKDIR /home/gradle/build

# Copy Gradle project descriptor files first to leverage Docker layer caching
# of dependency downloads (build.gradle.kts / settings.gradle.kts rarely change
# vs. LexiconSyncServer.kt).
COPY settings.gradle.kts build.gradle.kts ./

# Pre-fetch dependencies. Allowed to fail (no sources yet) — the next step
# will recompile with sources present.
RUN gradle --no-daemon dependencies --quiet || true

# Copy the Kotlin source (flat layout — file lives at project root).
COPY LexiconSyncServer.kt ./

# Assemble a runnable distribution into build/install/lexicon-sync-server/
# (bin/lexicon-sync-server + lib/*.jar) using the `application` plugin.
RUN gradle --no-daemon installDist --no-build-cache

# ---------- Stage 2: Runtime ----------
FROM eclipse-temurin:17-jre-jammy

# Non-root user for the runtime image + curl for HEALTHCHECK
RUN apt-get update && apt-get install -y --no-install-recommends curl && \
    rm -rf /var/lib/apt/lists/* && \
    groupadd --system lexicon && \
    useradd  --system --gid lexicon --home-dir /app --shell /usr/sbin/nologin lexicon

WORKDIR /app

# Copy the assembled distribution (bin/ + lib/)
COPY --from=builder /home/gradle/build/build/install/lexicon-sync-server/ ./

# Ensure the startup script is executable
RUN chmod +x bin/lexicon-sync-server && \
    chown -R lexicon:lexicon /app

USER lexicon

# Default runtime environment — override at `docker run -e ...`
ENV DATABASE_URL="jdbc:postgresql://localhost:5432/lexicon" \
    DATABASE_USER="lexicon" \
    DATABASE_PASSWORD="" \
    KTOR_PORT="8080"

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s --start-period=15s --retries=3 \
    CMD curl -fsS http://127.0.0.1:8080/api/v1/languages || exit 1

ENTRYPOINT ["./bin/lexicon-sync-server"]
