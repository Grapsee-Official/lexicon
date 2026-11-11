"use client";

import { LayoutGrid } from "lucide-react";
import { useHaptics } from "@/hooks/use-haptics";
import { cn } from "@/lib/utils";
import { useAppStore } from "@/lib/app-store";
import { getWordById, type TrendingTag } from "@/lib/lexicon-data";

export function TrendingTagRow({
  tags,
  className,
}: {
  tags: TrendingTag[];
  className?: string;
}) {
  const haptic = useHaptics();
  const openWord = useAppStore((s) => s.openWord);
  const setActiveTab = useAppStore((s) => s.setActiveTab);

  return (
    <div className={cn("flex items-center gap-2 overflow-x-auto no-scrollbar", className)}>
      {tags.map((t) => (
        <button
          key={t.label}
          onClick={() => {
            haptic("light");
            const entry = getWordById(t.wordId);
            if (entry) openWord(entry);
          }}
          className="shrink-0 rounded-full border border-[#31462F]/12 dark:border-[#9EA6B2]/16 bg-[#FAF4E8]/75 dark:bg-[#141B25] px-3.5 py-1.5 font-body text-[12px] text-[#31462F]/85 dark:text-[#EDE4D2]/88 transition-transform active:scale-95"
        >
          {t.label}
        </button>
      ))}
      <button
        aria-label="More tags"
        onClick={() => {
          haptic("light");
          setActiveTab("dictionary");
        }}
        className="grid h-8 w-[34px] shrink-0 place-items-center rounded-xl border border-[#31462F]/12 dark:border-[#9EA6B2]/16 bg-[#FAF4E8]/75 dark:bg-[#141B25] text-[#31462F]/70 dark:text-[#D4A45A] transition-transform active:scale-95"
      >
        <LayoutGrid className="h-3.5 w-3.5" strokeWidth={1.6} />
      </button>
    </div>
  );
}
