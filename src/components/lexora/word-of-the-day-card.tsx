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
 *   RIGHT (60%): solid bg-[#FAF3E9], text-[#677554] (definition + Explore more)
 *
 * Right panel internal layout:
 *   - relative overflow-hidden container
 *   - Text wrapper: w-[60%] relative z-10 flex flex-col justify-center h-full px-6
 *     (holds BOTH paragraph + Explore more, sits on solid #FAF3E9)
 *   - Image: absolute top-0 right-0 w-[50%] h-full z-0, object-cover
 *   - Image mask: linear-gradient(to right, transparent 0%, black 40%)
 *     (only LEFT edge fades; top/bottom/right stay flush)
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
          RIGHT PANEL (60%) — relative overflow-hidden.
          Text wrapper (z-10) on solid #FAF3E9.
          Image (z-0) absolute top-0 right-0 w-[50%] h-full, masked.
          ============================================================ */}
      <div className="relative overflow-hidden bg-[#FAF3E9]">
        {/* Text wrapper — sits on solid #FAF3E9, never over image.
            w-[60%] keeps it clear of the image's w-[50%] on the right. */}
        <div className="relative z-10 flex h-full w-[60%] flex-col justify-center gap-3 px-6">
          <p className="line-clamp-3 font-long text-[12px] leading-relaxed text-[#677554]">
            {word.definitions[0]}
          </p>
          <span className="inline-flex items-center gap-1 self-end font-body text-[11px] font-medium text-[#677554]">
            Explore more
            <ArrowRight className="h-3 w-3" strokeWidth={2} />
          </span>
        </div>

        {/* Decorative image — absolute top-0 right-0, w-[50%] h-full.
            object-cover so it fills top-to-bottom with no harsh cuts.
            Mask: only LEFT edge fades into #FAF3E9. Top/bottom/right flush. */}
        <img
          src="/illustrations/wotd-bg-light.png"
          alt=""
          aria-hidden
          className="pointer-events-none absolute right-0 top-0 z-0 h-full w-[50%] object-cover dark:hidden"
          style={{
            // Only the LEFT edge fades. Top/bottom/right stay crisp.
            WebkitMaskImage:
              "linear-gradient(to right, transparent 0%, black 40%)",
            maskImage:
              "linear-gradient(to right, transparent 0%, black 40%)",
          }}
        />
        <img
          src="/illustrations/wotd-bg-dark.png"
          alt=""
          aria-hidden
          className="pointer-events-none absolute right-0 top-0 z-0 hidden h-full w-[50%] object-cover dark:block"
          style={{
            WebkitMaskImage:
              "linear-gradient(to right, transparent 0%, black 40%)",
            maskImage:
              "linear-gradient(to right, transparent 0%, black 40%)",
          }}
        />
      </div>
    </article>
  );
}
