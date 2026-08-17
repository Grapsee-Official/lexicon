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
 * The landscape image is placed ONLY inside the right panel as an absolute
 * element anchored to the bottom-right. A mask-image fades its left + top
 * edges into the #FAF3E9 background. The image NEVER sits under the text.
 *
 * The transition between left and right panels is a SHARP, CLEAN LINE.
 * No gradient blending.
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
          RIGHT PANEL (60%) — solid #FAF3E9, text-[#677554]
          Decorative image anchored bottom-right with mask fade.
          Image NEVER sits under the text.
          ============================================================ */}
      <div className="relative bg-[#FAF3E9]">
        {/* Decorative landscape image — absolute, bottom-right anchored.
            Mask fades left + top edges into #FAF3E9 so it's purely
            decorative and never under the text. */}
        <img
          src="/illustrations/wotd-bg-light.png"
          alt=""
          aria-hidden
          className="pointer-events-none absolute bottom-0 right-0 h-full w-full object-cover dark:hidden"
          style={{
            // Fade left + top edges into #FAF3E9. Image only visible on
            // the bottom-right, never under the text.
            WebkitMaskImage:
              "linear-gradient(to right, transparent 0%, transparent 30%, black 70%, black 100%), linear-gradient(to bottom, transparent 0%, transparent 40%, black 70%, black 100%)",
            maskImage:
              "linear-gradient(to right, transparent 0%, transparent 30%, black 70%, black 100%), linear-gradient(to bottom, transparent 0%, transparent 40%, black 70%, black 100%)",
            WebkitMaskComposite: "source-in",
            maskComposite: "intersect",
          }}
        />
        <img
          src="/illustrations/wotd-bg-dark.png"
          alt=""
          aria-hidden
          className="pointer-events-none absolute bottom-0 right-0 hidden h-full w-full object-cover dark:block"
          style={{
            WebkitMaskImage:
              "linear-gradient(to right, transparent 0%, transparent 30%, black 70%, black 100%), linear-gradient(to bottom, transparent 0%, transparent 40%, black 70%, black 100%)",
            maskImage:
              "linear-gradient(to right, transparent 0%, transparent 30%, black 70%, black 100%), linear-gradient(to bottom, transparent 0%, transparent 40%, black 70%, black 100%)",
            WebkitMaskComposite: "source-in",
            maskComposite: "intersect",
          }}
        />

        {/* Text content — on top of the solid #FAF3E9 background.
            text-[#677554] is perfectly readable against cream. */}
        <div className="relative flex h-full min-h-[150px] flex-col justify-between gap-2 p-4">
          <p className="line-clamp-3 font-long text-[12px] leading-relaxed text-[#677554]">
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
