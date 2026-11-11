"use client";

import { Volume2, ArrowRight } from "lucide-react";
import { useHaptics } from "@/hooks/use-haptics";
import { cn } from "@/lib/utils";
import { useAppStore } from "@/lib/app-store";
import { BotanicalSprig } from "@/components/lexicon/hero";
import type { WordEntry } from "@/lib/lexicon-data";

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
        "grid h-[152px] cursor-pointer overflow-hidden rounded-2xl border border-[#31462F]/12 dark:border-[#9EA6B2]/16 shadow-card grid-cols-[41%_59%] transition-transform active:scale-[0.99]",
        className
      )}
    >
      {/* LEFT PANEL (41%) */}
      <div className="flex flex-col justify-between border-r border-[#31462F]/8 dark:border-[#9EA6B2]/16 bg-[#657D5A] dark:bg-[#121822] px-3.5 py-3.5">
        <div className="flex items-center gap-1.5 text-[#E2CCA0] dark:text-[#D4A45A]">
          <span className="whitespace-nowrap font-body text-[9.5px] font-medium uppercase tracking-[0.06em]">
            Word of the Day
          </span>
          <BotanicalSprig className="h-[15px] w-[15px]" />
        </div>

        <div className="flex min-w-0 items-center gap-1.5">
          <h3 className="truncate font-display text-[24px] font-medium leading-tight tracking-[-0.01em] text-white dark:text-[#EDE4D2]">
            {word.word}
          </h3>
          <button
            aria-label={`Listen to ${word.word}`}
            onClick={(e) => {
              e.stopPropagation();
              haptic("light");
            }}
            className="grid h-6 w-6 shrink-0 place-items-center rounded-full text-[#E2CCA0] dark:text-[#D4A45A] transition-transform active:scale-95"
          >
            <Volume2 className="h-3.5 w-3.5" strokeWidth={1.6} />
          </button>
        </div>

        <div className="flex flex-col gap-0.5">
          <p className="truncate font-body text-[11.5px] text-white/75 dark:text-[#9EA6B2]">
            {word.phonetic}
          </p>
          <p className="font-body text-[12px] lowercase text-[#E2CCA0] dark:text-[#D4A45A]">
            {word.partOfSpeech}
          </p>
        </div>
      </div>

      {/* RIGHT PANEL (59%) */}
      <div className="relative overflow-hidden bg-[#FAF3E6] dark:bg-[#141B25]">
        <div className="absolute right-0 top-0 bottom-0 z-0 h-full w-[54%] overflow-hidden">
          <img
            src="/illustrations/wotd-bg-light.png"
            alt=""
            aria-hidden
            className="h-full w-full object-cover object-right dark:hidden"
          />
          <img
            src="/illustrations/wotd-bg-dark.png"
            alt=""
            aria-hidden
            className="hidden h-full w-full object-cover object-right dark:block"
          />
        </div>

        <div className="relative z-10 flex h-full w-[68%] flex-col justify-between py-4 pl-3.5 pr-1">
          <p className="font-body text-[12.5px] leading-[1.48] text-[#31462F]/88 dark:text-[#EDE4D2]/90 line-clamp-3">
            {word.definitions[0]}
          </p>
          <span className="inline-flex items-center gap-1.5 self-start font-body text-[12px] font-medium text-[#657D5A] dark:text-[#D4A45A]">
            Explore more
            <ArrowRight className="h-3.5 w-3.5" strokeWidth={1.8} />
          </span>
        </div>
      </div>
    </article>
  );
}
