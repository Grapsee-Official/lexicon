"use client";

import { LayoutGrid } from "lucide-react";
import { useHaptics } from "@/hooks/use-haptics";
import { cn } from "@/lib/utils";
import { useAppStore } from "@/lib/app-store";
import { getWordById, type TrendingTag } from "@/lib/lexora-data";

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
    <div
      className={cn(
        "flex gap-2 overflow-x-auto no-scrollbar -mx-5 px-5",
        className
      )}
    >
      {tags.map((t) => (
        <button
          key={t.label}
          onClick={() => {
            haptic("light");
            const entry = getWordById(t.wordId);
            if (entry) openWord(entry);
          }}
          className="shrink-0 px-3 py-1 rounded-full bg-card border border-border text-[12px] font-body text-foreground/70 active:scale-95 transition-transform"
        >
          {t.label}
        </button>
      ))}
      <button
        aria-label="More tags"
        onClick={() => haptic("light")}
        className="shrink-0 w-7 h-7 grid place-items-center rounded-full bg-card border border-border text-foreground/50 active:scale-95 transition-transform"
      >
        <LayoutGrid className="w-3.5 h-3.5" strokeWidth={1.75} />
      </button>
    </div>
  );
}
