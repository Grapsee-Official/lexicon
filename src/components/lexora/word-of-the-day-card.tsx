"use client";

import { Volume2, Leaf, ArrowRight } from "lucide-react";
import { useHaptics } from "@/hooks/use-haptics";
import { cn } from "@/lib/utils";
import { useAppStore } from "@/lib/app-store";
import type { WordEntry } from "@/lib/lexora-data";

/**
 * WordOfTheDayCard — Pure Tailwind. No CSS file. No !important.
 *
 * STRICT 2-COLUMN SPLIT:
 *   LEFT (40%):  solid bg-[#677554], white/cream text (word info)
 *   RIGHT (60%): solid bg-[#FAF3E9], text-[#677554] (full definition + Explore more)
 *
 * Right panel:
 *   - Card container: overflow-hidden rounded-2xl
 *   - Image container: absolute top-0 right-0 h-full w-[45%] z-0
 *   - Image: h-full w-full object-cover object-right (fills full height, no checkered bg)
 *   - Image mask: linear-gradient(to right, transparent 0%, black 35%) — left edge only
 *   - Text container: relative z-10 w-[60%] flex flex-col justify-center h-full p-6
 *   - Definition: NO line-clamp, full text renders
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
        "grid cursor-pointer overflow-hidden rounded-2xl border border-border shadow-card grid-cols-[40%_60%] transition-transform active:scale-[0.99]",
        className
      )}
    >
      {/* ============================================================
          LEFT PANEL (40%) — solid #677554, white/cream text
          ============================================================ */}
      <div className="flex flex-col justify-center gap-1 bg-[#677554] p-4">
        <div className="flex items-center gap-1 text-[#FBF4EA]/80">
          <Leaf className="h-3 w-3 shrink-0" strokeWidth={1.5} />
          <span className="font-body text-[9px] font-semibold uppercase tracking-[0.14em]">
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
          Image fills full height (object-cover object-right).
          Text container z-10 on left 60%, over the faded image edge.
          ============================================================ */}
      <div className="relative overflow-hidden bg-[#FAF3E9]">
        {/* Image container — absolute top-0 right-0 h-full w-[45%] z-0.
            object-cover object-right stretches image top-to-bottom
            without exposing transparent checkered backgrounds. */}
        <div className="absolute right-0 top-0 z-0 h-full w-[45%]">
          <img
            src="/illustrations/wotd-bg-light.png"
            alt=""
            aria-hidden
            className="h-full w-full object-cover object-right dark:hidden"
            style={{
              // Soft mask fade on LEFT edge only — blends into #FAF3E9.
              // Top/bottom/right stay crisp against card borders.
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

        {/* Text container — relative z-10 w-[60%] flex flex-col justify-center h-full p-6.
            Sits over the faded left region of the image. */}
        <div className="relative z-10 flex h-full w-[60%] flex-col justify-center gap-3 p-6">
          {/* NO line-clamp — full definition renders without truncation */}
          <p className="font-long text-[12px] leading-relaxed text-[#677554]">
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
