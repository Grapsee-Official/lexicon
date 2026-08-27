#!/usr/bin/env python3
"""
Lexicon ETL — Validation Script
=============================================================================

Per spec:
  - "Do run a Python script validating that every word ID has at least one
     definition or translation."
  - "Do reject entries with unclosed HTML tags."

Standalone companion to etl_pipeline.py. Run it:
  - BEFORE the pipeline, on wiktextract's JSON output, to catch bad data
    early (saves a multi-hour ETL run on garbage input).
  - AFTER the pipeline, on the produced .db / .db.zst files, to confirm
    the spec's two hard gates held end-to-end.

Usage:
  python3 validate.py --json words.json
  python3 validate.py --db ./output/lexicon_en.db
  python3 validate.py --db ./output/lexicon_en.db.zst
  python3 validate.py --db-dir ./output
  python3 validate.py --db-dir ./output --report rejected.json

Exit codes:
  0  every checked entry passes both rules
  1  at least one entry was rejected (or a file could not be opened)
  2  CLI usage error

Rule interpretation:
  - "definition or translation":
      * JSON mode  (pre-ETL):  accepts `definitions`  OR  `translations`
        (wiktextract may provide translations that etl_pipeline.py currently
        drops on the floor — flagging only when BOTH are empty).
      * SQLite mode (post-ETL): the lexicon_sqlite.sql schema has no
        `translations` column, so only `definitions` is checked. This also
        re-confirms that etl_pipeline.validate_entries() did its job.
  - "unclosed HTML tags": uses the same regex as
    etl_pipeline.validate_entries() so the two tools agree on what counts.
"""

from __future__ import annotations

import argparse
import hashlib
import json
import os
import re
import sqlite3
import sys
import tempfile
import unicodedata
from pathlib import Path
from typing import Dict, Iterable, List, Optional, Tuple

try:
    import zstandard as zstd
    _HAS_ZSTD = True
except ImportError:
    _HAS_ZSTD = False


# ─── Constants (mirror etl_pipeline.py) ─────────────────────────────────────

SUPPORTED_LANGUAGES = {
    "en", "fr", "de", "es", "it", "pt", "nl", "sv", "ru", "pl", "tr", "ar",
    "zh", "ja", "ko", "vi", "th", "hi", "fa", "he", "el", "la", "eo", "ca",
    "fi", "hu", "cs", "da", "no", "ro", "sk", "uk", "id", "ms", "ta", "bn",
    "sw",
}

# Same regexes as etl_pipeline.validate_entries() so the two tools agree
# on what counts as "unclosed HTML". Known limitation (shared with the
# pipeline): self-closing tags like <br/> are treated as opening tags and
# will be flagged as "unclosed" because no matching </br> follows. This is
# acceptable for Wiktionary data where self-closing tags in definitions are
# rare; if it becomes a problem, fix both files together.
HTML_TAG_RE = re.compile(r"<[^>]+>")
UNCLOSED_RE = re.compile(r"<([a-z]+)[^>]*>(?:(?!</\1>).)*$", re.DOTALL)


# ─── Helpers ────────────────────────────────────────────────────────────────

def normalize_nfc(text: str) -> str:
    """NFC normalize — mirrors etl_pipeline.normalize_nfc()."""
    if not text:
        return ""
    return unicodedata.normalize("NFC", text)


def word_id(word: str, lang_code: str, pos: str) -> str:
    """Replicate etl_pipeline.WordEntry.id = sha256(word:lang:pos)[:16]."""
    raw = f"{normalize_nfc(word)}:{lang_code}:{pos or ''}"
    return hashlib.sha256(raw.encode("utf-8")).hexdigest()[:16]


def has_unclosed_html(*texts: str) -> bool:
    """Return True if any concatenated text has an opening tag with no
    matching close. Matches etl_pipeline.validate_entries() semantics."""
    blob = " ".join(t for t in texts if t)
    return bool(UNCLOSED_RE.search(blob))


