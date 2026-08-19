"use client";

import { Volume2, Leaf, ArrowRight } from "lucide-react";
import { useHaptics } from "@/hooks/use-haptics";
import { cn } from "@/lib/utils";
import { useAppStore } from "@/lib/app-store";
import type { WordEntry } from "@/lib/lexora-data";

/**
 * WordOfTheDayCard — Pure Tailwind. No CSS file. No !important.
 *
 * STRICT 2-COLUMN SPLIT (40/60):
 *   LEFT:  solid bg-[#677554], white/cream text. "WORD OF THE DAY" on ONE line.
 *   RIGHT: solid bg-[#FAF3E9], text-[#677554]. Wider text area (w-[70%]).
 *
 * Card height capped at h-[160px]. Compact py-3.5 px-4 padding.
 * Image fills the top-right corner completely (object-cover object-top),
 * inside absolute right-0 top-0 h-full w-[40%] z-0 — no checkered gaps.
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
        "grid h-[140px] cursor-pointer overflow-hidden rounded-2xl border border-border shadow-card grid-cols-[40%_60%] transition-transform active:scale-[0.99]",
        className
      )}
    >
      {/* ============================================================
          LEFT PANEL (40%) — solid #677554, white/cream text.
          "WORD OF THE DAY" uses whitespace-nowrap to stay on ONE line.
          ============================================================ */}
      <div className="flex flex-col justify-center gap-2 bg-[#677554] px-3 py-3.5">
        <div className="flex items-center gap-1 text-[#FBF4EA]/80">
          <Leaf className="h-2.5 w-2.5 shrink-0" strokeWidth={1.5} />
          {/* whitespace-nowrap + tighter tracking: prevents "WORD OF THE DAY" from breaking */}
          <span className="whitespace-nowrap font-body text-[8.5px] font-semibold uppercase tracking-[0.08em]">
            Word of the Day
          </span>
        </div>
        <div className="flex min-w-0 items-center gap-1">
          <h3 className="break-words font-display text-[17px] font-semibold leading-tight text-white">
            {word.word}
          </h3>
          <button
            aria-label={`Listen to ${word.word}`}
            onClick={(e) => {
              e.stopPropagation();
              haptic("light");
            }}
            className="grid h-6 w-6 shrink-0 place-items-center rounded-full text-white/70 transition-transform active:scale-95"
          >
            <Volume2 className="h-3.5 w-3.5" strokeWidth={1.5} />
          </button>
        </div>
        <p className="break-words font-body text-[10px] italic text-white/60">
          {word.phonetic}
        </p>
        <p className="font-body text-[11px] lowercase text-white/70">
          {word.partOfSpeech}
        </p>
      </div>

      {/* ============================================================
          RIGHT PANEL (60%) — relative overflow-hidden bg-[#FAF3E9].
          Image fills top-right corner completely (no checkered gaps).
          Text container widened to w-[70%] for comfortable 3-line wrap.
          ============================================================ */}
      <div className="relative overflow-hidden bg-[#FAF3E9]">
        {/* Image container — absolute top-0 bottom-0 right-0 w-[45%] h-full z-0.
            Pinned to all three edges (top, bottom, right) so it fills the
            complete right edge with zero checkered gaps. */}
        <div className="absolute right-0 top-0 bottom-0 z-0 h-full w-[45%] overflow-hidden">
          <img
            src="/illustrations/wotd-bg-light.png"
            alt=""
            aria-hidden
            className="h-full w-full object-cover object-right dark:hidden"
            style={{
              // Soft mask fade on LEFT edge only — blends into #FAF3E9.
              WebkitMaskImage:
                "linear-gradient(to right, transparent 0%, black 35%)",
              maskImage:
                "linear-gradient(to right, transparent 0%, black 35%)",
            }}
          />
          <img
            src="/illustrations/wotd-bg-dark.png"
            alt=""
            aria-hidden
            className="hidden h-full w-full object-cover object-right dark:block"
            style={{
              WebkitMaskImage:
                "linear-gradient(to right, transparent 0%, black 35%)",
              maskImage:
                "linear-gradient(to right, transparent 0%, black 35%)",
            }}
          />
        </div>

        {/* Text container — relative z-10 w-[70%] flex flex-col justify-center h-full.
            Widened from 60% to 70% so definition sits on 3 wider lines. */}
        <div className="relative z-10 flex h-full w-[70%] flex-col justify-center gap-2 px-4 py-3.5">
          {/* NO line-clamp — full definition renders. text-sm for wider lines. */}
          <p className="font-long text-[13px] leading-snug text-[#677554]">
            {word.definitions[0]}
          </p>
          <span className="inline-flex items-center gap-1 self-end font-body text-[11px] font-medium text-[#677554]">
            Explore more
            <ArrowRight className="h-3 w-3" strokeWidth={2} />
          </span>
        </div>
      </div>
    </article>
  );
}
