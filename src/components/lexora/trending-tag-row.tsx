"use client";

import { LayoutGrid } from "lucide-react";
import { useHaptics } from "@/hooks/use-haptics";
import { cn } from "@/lib/utils";
import type { TrendingTag } from "@/lib/lexora-data";

export function TrendingTagRow({
  tags,
  className,
}: {
  tags: TrendingTag[];
  className?: string;
}) {
  const haptic = useHaptics();

  return (
    <div
      className={cn(
        "flex gap-2.5 overflow-x-auto no-scrollbar -mx-5 px-5",
        className
      )}
    >
      {tags.map((t) => (
        <button
          key={t.label}
          onClick={() => haptic("light")}
          className="shrink-0 px-3.5 py-1.5 rounded-full bg-card border border-border text-xs font-body text-foreground/80 active:scale-95 transition-transform"
        >
          {t.label}
        </button>
      ))}
      <button
        aria-label="More tags"
        onClick={() => haptic("light")}
        className="shrink-0 w-9 h-9 grid place-items-center rounded-full bg-card border border-border text-foreground/60 active:scale-95 transition-transform"
      >
        <LayoutGrid className="w-4 h-4" strokeWidth={1.75} />
      </button>
    </div>
  );
}