def _as_str_list(value) -> List[str]:
    """Coerce a wiktextract field into a list of strings.
    wiktextract sometimes returns definitions as a list of dicts; we only
    treat string elements as definitions (matching the pipeline's
    `normalize_nfc(d)` call which would TypeError on a dict)."""
    if not value:
        return []
    out = []
    for item in value:
        if isinstance(item, str):
            out.append(item)
        elif isinstance(item, dict):
            # Some wiktextract versions embed the text under "text" or "gloss"
            txt = item.get("text") or item.get("gloss") or ""
            if isinstance(txt, str) and txt:
                out.append(txt)
    return out


# ─── Report ─────────────────────────────────────────────────────────────────

class Report:
    """Accumulates validation results for one source (file or directory)."""

    def __init__(self, source: str):
        self.source = source
        self.total = 0          # entries checked
        self.valid = 0          # entries that passed both rules
        self.rejected: List[dict] = []  # {id, word, language, reason, sample}

    def add_reject(self, wid: str, word: str, language: str,
                   reason: str, sample: str = "") -> None:
        self.rejected.append({
            "id": wid,
            "word": word,
            "language": language,
            "reason": reason,
            "sample": (sample or "")[:200],
        })

    def merge(self, other: "Report") -> None:
        self.total += other.total
        self.valid += other.valid
        self.rejected.extend(other.rejected)

    def __str__(self) -> str:
        lines = [
            f"  source:    {self.source}",
            f"  total:     {self.total}",
            f"  valid:     {self.valid}",
            f"  rejected:  {len(self.rejected)}",
        ]
        if self.rejected:
            by_reason: Dict[str, int] = {}
            for r in self.rejected:
                by_reason[r["reason"]] = by_reason.get(r["reason"], 0) + 1
            breakdown = ", ".join(f"{k}={v}" for k, v in sorted(by_reason.items()))
            lines.append(f"  by reason: {breakdown}")
            lines.append("  first 10 rejections:")
            for r in self.rejected[:10]:
                lines.append(
                    f"    [{r['reason']}] id={r['id']} "
                    f"word={r['word']!r} lang={r['language']!r}"
                )
                if r["sample"]:
                    lines.append(f"      sample: {r['sample']!r}")
        return "\n".join(lines)


# ─── JSON validation (wiktextract output, pre-ETL) ──────────────────────────

def _iter_wiktextract_records(path: str) -> Iterable[dict]:
    """Yield top-level dicts from a wiktextract JSON file.

    Accepts both a single top-level JSON array and newline-delimited JSON
    (JSONL), since wiktextract can emit either.
    """
    with open(path, "r", encoding="utf-8") as f:
        first = f.read(1)
        f.seek(0)
        if first == "[":
            data = json.load(f)
            yield from data
        else:
            # JSONL: one object per line
            for line in f:
                line = line.strip()
                if line and not line.startswith("#"):
                    yield json.loads(line)


def validate_json(path: str) -> Report:
    """Validate a wiktextract JSON file (pre-ETL).

    Iterates the same structure etl_pipeline.parse_wiktextract_json()
    consumes: top-level list of {word, lang_codes: [{lang, pos,
    definitions, etymology, examples, ...}]}.
    """
    report = Report(source=path)

    for entry in _iter_wiktextract_records(path):
        word = (entry.get("word") or "").strip()
        if not word:
            continue

        for lang_entry in entry.get("lang_codes", []) or []:
            lang_code = (lang_entry.get("lang") or "").lower()
            if lang_code not in SUPPORTED_LANGUAGES:
                continue

            pos = lang_entry.get("pos") or ""
            wid = word_id(word, lang_code, pos)
            report.total += 1

            definitions = [normalize_nfc(d)
                           for d in _as_str_list(lang_entry.get("definitions"))]
            translations = [normalize_nfc(t)
                            for t in _as_str_list(lang_entry.get("translations"))]
            etymology = normalize_nfc(lang_entry.get("etymology") or "")
            examples = [normalize_nfc(e)
                        for e in _as_str_list(lang_entry.get("examples"))]
            language = lang_code

            # Rule 1: at least one definition OR translation
            if not definitions and not translations:
                report.add_reject(wid, word, language, "missing_definition")
                continue

            # Rule 2: no unclosed HTML tags
            text_blob = " ".join(definitions + [etymology] + examples)
            if has_unclosed_html(text_blob):
                m = UNCLOSED_RE.search(text_blob)
                report.add_reject(
                    wid, word, language, "unclosed_html",
                    sample=m.group(0) if m else text_blob[:200],
                )
                continue

            report.valid += 1

    return report


