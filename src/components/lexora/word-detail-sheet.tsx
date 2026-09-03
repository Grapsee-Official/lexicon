"use client";

import {
  Drawer,
  DrawerContent,
  DrawerHeader,
  DrawerTitle,
  DrawerDescription,
} from "@/components/ui/drawer";
import { useAppStore } from "@/lib/app-store";
import { useHaptics } from "@/hooks/use-haptics";
import {
  Volume2,
  Bookmark,
  Share2,
  Sparkles,
  BookOpen,
  Quote,
  Repeat,
  Ban,
  Languages,
} from "lucide-react";
import { cn } from "@/lib/utils";

export function WordDetailSheet() {
  const word = useAppStore((s) => s.selectedWord);
  const closeWord = useAppStore((s) => s.closeWord);
  const isSaved = useAppStore((s) => s.isSaved);
  const toggleSave = useAppStore((s) => s.toggleSave);
  const haptic = useHaptics();

  const open = word !== null;
  const saved = word ? isSaved(word.id) : false;

  return (
    <Drawer open={open} onOpenChange={(o) => !o && closeWord()}>
      <DrawerContent className="max-h-[88dvh] bg-background border-border">
        <DrawerHeader className="pb-2">
          <DrawerTitle className="sr-only">
            {word ? word.word : "Word details"}
          </DrawerTitle>
          <DrawerDescription className="sr-only">
            Full dictionary entry with definitions, etymology, synonyms, and
            examples.
          </DrawerDescription>
        </DrawerHeader>

        {word && (
          <div className="overflow-y-auto pb-safe px-5 max-h-[80dvh]">
            {/* Header: word + phonetic + actions */}
            <div className="flex items-start justify-between gap-3 mb-4">
              <div className="min-w-0 flex-1">
                <div className="flex items-center gap-1.5 mb-1 text-highlight dark:text-accent">
                  <Sparkles className="w-3.5 h-3.5" strokeWidth={1.75} />
                  <span className="font-body text-[10px] font-semibold tracking-[0.16em] uppercase">
                    Lexora Entry
                  </span>
                </div>
                <h2 className="font-display text-3xl font-semibold text-foreground leading-tight break-words">
                  {word.word}
                </h2>
                <p className="font-body italic text-sm text-muted-foreground mt-1">
                  {word.phonetic}{" "}
                  <span className="not-italic text-foreground/40 mx-1">·</span>{" "}
                  <span className="not-italic lowercase">{word.partOfSpeech}</span>
                </p>
              </div>
              <div className="flex items-center gap-1 shrink-0">
                <button
                  aria-label="Pronounce"
                  onClick={() => haptic("light")}
                  className="w-10 h-10 grid place-items-center rounded-full bg-card border border-border text-foreground/80 active:scale-95 transition-transform"
                >
                  <Volume2 className="w-[18px] h-[18px]" strokeWidth={1.75} />
                </button>
                <button
                  aria-label={saved ? "Remove from saved" : "Save word"}
                  onClick={() => {
                    haptic("medium");
                    toggleSave(word.id);
                  }}
                  className={cn(
                    "w-10 h-10 grid place-items-center rounded-full border active:scale-95 transition-transform",
                    saved
                      ? "bg-accent/15 border-accent/40 text-accent"
                      : "bg-card border-border text-foreground/80"
                  )}
                >
                  <Bookmark
                    className="w-[18px] h-[18px]"
                    strokeWidth={1.75}
                    fill={saved ? "currentColor" : "none"}
                  />
                </button>
                <button
                  aria-label="Share"
                  onClick={() => haptic("light")}
                  className="w-10 h-10 grid place-items-center rounded-full bg-card border border-border text-foreground/80 active:scale-95 transition-transform"
                >
                  <Share2 className="w-[18px] h-[18px]" strokeWidth={1.75} />
                </button>
              </div>
            </div>

            {/* Definitions */}
            <section className="mb-5">
              <div className="flex items-center gap-2 mb-2 text-foreground/60">
                <BookOpen className="w-3.5 h-3.5" strokeWidth={1.75} />
                <h3 className="font-body text-[11px] font-semibold tracking-[0.14em] uppercase">
                  Definition{word.definitions.length > 1 ? "s" : ""}
                </h3>
              </div>
              <ol className="space-y-2.5">
                {word.definitions.map((d, i) => (
                  <li
                    key={i}
                    className="flex gap-3 font-long text-[15px] leading-relaxed text-foreground/90"
                  >
                    <span className="shrink-0 font-body text-xs text-highlight dark:text-accent mt-1 w-4">
                      {i + 1}.
                    </span>
                    <span>{d}</span>
                  </li>
                ))}
              </ol>
            </section>

            {/* Etymology */}
            {word.etymology && (
              <section className="mb-5 p-4 rounded-xl bg-card border border-border">
                <div className="flex items-center gap-2 mb-2 text-foreground/60">
                  <Sparkles className="w-3.5 h-3.5" strokeWidth={1.75} />
                  <h3 className="font-body text-[11px] font-semibold tracking-[0.14em] uppercase">
                    Etymology
                  </h3>
                </div>
                <p className="font-long text-[13px] leading-relaxed text-foreground/80">
                  {word.etymology}
                </p>
              </section>
            )}

            {/* Language origin */}
            {word.language && (
              <section className="mb-5 flex items-center gap-2.5 text-sm">
                <Languages
                  className="w-4 h-4 text-highlight dark:text-accent shrink-0"
                  strokeWidth={1.75}
                />
                <span className="font-body text-muted-foreground">Origin:</span>
                <span className="font-body text-foreground/90">
                  {word.language}
                </span>
              </section>
            )}

            {/* Synonyms */}
            {word.synonyms.length > 0 && (
              <section className="mb-5">
                <div className="flex items-center gap-2 mb-2 text-foreground/60">
                  <Repeat className="w-3.5 h-3.5" strokeWidth={1.75} />
                  <h3 className="font-body text-[11px] font-semibold tracking-[0.14em] uppercase">
                    Synonyms
                  </h3>
                </div>
                <div className="flex flex-wrap gap-2">
                  {word.synonyms.map((s) => (
                    <span
                      key={s}
                      className="px-3 py-1.5 rounded-full bg-secondary/15 border border-secondary/30 text-xs font-body text-foreground/80"
                    >
                      {s}
                    </span>
                  ))}
                </div>
              </section>
            )}

            {/* Antonyms */}
            {word.antonyms.length > 0 && (
              <section className="mb-5">
                <div className="flex items-center gap-2 mb-2 text-foreground/60">
                  <Ban className="w-3.5 h-3.5" strokeWidth={1.75} />
                  <h3 className="font-body text-[11px] font-semibold tracking-[0.14em] uppercase">
                    Antonyms
                  </h3>
                </div>
                <div className="flex flex-wrap gap-2">
                  {word.antonyms.map((s) => (
                    <span
                      key={s}
                      className="px-3 py-1.5 rounded-full bg-destructive/10 border border-destructive/20 text-xs font-body text-foreground/70"
                    >
                      {s}
                    </span>
                  ))}
                </div>
              </section>
            )}

            {/* Examples */}
            {word.examples.length > 0 && (
              <section className="mb-2">
                <div className="flex items-center gap-2 mb-2 text-foreground/60">
                  <Quote className="w-3.5 h-3.5" strokeWidth={1.75} />
                  <h3 className="font-body text-[11px] font-semibold tracking-[0.14em] uppercase">
                    In Context
                  </h3>
                </div>
                <div className="space-y-2.5">
                  {word.examples.map((ex, i) => (
                    <blockquote
                      key={i}
                      className="relative pl-4 pr-2 py-1 font-long text-[14px] italic leading-relaxed text-foreground/80 border-l-2 border-highlight/60 dark:border-accent/60"
                    >
                      {ex}
                    </blockquote>
                  ))}
                </div>
              </section>
            )}

            <div className="h-6" />
          </div>
        )}
      </DrawerContent>
    </Drawer>
  );
}
