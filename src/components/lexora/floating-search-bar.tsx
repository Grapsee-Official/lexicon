"use client";

import { Search, Camera, Mic } from "lucide-react";
import { useHaptics } from "@/hooks/use-haptics";
import { useAppStore } from "@/lib/app-store";
import { cn } from "@/lib/utils";

export function FloatingSearchBar({
  className,
  placeholder = "Search for a word...",
}: {
  className?: string;
  placeholder?: string;
}) {
  const haptic = useHaptics();
  const openSearch = useAppStore((s) => s.openSearch);

  return (
    <button
      type="button"
      onClick={() => {
        haptic("light");
        openSearch();
      }}
      className={cn(
        "relative paper-surface border border-border rounded-3xl shadow-card overflow-hidden w-full text-left active:scale-[0.99] transition-transform",
        className
      )}
      aria-label="Open search"
    >
      <div className="flex items-center gap-3 px-4 py-3.5">
        <Search
          className="w-5 h-5 text-foreground/60 shrink-0"
          strokeWidth={1.75}
        />
        <span className="flex-1 text-sm font-body text-muted-foreground/70">
          {placeholder}
        </span>
        <span
          aria-hidden
          className="w-8 h-8 grid place-items-center rounded-full text-foreground/60"
        >
          <Camera className="w-[18px] h-[18px]" strokeWidth={1.75} />
        </span>
        <span
          aria-hidden
          className="w-8 h-8 grid place-items-center rounded-full text-foreground/60"
        >
          <Mic className="w-[18px] h-[18px]" strokeWidth={1.75} />
        </span>
      </div>
    </button>
  );
}
