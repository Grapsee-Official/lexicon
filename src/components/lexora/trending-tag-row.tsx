"use client";

import { LayoutGrid } from "lucide-react";
import { useHaptics } from "@/hooks/use-haptics";
import { cn } from "@/lib/utils";
import { useAppStore } from "@/lib/app-store";
import { getWordById, type TrendingTag } from "@/lib/lexora-data";

/**
 * TrendingTagRow — Independent tag chips component.
 * Pure Tailwind. Thin stroke icons. Includes 5th grid button.
 */
export function TrendingTagRow({
  tags,
  className,
}: {
  tags: TrendingTag[];
  className?: string;
}) {
  const haptic = useHaptics();
  const openWord = useAppStore((s) => s.openWord);

  return (
    <div className={cn("flex gap-2 overflow-x-auto no-scrollbar", className)}>
      {tags.map((t) => (
        <button
          key={t.label}
          onClick={() => {
            haptic("light");
            const entry = getWordById(t.wordId);
            if (entry) openWord(entry);
          }}
          className="shrink-0 rounded-full border border-border bg-card px-3 py-1.5 font-body text-[11px] text-foreground/70 transition-transform active:scale-95"
        >
          {t.label}
        </button>
      ))}
      {/* 5th grid button */}
      <button
        aria-label="More tags"
        onClick={() => haptic("light")}
        className="grid h-7 w-7 shrink-0 place-items-center rounded-full border border-border bg-card text-foreground/50 transition-transform active:scale-95"
      >
        <LayoutGrid className="h-3.5 w-3.5" strokeWidth={0.8} />
      </button>
    </div>
  );
}
