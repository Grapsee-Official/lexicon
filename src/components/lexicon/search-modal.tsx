"use client";

import { useEffect, useState, useRef, useMemo } from "react";
import { motion, AnimatePresence } from "framer-motion";
import { Search, X, Clock, ArrowRight, Mic, Camera, Trash2 } from "lucide-react";
import { useAppStore } from "@/lib/app-store";
import { useHaptics } from "@/hooks/use-haptics";
import { searchWords, type WordEntry } from "@/lib/lexicon-data";

export function SearchModal() {
  const open = useAppStore((s) => s.searchOpen);
  const closeSearch = useAppStore((s) => s.closeSearch);
  const openWord = useAppStore((s) => s.openWord);
  const recentSearches = useAppStore((s) => s.recentSearches);
  const addRecentSearch = useAppStore((s) => s.addRecentSearch);
  const clearRecentSearches = useAppStore((s) => s.clearRecentSearches);
  const haptic = useHaptics();

  const [query, setQuery] = useState("");
  const inputRef = useRef<HTMLInputElement>(null);

  // Live search via useMemo (no setState-in-effect lint violation)
  const results: WordEntry[] = useMemo(() => {
    if (!query.trim()) return [];
    return searchWords(query, 8);
  }, [query]);

  // Focus input when modal opens (no setState — just DOM focus)
  useEffect(() => {
    if (open) {
      const t = setTimeout(() => inputRef.current?.focus(), 250);
      return () => clearTimeout(t);
    }
  }, [open]);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!query.trim()) return;
    addRecentSearch(query.trim());
    haptic("light");
  };

  const selectWord = (w: WordEntry) => {
    haptic("medium");
    addRecentSearch(w.word);
    closeSearch();
    setQuery("");
    setTimeout(() => openWord(w), 200);
  };

  const handleClose = () => {
    haptic("light");
    setQuery("");
    closeSearch();
  };

  const handleRecentClick = (q: string) => {
    haptic("light");
    setQuery(q);
  };

  return (
    <AnimatePresence>
      {open && (
        <motion.div
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          exit={{ opacity: 0 }}
          transition={{ duration: 0.2 }}
          className="fixed inset-0 z-50 bg-background pt-safe"
          role="dialog"
          aria-modal="true"
          aria-label="Search dictionary"
        >
          <motion.div
            initial={{ y: 24, opacity: 0 }}
            animate={{ y: 0, opacity: 1 }}
            exit={{ y: 24, opacity: 0 }}
            transition={{ type: "spring", stiffness: 320, damping: 32 }}
            className="h-full flex flex-col"
          >
            {/* Search bar row */}
            <div className="px-4 py-3 paper-surface border-b border-border">
              <form
                onSubmit={handleSubmit}
                className="flex items-center gap-2"
              >
                <button
                  type="button"
                  aria-label="Close search"
                  onClick={handleClose}
                  className="shrink-0 w-9 h-9 grid place-items-center rounded-full text-foreground/70 active:scale-95 transition-transform"
                >
                  <X className="w-5 h-5" strokeWidth={2} />
                </button>
                <div className="flex-1 flex items-center gap-2.5 px-3.5 py-2 rounded-full bg-card border border-border">
                  <Search
                    className="w-4 h-4 text-foreground/50 shrink-0"
                    strokeWidth={1.75}
                  />
                  <input
                    ref={inputRef}
                    type="text"
                    inputMode="search"
                    value={query}
                    onChange={(e) => setQuery(e.target.value)}
                    placeholder="Search for a word..."
                    className="flex-1 bg-transparent outline-none text-sm font-body text-foreground placeholder:text-muted-foreground/70"
                  />
                  <button
                    type="button"
                    aria-label="Voice search"
                    onClick={() => haptic("light")}
                    className="shrink-0 w-7 h-7 grid place-items-center rounded-full text-foreground/50 active:scale-95 transition-transform"
                  >
                    <Mic className="w-4 h-4" strokeWidth={1.75} />
                  </button>
                  <button
                    type="button"
                    aria-label="Image search"
                    onClick={() => haptic("light")}
                    className="shrink-0 w-7 h-7 grid place-items-center rounded-full text-foreground/50 active:scale-95 transition-transform"
                  >
                    <Camera className="w-4 h-4" strokeWidth={1.75} />
                  </button>
                </div>
              </form>
            </div>

            {/* Content */}
            <div className="flex-1 overflow-y-auto px-5 py-4">
              {!query.trim() ? (
                <div className="flex flex-col gap-6">
                  {recentSearches.length > 0 && (
                    <section>
                      <div className="flex items-center justify-between mb-3">
                        <h3 className="font-body text-[11px] font-semibold tracking-[0.14em] uppercase text-muted-foreground">
                          Recent
                        </h3>
                        <button
                          onClick={() => {
                            haptic("light");
                            clearRecentSearches();
                          }}
                          className="inline-flex items-center gap-1 text-[11px] font-body text-foreground/60 active:scale-95 transition-transform"
                        >
                          <Trash2 className="w-3 h-3" strokeWidth={1.75} />
                          Clear
                        </button>
                      </div>
                      <ul className="space-y-1">
                        {recentSearches.map((r) => (
                          <li key={r}>
                            <button
                              onClick={() => handleRecentClick(r)}
                              className="w-full flex items-center gap-3 py-2.5 text-left active:scale-[0.99] transition-transform"
                            >
                              <Clock
                                className="w-4 h-4 text-muted-foreground shrink-0"
                                strokeWidth={1.75}
                              />
                              <span className="flex-1 font-body text-sm text-foreground/80 truncate">
                                {r}
                              </span>
                              <ArrowRight
                                className="w-4 h-4 text-muted-foreground shrink-0"
                                strokeWidth={1.75}
                              />
                            </button>
                          </li>
                        ))}
                      </ul>
                    </section>
                  )}

                  <section>
                    <h3 className="font-body text-[11px] font-semibold tracking-[0.14em] uppercase text-muted-foreground mb-3">
                      Suggested
                    </h3>
                    <div className="flex flex-wrap gap-2">
                      {[
                        "Petrichor",
                        "Saudade",
                        "Komorebi",
                        "Mellifluous",
                        "Halcyon",
                        "Numinous",
                      ].map((s) => (
                        <button
                          key={s}
                          onClick={() => {
                            haptic("light");
                            setQuery(s);
                          }}
                          className="px-3.5 py-1.5 rounded-full bg-card border border-border text-xs font-body text-foreground/80 active:scale-95 transition-transform"
                        >
                          {s}
                        </button>
                      ))}
                    </div>
                  </section>
                </div>
              ) : results.length > 0 ? (
                <ul className="space-y-1">
                  {results.map((w) => (
                    <li key={w.id}>
                      <button
                        onClick={() => selectWord(w)}
                        className="w-full flex items-baseline gap-3 py-3 px-2 text-left rounded-lg active:bg-card/60 transition-colors"
                      >
                        <span className="font-display text-base font-semibold text-foreground shrink-0">
                          {w.word}
                        </span>
                        <span className="font-body italic text-xs text-muted-foreground shrink-0">
                          {w.partOfSpeech}
                        </span>
                        <span className="flex-1 font-long text-[13px] text-foreground/70 line-clamp-1">
                          {w.definitions[0]}
                        </span>
                      </button>
                    </li>
                  ))}
                </ul>
              ) : (
                <div className="flex flex-col items-center justify-center pt-16 text-center">
                  <div className="w-16 h-16 rounded-full bg-card border border-border grid place-items-center mb-4">
                    <Search
                      className="w-7 h-7 text-muted-foreground"
                      strokeWidth={1.5}
                    />
                  </div>
                  <p className="font-display text-lg text-foreground mb-1">
                    No words found
                  </p>
                  <p className="font-body text-xs text-muted-foreground max-w-[28ch]">
                    We couldn&apos;t find &ldquo;{query}&rdquo; in this offline
                    pack. Try another spelling or download more language pairs.
                  </p>
                </div>
              )}
            </div>
          </motion.div>
        </motion.div>
      )}
    </AnimatePresence>
  );
}
