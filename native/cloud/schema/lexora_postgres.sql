-- Lexora PostgreSQL Cloud Schema
-- Per spec: "PostgreSQL (Confirmed)" for cloud backend.
-- Per spec: "Do batch insert processed words into PostgreSQL using COPY command."

-- Enable required extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";  -- For trigram fuzzy search

-- Main words table
CREATE TABLE IF NOT EXISTS words (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    word TEXT NOT NULL,
    phonetic TEXT,
    part_of_speech TEXT,
    definitions JSONB NOT NULL DEFAULT '[]',
    etymology TEXT,
    synonyms JSONB NOT NULL DEFAULT '[]',
    antonyms JSONB NOT NULL DEFAULT '[]',
    examples JSONB NOT NULL DEFAULT '[]',
    language TEXT NOT NULL,
    word_nfc TEXT NOT NULL,
    word_no_diacritics TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(word, language, part_of_speech)
);

-- Indexes for fast lookup
CREATE INDEX IF NOT EXISTS idx_words_word ON words(word);
CREATE INDEX IF NOT EXISTS idx_words_nfc ON words(word_nfc);
CREATE INDEX IF NOT EXISTS idx_words_language ON words(language);
CREATE INDEX IF NOT EXISTS idx_words_no_diacritics ON words(word_no_diacritics);

-- Trigram index for fuzzy search (PostgreSQL-side fallback)
CREATE INDEX IF NOT EXISTS idx_words_trgm ON words USING gin(word gin_trgm_ops);

-- Updated_at trigger
CREATE OR REPLACE FUNCTION update_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER words_updated_at
    BEFORE UPDATE ON words
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at();

-- Language packs table (tracks which .db.zst files are available)
CREATE TABLE IF NOT EXISTS language_packs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    language_code TEXT NOT NULL,        -- ISO 639-1 (e.g., 'en', 'vi', 'fr')
    language_name TEXT NOT NULL,
    version INTEGER NOT NULL DEFAULT 1,
    word_count INTEGER NOT NULL,
    file_checksum TEXT NOT NULL,         -- SHA-256
    file_size_bytes BIGINT NOT NULL,
    download_url TEXT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(language_code, version)
);

-- User sync tracking (for delta sync)
CREATE TABLE IF NOT EXISTS user_sync_state (
    user_id UUID NOT NULL,
    language_code TEXT NOT NULL,
    last_synced_version INTEGER DEFAULT 0,
    last_synced_at TIMESTAMPTZ,
    PRIMARY KEY(user_id, language_code)
);

-- Sync log (tracks changes for delta sync)
CREATE TABLE IF NOT EXISTS sync_log (
    id BIGSERIAL PRIMARY KEY,
    operation TEXT NOT NULL CHECK (operation IN ('INSERT', 'UPDATE', 'DELETE')),
    word_id UUID NOT NULL,
    language_code TEXT NOT NULL,
    timestamp TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_sync_log_language_version
    ON sync_log(language_code, id)
    WHERE operation IN ('INSERT', 'UPDATE', 'DELETE');

-- Audit table for FTS5 corruption recovery
-- Per spec: "Zero database corruption errors across 10,000 simulated sync interruptions"
CREATE TABLE IF NOT EXISTS integrity_log (
    id BIGSERIAL PRIMARY KEY,
    user_id UUID,
    check_type TEXT NOT NULL,  -- 'integrity_check', 'quick_check', 'foreign_key_check'
    result TEXT NOT NULL,      -- 'ok', 'corrupt', 'repaired'
    details TEXT,
    checked_at TIMESTAMPTZ DEFAULT NOW()
);
