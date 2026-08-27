"use client";

import { Search, Camera, Mic } from "lucide-react";
import { useHaptics } from "@/hooks/use-haptics";
import { useAppStore } from "@/lib/app-store";
import { cn } from "@/lib/utils";

/**
 * FloatingSearchBar — Independent search bar component.
 * Pure Tailwind. Thin stroke icons.
 */
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
        "flex w-full items-center gap-2.5 rounded-full border border-border bg-card px-4 py-2.5 text-left shadow-card transition-transform active:scale-[0.99]",
        className
      )}
      aria-label="Open search"
    >
      <Search
        className="h-4 w-4 shrink-0 text-foreground/50"
        strokeWidth={0.8}
      />
      <span className="flex-1 font-body text-[13px] text-muted-foreground/70">
        {placeholder}
      </span>
      <Camera
        className="h-4 w-4 shrink-0 text-foreground/40"
        strokeWidth={0.8}
      />
      <Mic
        className="h-4 w-4 shrink-0 text-foreground/40"
        strokeWidth={0.8}
      />
    </button>
  );
}
