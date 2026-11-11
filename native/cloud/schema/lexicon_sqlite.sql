-- Lexicon SQLite FTS5 Schema
-- Per spec: "Do create FTS5 virtual tables with tokenize='porter unicode61'.
--           Do external content tables to save space."
-- Per spec: "Do use PRAGMA journal_mode=WAL."

PRAGMA journal_mode=WAL;
PRAGMA foreign_keys=ON;

-- Base words table (external content for FTS5)
CREATE TABLE IF NOT EXISTS words (
    id TEXT PRIMARY KEY,
    word TEXT NOT NULL,
    phonetic TEXT,
    part_of_speech TEXT,
    definitions TEXT,        -- JSON array
    etymology TEXT,
    synonyms TEXT,           -- JSON array
    antonyms TEXT,           -- JSON array
    examples TEXT,           -- JSON array
    language TEXT,
    word_nfc TEXT,           -- NFC-normalized word for exact matching
    word_no_diacritics TEXT,  -- Diacritic-stripped for insensitive search
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- FTS5 full-text search index (external content table)
CREATE VIRTUAL TABLE IF NOT EXISTS words_fts USING fts5(
    word,
    definitions,
    etymology,
    synonyms,
    content='words',
    content_rowid='rowid',
    tokenize='porter unicode61'
);

-- Triggers to keep FTS5 index in sync with base table
CREATE TRIGGER IF NOT EXISTS words_ai AFTER INSERT ON words BEGIN
    INSERT INTO words_fts(rowid, word, definitions, etymology, synonyms)
    VALUES (new.rowid, new.word, new.definitions, new.etymology, new.synonyms);
END;

CREATE TRIGGER IF NOT EXISTS words_ad AFTER DELETE ON words BEGIN
    INSERT INTO words_fts(words_fts, rowid, word, definitions, etymology, synonyms)
    VALUES ('delete', old.rowid, old.word, old.definitions, old.etymology, old.synonyms);
END;

CREATE TRIGGER IF NOT EXISTS words_au AFTER UPDATE ON words BEGIN
    INSERT INTO words_fts(words_fts, rowid, word, definitions, etymology, synonyms)
    VALUES ('delete', old.rowid, old.word, old.definitions, old.etymology, old.synonyms);
    INSERT INTO words_fts(rowid, word, definitions, etymology, synonyms)
    VALUES (new.rowid, new.word, new.definitions, new.etymology, new.synonyms);
END;

-- Diacritic-insensitive index (for Vietnamese fallback)
CREATE INDEX IF NOT EXISTS idx_words_no_diacritics ON words(word_no_diacritics);

-- Language index
CREATE INDEX IF NOT EXISTS idx_words_language ON words(language);

-- Manifest table (tracks database version, checksums, word count)
CREATE TABLE IF NOT EXISTS manifest (
    key TEXT PRIMARY KEY,
    value TEXT
);

-- Delta sync tracking
CREATE TABLE IF NOT EXISTS sync_log (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    operation TEXT NOT NULL,  -- 'INSERT', 'UPDATE', 'DELETE'
    word_id TEXT NOT NULL,
    timestamp DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Integrity check function (call on cold start)
-- Per spec: "SQLite integrity_check run on app cold start"
-- Run: PRAGMA integrity_check;
