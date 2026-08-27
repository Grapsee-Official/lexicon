# Lexicon ETL Pipeline

Converts the latest English Wiktionary XML dump into 36 per-language
SQLite FTS5 databases, compressed with Zstandard, ready for in-app packaging
and delta-sync to the cloud.

```
enwiktionary dump (.xml.bz2)
        │  (1) download from Wikimedia
        ▼
enwiktionary-latest-pages-articles.xml
        │  (2) wiktextract  →  parses wikitext into structured JSON
        ▼
words.json
        │  (3) etl_pipeline.py  →  NFC normalize, validate, dedup,
        │                          SQLite FTS5 build, Zstd-19 compress
        ▼
output/
├── lexicon_en.db.zst          ← one compressed .db per language
├── lexicon_fr.db.zst
├── ...
└── manifest.json             ← SHA-256 + word counts + schema version
```

## Prerequisites

| Tool | Why | Install |
|---|---|---|
| **Python 3.11+** | Runs `etl_pipeline.py` and `validate.py` | https://python.org |
| **zstandard** (pip) | Zstd-19 --ultra compression of the .db files | `pip install -r requirements.txt` |
| **wiktextract** | Parses Wiktionary wikitext XML → structured JSON | `pipx install wiktextract` (or `pip install wiktextract`) |
| **bzip2** | Decompress the Wikimedia dump | preinstalled on most systems / `apt install bzip2` |
| **~20 GB free disk** | Enwiktionary XML dump + wiktextract JSON output | — |
| **~16 GB RAM** | wiktextract is memory-hungry | — |

> `wiktextract` is a **standalone CLI tool**, not a library imported by
> `etl_pipeline.py`. It is intentionally absent from `requirements.txt`.
> Install it once into an isolated environment with `pipx`.

---

## Step 1 — Download the Wiktionary dump

The canonical source is Wikimedia's own dump host. Pull the latest
*pages-articles* XML, bz2-compressed:

```bash
mkdir -p ~/lexicon-etl/dump && cd ~/lexicon-etl/dump

# Latest snapshot (~1.2 GB compressed, ~6 GB uncompressed)
curl -L -O https://dumps.wikimedia.org/enwiktionary/latest/enwiktionary-latest-pages-articles.xml.bz2

# Decompress (parallel pbzip2 is ~5x faster if available)
bunzip2 enwiktionary-latest-pages-articles.xml.bz2
# or, preserving the .bz2:
# bzip2 -dk enwiktionary-latest-pages-articles.xml.bz2
```

**Pinned date alternative** (recommended for reproducible builds — replace
`YYYYMMDD` with the snapshot you want):

```bash
curl -L -O https://dumps.wikimedia.org/enwiktionary/YYYYMMDD/enwiktionary-YYYYMMDD-pages-articles.xml.bz2
```

You should now have a file named:

```
enwiktionary-latest-pages-articles.xml     (~6 GB)
```

---

## Step 2 — Run wiktextract

`wiktextract` reads the XML dump and emits a single JSON file containing every
word entry with its language codes, definitions, etymology, examples, etc.

```bash
cd ~/lexicon-etl

wiktextract \
  --pages dump/enwiktionary-latest-pages-articles.xml \
  --output words.json
```

Notes:

- This is the slowest stage (~2–4 hours on a 16-core box) and uses a lot of
  RAM. Run it on a beefy machine or in the provided Docker container (see
  `../Dockerfile`).
- `wiktextract` writes JSONL-ish records; `etl_pipeline.py` accepts either a
  top-level JSON array **or** a stream of newline-delimited JSON objects.
- If you only need a subset of languages, you can pass
  `--language en,fr,de,...` to skip the rest and cut runtime dramatically.

You should now have:

```
words.json     (~3–5 GB)
```

---

## Step 3 — (Optional) Validate the wiktextract output

Before running the full ETL, sanity-check the JSON. The validator enforces the
two spec rules:

> *Do run a Python script validating that every word ID has at least one
> definition or translation.*
> *Do reject entries with unclosed HTML tags.*

```bash
cd /path/to/my-project/native/etl/scripts

python3 validate.py --json ~/lexicon-etl/words.json
```

Exit code `0` = every entry passes; exit code `1` = at least one entry was
rejected. Pass `--report rejected.json` to dump the offending IDs.

---

## Step 4 — Run the ETL pipeline

