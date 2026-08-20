# syntax=docker/dockerfile:1
#
# Lexora ETL — reproducible build environment
# ===========================================================================
# Produces an image that can run the full Lexora dictionary ETL pipeline:
#
#   1. wiktextract   — parse enwiktionary XML dump → structured JSON
#   2. etl_pipeline  — JSON → NFC → validate → dedup → SQLite FTS5 → Zstd-19
#   3. validate      — enforce "every word ID has ≥1 definition" + "no
#                      unclosed HTML tags" (per spec)
#
# Build context MUST be the `native/` directory so the shared SQLite schema
# at `cloud/schema/lexora_sqlite.sql` is in scope:
#
#     docker build -f native/etl/Dockerfile -t lexora-etl native/
#
# Or, from inside native/:
#
#     docker build -f etl/Dockerfile -t lexora-etl .
#
# Run (mount the dump + an output dir):
#
#     docker run --rm -it \
#         -v "$HOME/lexora-etl:/work" \
#         -v "$(pwd)/etl/output:/out" \
#         lexora-etl \
#         bash -c '
#           wiktextract --pages /work/dump/enwiktionary-latest-pages-articles.xml \
#                       --output /work/words.json &&
#           python3 /app/etl/scripts/etl_pipeline.py \
#                       --input /work/words.json --output /out &&
#           python3 /app/etl/scripts/validate.py --db-dir /out
#         '
#
# Image layout:
#   /app/etl/scripts/etl_pipeline.py      — main pipeline
#   /app/etl/scripts/validate.py          — standalone validator
#   /app/etl/scripts/requirements.txt     — Python deps (zstandard only)
#   /app/etl/scripts/README.md            — human-facing docs
#   /app/etl/cloud/schema/lexora_sqlite.sql — FTS5 schema (where etl_pipeline
#                                             looks for it via Path(__file__)
#                                             .parent.parent / "cloud" / ...)
#   /app/cloud/schema/lexora_sqlite.sql   — canonical repo path (for any
#                                             downstream tooling)
# ===========================================================================

FROM python:3.11-slim AS base

# ─── System dependencies ────────────────────────────────────────────────────
# bzip2            decompress the Wikimedia .xml.bz2 dump
# ca-certificates   HTTPS for pip + Wikimedia downloads
# curl             optional: fetch the dump from inside the container
# git              wiktextract uses git for some auxiliary data fetches
# build-essential  wiktextract & zstandard C extensions
# libxml2-dev,
# libxslt1-dev     wiktextract parses MediaWiki XML via lxml
# libzstd-dev      native zstd bindings for zstandard
RUN apt-get update && apt-get install -y --no-install-recommends \
        bzip2 \
        ca-certificates \
        curl \
        git \
        build-essential \
        libxml2-dev \
        libxslt1-dev \
        libzstd-dev \
    && rm -rf /var/lib/apt/lists/*

ENV PYTHONUNBUFFERED=1 \
    PYTHONDONTWRITEBYTECODE=1 \
    PIP_NO_CACHE_DIR=1 \
    PIP_DISABLE_PIP_VERSION_CHECK=1

WORKDIR /app

# ─── Python dependencies (zstandard) ────────────────────────────────────────
# Install from requirements.txt first so this layer is cached when only the
# scripts change. wiktextract is intentionally NOT in requirements.txt — it
# is a standalone CLI tool with a heavy dependency tree.
COPY etl/scripts/requirements.txt /app/etl/scripts/requirements.txt
RUN pip install --upgrade pip \
    && pip install -r /app/etl/scripts/requirements.txt

# ─── wiktextract (standalone CLI tool) ──────────────────────────────────────
# Provides the `wiktextract` console script used to parse the Wiktionary
# XML dump into JSON. Installed separately from requirements.txt per the
# task spec ("wiktextract is a separate tool, not pip" — meaning it is not
# a library imported by etl_pipeline.py).
RUN pip install wiktextract

# ─── Application code ───────────────────────────────────────────────────────
COPY etl/scripts/etl_pipeline.py /app/etl/scripts/etl_pipeline.py
COPY etl/scripts/validate.py     /app/etl/scripts/validate.py
COPY etl/scripts/README.md       /app/etl/scripts/README.md

# The shared SQLite FTS5 schema. etl_pipeline.py (line 139) resolves it as
#   Path(__file__).parent.parent / "cloud" / "schema" / "lexora_sqlite.sql"
# which from /app/etl/scripts/etl_pipeline.py points to
# /app/etl/cloud/schema/lexora_sqlite.sql — so that's where we put it.
# A second copy lands at the repo-canonical /app/cloud/schema/ path for any
# downstream tooling that looks it up relative to the repo root.
COPY cloud/schema/lexora_sqlite.sql /app/etl/cloud/schema/lexora_sqlite.sql
COPY cloud/schema/lexora_sqlite.sql /app/cloud/schema/lexora_sqlite.sql

WORKDIR /app/etl/scripts

# ─── Build-time smoke test ──────────────────────────────────────────────────
# Fail the build early if any of the three tools is not callable, so a
# broken base image never ships.
RUN python3 -c "import zstandard; print('zstandard', zstandard.__version__)" \
    && command -v wiktextract \
    && python3 etl_pipeline.py --help >/dev/null \
    && python3 validate.py    --help >/dev/null

# Empty entrypoint so callers can invoke any of the three tools directly:
#   docker run lexora-etl                                   # → pipeline --help
#   docker run lexora-etl python3 etl_pipeline.py --input … # → run pipeline
#   docker run lexora-etl python3 validate.py    --db-dir … # → run validator
#   docker run lexora-etl wiktextract --pages … --output …  # → run wiktextract
#   docker run lexora-etl bash                               # → interactive shell
ENTRYPOINT []
CMD ["python3", "etl_pipeline.py", "--help"]
