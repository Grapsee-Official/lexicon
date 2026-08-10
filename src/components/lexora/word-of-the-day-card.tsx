"use client";

import { Volume2, Leaf, ArrowRight } from "lucide-react";
import { useHaptics } from "@/hooks/use-haptics";
import { cn } from "@/lib/utils";
import { useAppStore } from "@/lib/app-store";
import type { WordEntry } from "@/lib/lexora-data";

export function WordOfTheDayCard({
  word,
  className,
}: {
  word: WordEntry;
  className?: string;
}) {
  const haptic = useHaptics();
  const openWord = useAppStore((s) => s.openWord);

  return (
    <article
      onClick={() => {
        haptic("medium");
        openWord(word);
      }}
      className={cn(
        "relative overflow-hidden rounded-2xl shadow-card border border-border cursor-pointer active:scale-[0.99] transition-transform flex",
        className
      )}
    >
      {/* Left panel: solid sage green with word info */}
      <div className="relative w-[45%] bg-primary dark:bg-background p-4 flex flex-col justify-center gap-1 min-w-0 shrink-0 z-10">
        <div className="flex items-center gap-1 text-highlight dark:text-accent">
          <Leaf className="w-3 h-3 shrink-0" strokeWidth={1.75} />
          <span className="font-body text-[9px] font-semibold tracking-[0.14em] uppercase">
            Word of the Day
          </span>
        </div>
        <div className="flex items-center gap-1 min-w-0">
          <h3 className="font-display text-[17px] font-semibold text-primary-foreground dark:text-foreground leading-tight break-words">
            {word.word}
          </h3>
          <button
            aria-label={`Listen to ${word.word}`}
            onClick={(e) => {
              e.stopPropagation();
              haptic("light");
            }}
            className="shrink-0 w-6 h-6 grid place-items-center rounded-full text-primary-foreground/70 dark:text-foreground/70 active:scale-95 transition-transform"
          >
            <Volume2 className="w-3.5 h-3.5" strokeWidth={1.75} />
          </button>
        </div>
        <p className="font-body italic text-[10px] text-primary-foreground/60 dark:text-foreground/60 break-words">
          {word.phonetic}
        </p>
        <p className="font-body text-[11px] text-primary-foreground/70 dark:text-foreground/70 lowercase">
          {word.partOfSpeech}
        </p>
      </div>

      {/* Right panel: watercolour image with definition overlay */}
      <div className="relative w-[55%] min-w-0">
        <img
          src="/illustrations/wotd-bg-light.png"
          alt=""
          aria-hidden
          className="absolute inset-0 w-full h-full object-cover dark:hidden"
        />
        <img
          src="/illustrations/wotd-bg-dark.png"
          alt=""
          aria-hidden
          className="absolute inset-0 w-full h-full object-cover hidden dark:block"
        />
        {/* Scrim so definition text stays legible over the watercolour */}
        <div
          className="absolute inset-0 bg-gradient-to-br from-primary/70 via-primary/35 to-primary/10 dark:from-background/80 dark:via-background/45 dark:to-background/15"
          aria-hidden
        />
        <div className="relative h-full flex flex-col justify-between gap-1.5 p-4 min-h-[140px]">
          <p
            className="font-long text-[12px] leading-relaxed text-primary-foreground dark:text-foreground line-clamp-3"
            style={{ textShadow: "0 1px 3px rgba(0,0,0,0.45)" }}
          >
            {word.definitions[0]}
          </p>
          <span
            className="self-end inline-flex items-center gap-1 text-[11px] font-body font-medium text-highlight dark:text-accent"
            style={{ textShadow: "0 1px 2px rgba(0,0,0,0.4)" }}
          >
            Explore more
            <ArrowRight className="w-3 h-3" strokeWidth={2} />
          </span>
        </div>
      </div>
    </article>
  );
}