# ─── SQLite validation (post-ETL) ───────────────────────────────────────────

def _open_db(path: str) -> Tuple[sqlite3.Connection, Optional[str]]:
    """Open a SQLite db, transparently decompressing .db.zst to a temp file.

    Returns (connection, temp_path_or_None). Caller must close the connection
    and unlink the temp path if non-None.
    """
    if str(path).endswith(".zst"):
        if not _HAS_ZSTD:
            raise RuntimeError(
                "Cannot validate .db.zst without the 'zstandard' package. "
                "Install it: pip install -r requirements.txt"
            )
        fd, tmp_path = tempfile.mkstemp(suffix=".db")
        os.close(fd)
        dctx = zstd.ZstdDecompressor()
        with open(path, "rb") as fin, open(tmp_path, "wb") as fout:
            dctx.copy_stream(fin, fout)
        conn = sqlite3.connect(tmp_path)
        return conn, tmp_path
    else:
        return sqlite3.connect(path), None


def validate_db(path: str) -> Report:
    """Validate a single SQLite .db (or .db.zst) file (post-ETL)."""
    report = Report(source=path)
    conn: Optional[sqlite3.Connection] = None
    tmp_path: Optional[str] = None
    try:
        conn, tmp_path = _open_db(path)
        # Columns per native/cloud/schema/lexicon_sqlite.sql. There is no
        # `translations` column, so Rule 1 here checks `definitions` only.
        rows = conn.execute(
            "SELECT id, word, definitions, etymology, examples, language "
            "FROM words"
        ).fetchall()
    finally:
        if conn is not None:
            conn.close()
        if tmp_path is not None:
            try:
                os.unlink(tmp_path)
            except OSError:
                pass

    for wid, word, definitions_json, etymology, examples_json, language in rows:
        report.total += 1

        wid = wid or ""
        word = word or ""
        language = language or ""

        if not wid:
            report.add_reject(wid, word, language, "missing_id")
            continue

        try:
            definitions = json.loads(definitions_json) if definitions_json else []
        except (json.JSONDecodeError, TypeError):
            report.add_reject(wid, word, language,
                              "malformed_definitions_json",
                              sample=str(definitions_json))
            continue

        try:
            examples = json.loads(examples_json) if examples_json else []
        except (json.JSONDecodeError, TypeError):
            examples = []

        # Rule 1: at least one definition (no translations column in schema)
        if not definitions:
            report.add_reject(wid, word, language, "missing_definition")
            continue

        # Rule 2: no unclosed HTML tags. After etl_pipeline.validate_entries()
        # strips all HTML, the .db should contain zero `<` chars; this check
        # confirms that stripping actually ran.
        text_blob = " ".join(
            [d if isinstance(d, str) else str(d) for d in definitions]
            + [etymology or ""]
            + [e if isinstance(e, str) else str(e) for e in examples]
        )
        if has_unclosed_html(text_blob):
            m = UNCLOSED_RE.search(text_blob)
            report.add_reject(
                wid, word, language, "unclosed_html",
                sample=m.group(0) if m else text_blob[:200],
            )
            continue

        report.valid += 1

    return report


