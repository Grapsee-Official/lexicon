"use client";

import { motion } from "framer-motion";
import { Bookmark, Trash2, Volume2, Sparkles } from "lucide-react";
import { useAppStore } from "@/lib/app-store";
import { useHaptics } from "@/hooks/use-haptics";
import { dictionary } from "@/lib/lexicon-data";
import { cn } from "@/lib/utils";

export function CollectionsScreen() {
  const savedIds = useAppStore((s) => s.savedWordIds);
  const toggleSave = useAppStore((s) => s.toggleSave);
  const openWord = useAppStore((s) => s.openWord);
  const haptic = useHaptics();

  const savedWords = dictionary.filter(
    (w, i, arr) => savedIds.includes(w.id) && arr.findIndex((x) => x.id === w.id) === i
  );

  if (savedWords.length === 0) {
    return (
      <motion.div
        initial={{ opacity: 0, y: 12 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.3 }}
        className="flex flex-col items-center justify-center text-center pt-16 px-8"
      >
        {/* Watercolour empty state */}
        <div className="relative w-40 h-40 mb-6">
          <div className="absolute inset-0 rounded-full bg-primary/10 dark:bg-accent/10 blur-2xl" />
          <img
            src="/illustrations/cat-literature.png"
            alt=""
            aria-hidden
            className="relative w-full h-full object-cover rounded-2xl fade-edges opacity-80 dark:opacity-60"
          />
          <div className="absolute -bottom-2 -right-2 w-12 h-12 rounded-full bg-card border border-border grid place-items-center shadow-card">
            <Bookmark
              className="w-5 h-5 text-highlight dark:text-accent"
              strokeWidth={1.75}
            />
          </div>
        </div>

        <h2 className="font-display text-xl font-semibold text-foreground mb-2">
          Your collection awaits
        </h2>
        <p className="font-body text-sm text-muted-foreground max-w-[32ch] leading-relaxed">
          Tap the bookmark icon on any word to save it here. Build your own
          private herbarium of language.
        </p>

        <div className="mt-8 flex items-center gap-1.5 px-3 py-1.5 rounded-full bg-card border border-border">
          <Sparkles
            className="w-3.5 h-3.5 text-highlight dark:text-accent"
            strokeWidth={1.75}
          />
          <span className="font-body text-[11px] text-muted-foreground">
            Saved words sync offline across all your devices
          </span>
        </div>
      </motion.div>
    );
  }

  return (
    <div className="flex flex-col gap-3">
      <div className="flex items-baseline justify-between px-1">
        <h2 className="font-display text-2xl font-semibold text-foreground">
          Saved Words
        </h2>
        <span className="font-body text-[11px] text-muted-foreground">
          {savedWords.length} {savedWords.length === 1 ? "word" : "words"}
        </span>
      </div>

      <ul className="bg-card border border-border rounded-2xl overflow-hidden shadow-card">
        {savedWords.map((entry, idx) => (
          <motion.li
            key={entry.id}
            initial={{ opacity: 0, x: -8 }}
            animate={{ opacity: 1, x: 0 }}
            transition={{ delay: idx * 0.04, duration: 0.25 }}
          >
            <button
              onClick={() => {
                haptic("light");
                openWord(entry);
              }}
              className={cn(
                "w-full flex items-center gap-3 px-4 py-3 text-left active:bg-background/50 transition-colors",
                idx !== 0 && "border-t border-border"
              )}
            >
              <Bookmark
                className="w-4 h-4 text-highlight dark:text-accent shrink-0"
                strokeWidth={1.75}
                fill="currentColor"
              />
              <div className="flex-1 min-w-0">
                <div className="flex items-baseline gap-2 mb-0.5">
                  <h4 className="font-display text-base font-semibold text-foreground truncate">
                    {entry.word}
                  </h4>
                  <span className="font-body italic text-[11px] text-muted-foreground lowercase shrink-0">
                    {entry.partOfSpeech}
                  </span>
                </div>
                <p className="font-long text-[13px] text-foreground/70 line-clamp-1 leading-snug">
                  {entry.definitions[0]}
                </p>
              </div>
              <Volume2
                className="w-4 h-4 text-muted-foreground shrink-0"
                strokeWidth={1.75}
              />
              <button
                aria-label={`Remove ${entry.word}`}
                onClick={(e) => {
                  e.stopPropagation();
                  haptic("light");
                  toggleSave(entry.id);
                }}
                className="shrink-0 w-7 h-7 grid place-items-center rounded-full text-muted-foreground/60 active:scale-95 transition-transform"
              >
                <Trash2 className="w-4 h-4" strokeWidth={1.75} />
              </button>
            </button>
          </motion.li>
        ))}
      </ul>
    </div>
  );
}
