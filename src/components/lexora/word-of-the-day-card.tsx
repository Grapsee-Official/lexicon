"use client";

import { Volume2, Leaf, ArrowRight } from "lucide-react";
import { useHaptics } from "@/hooks/use-haptics";
import { cn } from "@/lib/utils";
import { useAppStore } from "@/lib/app-store";
import type { WordEntry } from "@/lib/lexora-data";

/**
 * WordOfTheDayCard — Pure Tailwind. No CSS file. No !important.
 * Smooth gradient fade between solid left panel and right image (no hard line).
 * Light overlay on right side ensures dark text is readable over the image.
 */
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
        "relative flex cursor-pointer overflow-hidden rounded-2xl border border-border shadow-card transition-transform active:scale-[0.99]",
        className
      )}
    >
      {/* Background image spans the FULL card (both panels).
          This eliminates the hard vertical split line — the image is one
          continuous layer behind everything. */}
      <img
        src="/illustrations/wotd-bg-light.png"
        alt=""
        aria-hidden
        className="absolute inset-0 h-full w-full object-cover dark:hidden"
      />
      <img
        src="/illustrations/wotd-bg-dark.png"
        alt=""
        aria-hidden
        className="absolute inset-0 hidden h-full w-full object-cover dark:block"
      />

      {/* WIDE gradient overlay — fades from solid #677554 on the left to
          transparent on the right. Smooth transition, no hard split. */}
      <div
        className="absolute inset-0 bg-gradient-to-r from-[#677554] via-[#677554]/85 to-[#677554]/30 dark:from-background dark:via-background/80 dark:to-background/20"
        aria-hidden
      />
      {/* Secondary soft overlay on the right side so dark text stays
          readable over the artwork. */}
      <div
        className="absolute inset-0 bg-gradient-to-br from-transparent via-[#677554]/20 to-[#677554]/40 dark:from-transparent dark:via-background/20 dark:to-background/40"
        aria-hidden
      />

      {/* Content layer — 2-column grid over the gradient background */}
      <div className="relative grid min-h-[150px] w-full grid-cols-[45%_55%]">
        {/* LEFT panel: word info (text readable on solid primary gradient) */}
        <div className="flex flex-col justify-center gap-1 p-4">
          <div className="flex items-center gap-1 text-highlight dark:text-accent">
            <Leaf className="h-3 w-3 shrink-0" strokeWidth={1.5} />
            <span className="font-body text-[9px] font-semibold uppercase tracking-[0.14em]">
              Word of the Day
            </span>
          </div>
          <div className="flex min-w-0 items-center gap-1">
            <h3 className="break-words font-display text-[17px] font-semibold leading-tight text-primary-foreground dark:text-foreground">
              {word.word}
            </h3>
            <button
              aria-label={`Listen to ${word.word}`}
              onClick={(e) => {
                e.stopPropagation();
                haptic("light");
              }}
              className="grid h-6 w-6 shrink-0 place-items-center rounded-full text-primary-foreground/70 transition-transform active:scale-95 dark:text-foreground/70"
            >
              <Volume2 className="h-3.5 w-3.5" strokeWidth={1.5} />
            </button>
          </div>
          <p className="break-words font-body text-[10px] italic text-primary-foreground/60 dark:text-foreground/60">
            {word.phonetic}
          </p>
          <p className="font-body text-[11px] lowercase text-primary-foreground/70 dark:text-foreground/70">
            {word.partOfSpeech}
          </p>
        </div>

        {/* RIGHT panel: definition text over the artwork.
            Dark overlay ensures text contrast. */}
        <div className="flex min-w-0 flex-col justify-between gap-2 p-4">
          <p
            className="line-clamp-3 font-long text-[12px] leading-relaxed text-white"
            style={{ textShadow: "0 1px 4px rgba(0,0,0,0.7)" }}
          >
            {word.definitions[0]}
          </p>
          <span
            className="inline-flex items-center gap-1 self-end font-body text-[11px] font-medium text-highlight dark:text-accent"
            style={{ textShadow: "0 1px 3px rgba(0,0,0,0.6)" }}
          >
            Explore more
            <ArrowRight className="h-3 w-3" strokeWidth={2} />
          </span>
        </div>
      </div>
    </article>
  );
}
