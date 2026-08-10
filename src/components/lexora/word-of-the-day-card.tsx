"use client";

import { Volume2, Leaf, ArrowRight } from "lucide-react";
import { useHaptics } from "@/hooks/use-haptics";
import { cn } from "@/lib/utils";
import type { WordOfTheDay } from "@/lib/lexora-data";

export function WordOfTheDayCard({
  word,
  className,
}: {
  word: WordOfTheDay;
  className?: string;
}) {
  const haptic = useHaptics();

  return (
    <article
      className={cn(
        "relative overflow-hidden rounded-2xl shadow-card border border-border bg-primary",
        className
      )}
    >
      {/* Watercolour background — fades into primary on the left for text legibility */}
      <img
        src="/illustrations/wotd-bg-light.png"
        alt=""
        aria-hidden
        className="absolute inset-0 w-full h-full object-cover dark:hidden fade-edges-l"
      />
      <img
        src="/illustrations/wotd-bg-dark.png"
        alt=""
        aria-hidden
        className="absolute inset-0 w-full h-full object-cover hidden dark:block fade-edges-l"
      />
      {/* Left-side color wash so text is readable on top of the image */}
      <div
        className="absolute inset-0 bg-primary dark:bg-background"
        style={{
          maskImage:
            "linear-gradient(to right, #000 0%, #000 38%, transparent 60%)",
          WebkitMaskImage:
            "linear-gradient(to right, #000 0%, #000 38%, transparent 60%)",
        }}
      />

      <div className="relative grid grid-cols-[42%_58%] min-h-[180px]">
        {/* Left panel: word info */}
        <div className="flex flex-col justify-center gap-1.5 p-5 min-w-0">
          <div className="flex items-center gap-1.5 text-highlight dark:text-accent">
            <Leaf className="w-3.5 h-3.5 shrink-0" strokeWidth={1.75} />
            <span className="font-body text-[10px] font-semibold tracking-[0.16em] uppercase">
              Word of the Day
            </span>
          </div>
          <div className="flex items-center gap-1.5 min-w-0">
            <h3 className="font-display text-[19px] font-semibold text-primary-foreground dark:text-foreground leading-tight break-words">
              {word.word}
            </h3>
            <button
              aria-label={`Listen to ${word.word}`}
              onClick={() => haptic("light")}
              className="shrink-0 w-7 h-7 grid place-items-center rounded-full text-primary-foreground/80 dark:text-foreground/80 active:scale-95 transition-transform"
            >
              <Volume2 className="w-4 h-4" strokeWidth={1.75} />
            </button>
          </div>
          <p className="font-body italic text-[11px] text-primary-foreground/70 dark:text-foreground/70 break-words">
            {word.phonetic}
          </p>
          <p className="font-body text-xs text-primary-foreground/80 dark:text-foreground/80">
            {word.partOfSpeech}
          </p>
        </div>

        {/* Right panel: definition */}
        <div className="relative flex flex-col justify-between gap-2 p-5 min-w-0">
          {/* Scrim so definition text stays legible over the watercolour.
              Heavier at top-left where the image is busiest, fading right. */}
          <div
            className="absolute inset-0 bg-gradient-to-br from-primary/85 via-primary/55 to-primary/25 dark:from-background/90 dark:via-background/60 dark:to-background/25"
            aria-hidden
          />
          <p
            className="relative font-long text-[13px] leading-relaxed text-primary-foreground dark:text-foreground"
            style={{ textShadow: "0 1px 3px rgba(0,0,0,0.45)" }}
          >
            {word.definition}
          </p>
          <button
            onClick={() => haptic("light")}
            className="relative self-end inline-flex items-center gap-1 text-xs font-body font-medium text-highlight dark:text-accent active:scale-95 transition-transform"
            style={{ textShadow: "0 1px 2px rgba(0,0,0,0.4)" }}
          >
            Explore more
            <ArrowRight className="w-3.5 h-3.5" strokeWidth={2} />
          </button>
        </div>
      </div>
    </article>
  );
}