def validate_db_dir(dir_path: str) -> Report:
    """Validate every .db and .db.zst file in a directory."""
    aggregate = Report(source=dir_path)
    p = Path(dir_path)
    db_paths = sorted(set(list(p.glob("*.db")) + list(p.glob("*.db.zst"))))
    if not db_paths:
        print(f"WARNING: no .db or .db.zst files found in {dir_path}",
              file=sys.stderr)
    for db_path in db_paths:
        print(f"\nValidating {db_path.name} ...")
        try:
            r = validate_db(str(db_path))
        except Exception as e:
            print(f"  ERROR: could not validate {db_path.name}: {e}",
                  file=sys.stderr)
            aggregate.rejected.append({
                "id": "", "word": "", "language": "",
                "reason": f"file_error: {e}", "sample": str(db_path),
            })
            continue
        print(r)
        aggregate.merge(r)
    return aggregate


# ─── CLI ────────────────────────────────────────────────────────────────────

def main(argv: Optional[List[str]] = None) -> int:
    parser = argparse.ArgumentParser(
        prog="validate.py",
        description="Lexicon ETL validator — enforces the two spec hard gates: "
                    "(1) every word ID has ≥1 definition/translation, "
                    "(2) no unclosed HTML tags.",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="""\
Exit codes:
  0  every checked entry passes both rules
  1  at least one entry was rejected (or a file failed to open)
  2  CLI usage error

Examples:
  python3 validate.py --json words.json
  python3 validate.py --db ./output/lexicon_en.db
  python3 validate.py --db ./output/lexicon_en.db.zst
  python3 validate.py --db-dir ./output --report rejected.json
""",
    )
    src = parser.add_mutually_exclusive_group(required=True)
    src.add_argument("--json", metavar="PATH",
                     help="validate a wiktextract JSON file (pre-ETL)")
    src.add_argument("--db", metavar="PATH",
                     help="validate a single SQLite .db or .db.zst file (post-ETL)")
    src.add_argument("--db-dir", metavar="DIR",
                     help="validate every .db / .db.zst in a directory (post-ETL)")
    parser.add_argument("--report", metavar="PATH",
                        help="optional: write rejected entries (with reasons) "
                             "as JSON to this path")
    args = parser.parse_args(argv)

    if args.db_dir and os.path.exists(args.db_dir) and not os.path.isdir(args.db_dir):
        parser.error(f"--db-dir expects a directory, but {args.db_dir!r} is not a directory")

    if args.json:
        if not os.path.isfile(args.json):
            print(f"ERROR: file not found: {args.json}", file=sys.stderr)
            return 1
        report = validate_json(args.json)
    elif args.db:
        if not os.path.isfile(args.db):
            print(f"ERROR: file not found: {args.db}", file=sys.stderr)
            return 1
        report = validate_db(args.db)
    else:  # --db-dir
        if not os.path.isdir(args.db_dir):
            print(f"ERROR: directory not found: {args.db_dir}", file=sys.stderr)
            return 1
        report = validate_db_dir(args.db_dir)

    # ─── Summary ───────────────────────────────────────────────────────────
    print("\n" + "=" * 60)
    print("VALIDATION SUMMARY")
    print("=" * 60)
    print(report)
    print("=" * 60)

    if args.report:
        payload = {
            "source": report.source,
            "total": report.total,
            "valid": report.valid,
            "rejected_count": len(report.rejected),
            "rejected": report.rejected,
        }
        with open(args.report, "w", encoding="utf-8") as f:
            json.dump(payload, f, indent=2, ensure_ascii=False)
        print(f"\nRejected entries written to: {args.report}")

    if report.total == 0:
        print("\nWARNING: no entries were checked.", file=sys.stderr)
        return 1

    return 0 if not report.rejected else 1


if __name__ == "__main__":
    sys.exit(main())