```bash
cd /path/to/my-project/native/etl/scripts

python3 etl_pipeline.py \
  --input  ~/lexicon-etl/words.json \
  --output ./output
```

What it does, per stage (matches the spec's hard gates):

1. **Parse** wiktextract JSON → group `WordEntry` objects by ISO 639-1 code.
2. **Validate** each entry: reject unclosed HTML tags and entries with zero
   definitions (delegated to `validate_entries`).
3. **NFC-normalize** every text field (definitions, etymology, examples,
   synonyms, antonyms, word itself) — per spec.
4. **Deduplicate** by `sha256(word + language + part_of_speech)`.
5. **Build** one `lexicon_<lang>.db` per language using the shared schema at
   `native/cloud/schema/lexicon_sqlite.sql` (FTS5 with
   `tokenize='porter unicode61'`, external-content tables, WAL mode,
   delta-sync triggers).
6. **Compress** each `.db` with Zstd level 19, `--ultra`, multithreaded
   (`threads=0` = all cores) — matches the spec's
   `zstd -19 --ultra -T0` invocation.
7. **Emit** `manifest.json` with SHA-256 checksums, byte sizes, word counts,
   and schema version for every `.db.zst`.

Output layout:

```
output/
├── lexicon_en.db.zst
├── lexicon_fr.db.zst
├── lexicon_de.db.zst
├── …                          (one per supported language, 36 total)
└── manifest.json
```

---

## Step 5 — (Optional) Validate the produced databases

After ETL, re-run the validator against the SQLite databases to confirm the
hard gate held end-to-end. This catches any regressions where a downstream
stage (dedup, insert) silently dropped a definition:

```bash
python3 validate.py --db-dir ./output
```

The validator will transparently decompress any `.db.zst` it encounters.

---

## Running everything inside Docker

A self-contained `Dockerfile` lives at `../Dockerfile` (one directory up
from this `scripts/` folder). It bakes in Python 3.11, `zstandard`, and
`wiktextract` so you can run the whole pipeline with no host-side installs
beyond Docker itself.

The build context **must** be the `native/` directory (not `native/etl/`)
so the shared SQLite schema at `native/cloud/schema/lexicon_sqlite.sql` is
in scope:

```bash
cd /path/to/my-project/native

# Build the image once
docker build -f etl/Dockerfile -t lexicon-etl .
```

Run wiktextract + ETL + validate in one shot, mounting the dump and output
dir:

```bash
docker run --rm -it \
  -v "$HOME/lexicon-etl:/work" \
  -v "$(pwd)/etl/output:/out" \
  lexicon-etl \
  bash -c '
    wiktextract --pages /work/dump/enwiktionary-latest-pages-articles.xml \
                --output /work/words.json &&
    python3 /app/etl/scripts/etl_pipeline.py \
                --input /work/words.json \
                --output /out &&
    python3 /app/etl/scripts/validate.py --db-dir /out
  '
```

The image's entrypoint is intentionally empty so you can invoke any of the
three tools directly — see the Dockerfile header for the full command
matrix.

---

## File reference

| File | Purpose |
|---|---|
| `etl_pipeline.py` | Main pipeline: JSON → NFC → validate → dedup → SQLite FTS5 → Zstd-19 → manifest |
| `validate.py` | Standalone validator enforcing "≥1 definition per word ID" and "no unclosed HTML" |
| `requirements.txt` | Python deps (just `zstandard` — wiktextract is installed separately) |
| `../Dockerfile` | Reproducible build environment (Python 3.11-slim + zstandard + wiktextract) |
| `../../cloud/schema/lexicon_sqlite.sql` | The FTS5 schema applied to every per-language `.db` |

## Troubleshooting

- **`wiktextract: command not found`** — install it: `pipx install wiktextract`
  (recommended) or `pip install wiktextract`. The Docker image already has it.
- **`ModuleNotFoundError: No module named 'zstandard'`** — run
  `pip install -r requirements.txt` from this directory.
- **OOM during wiktextract** — give the container/host ≥16 GB RAM, or run
  wiktextract with `--language en,fr,de,...` to filter.
- **`integrity_check` warnings from `etl_pipeline.py`** — re-run; if they
  persist, inspect the offending `.db` with `sqlite3` before compression
  (comment out the `os.unlink(db_path)` line in `etl_pipeline.py` to keep
  the uncompressed copy for debugging).
- **`validate.py` exit 1** — read the report; rejected entries are listed
  with the failing rule (`missing_definition` or `unclosed_html`).
