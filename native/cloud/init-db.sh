#!/usr/bin/env bash
# init-db.sh — wait for PostgreSQL to be ready, then apply the Lexora schema.
#
# This is a manual / fallback initializer. On a fresh `docker compose up` the
# postgres image already auto-applies schema/lexora_postgres.sql on first
# startup via /docker-entrypoint-initdb.d/. Use this script when:
#   - The data volume already existed and you want to (re-)apply the schema.
#   - You are pointing at an external PostgreSQL instance.
#   - The auto-init step failed and you need to re-run it cleanly.
#
# The schema is idempotent (CREATE EXTENSION/TABLE/INDEX IF NOT EXISTS,
# CREATE OR REPLACE FUNCTION, CREATE TRIGGER), so re-running is safe.
#
# Usage:
#   ./init-db.sh                                              # use defaults below
#   PGHOST=localhost PGPORT=5432 PGUSER=lexora PGDATABASE=lexora \
#     PGPASSWORD=lexora_password ./init-db.sh
#   SCHEMA_FILE=/path/to/custom.sql ./init-db.sh              # override schema
#   MAX_WAIT_SECONDS=120 ./init-db.sh                         # extend wait
#
# Exit codes:
#   0  schema applied successfully
#   1  PostgreSQL did not become ready in time
#   2  schema file not found

set -euo pipefail

# --- Configuration (env-overridable) -----------------------------------------
PGHOST="${PGHOST:-localhost}"
PGPORT="${PGPORT:-5432}"
PGUSER="${PGUSER:-lexora}"
PGDATABASE="${PGDATABASE:-lexora}"
PGPASSWORD="${PGPASSWORD:-lexora_password}"
MAX_WAIT_SECONDS="${MAX_WAIT_SECONDS:-60}"

# Resolve the schema file relative to this script so it works regardless of CWD.
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SCHEMA_FILE="${SCHEMA_FILE:-${SCRIPT_DIR}/schema/lexora_postgres.sql}"

# --- Pre-flight checks -------------------------------------------------------
if [[ ! -f "$SCHEMA_FILE" ]]; then
  echo "ERROR: schema file not found: $SCHEMA_FILE" >&2
  exit 2
fi

if ! command -v psql >/dev/null 2>&1; then
  echo "ERROR: psql client not found on PATH. Install postgresql-client." >&2
  exit 2
fi

# Export PGPASSWORD once so every psql/pg_isready invocation inherits it.
export PGPASSWORD

echo "==> Waiting for PostgreSQL at ${PGHOST}:${PGPORT} (user=${PGUSER}, db=${PGDATABASE})..."
echo "    schema: ${SCHEMA_FILE}"
echo "    timeout: ${MAX_WAIT_SECONDS}s"

# --- Wait for PostgreSQL -----------------------------------------------------
# Prefer pg_isready (lightweight, ships with postgresql-client). Fall back to
# a `SELECT 1` round-trip if pg_isready is not installed.
ELAPSED=0

wait_for_postgres() {
  if command -v pg_isready >/dev/null 2>&1; then
    pg_isready -h "$PGHOST" -p "$PGPORT" -U "$PGUSER" -d "$PGDATABASE" -q
  else
    psql -h "$PGHOST" -p "$PGPORT" -U "$PGUSER" -d "$PGDATABASE" \
      -tAq -c "SELECT 1" >/dev/null 2>&1
  fi
}

until wait_for_postgres; do
  if [[ $ELAPSED -ge $MAX_WAIT_SECONDS ]]; then
    echo "ERROR: PostgreSQL did not become ready within ${MAX_WAIT_SECONDS}s" >&2
    exit 1
  fi
  echo "    not ready yet (${ELAPSED}s elapsed) — retrying in 2s..."
  sleep 2
  ELAPSED=$((ELAPSED + 2))
done

echo "==> PostgreSQL is ready (after ${ELAPSED}s)."

# --- Apply the schema --------------------------------------------------------
echo "==> Applying schema..."
psql \
  -h "$PGHOST" \
  -p "$PGPORT" \
  -U "$PGUSER" \
  -d "$PGDATABASE" \
  -v ON_ERROR_STOP=1 \
  -f "$SCHEMA_FILE"

echo "==> Schema applied successfully."
echo "    Tables: words, language_packs, user_sync_state, sync_log, integrity_log"
echo "    Extensions: uuid-ossp, pg_trgm"
