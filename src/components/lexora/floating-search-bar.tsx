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
        "relative w-full flex items-center gap-2.5 px-4 py-2.5 rounded-2xl bg-card border border-border shadow-card text-left active:scale-[0.99] transition-transform",
        className
      )}
      aria-label="Open search"
    >
      <Search
        className="w-4 h-4 text-foreground/50 shrink-0"
        strokeWidth={1.75}
      />
      <span className="flex-1 text-[13px] font-body text-muted-foreground/70">
        {placeholder}
      </span>
      <Camera
        className="w-4 h-4 text-foreground/40 shrink-0"
        strokeWidth={1.75}
      />
      <Mic
        className="w-4 h-4 text-foreground/40 shrink-0"
        strokeWidth={1.75}
      />
    </button>
  );
}
