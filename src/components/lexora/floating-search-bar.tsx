"use client";

import { Search, Camera, Mic } from "lucide-react";
import { useHaptics } from "@/hooks/use-haptics";
import { cn } from "@/lib/utils";

export function FloatingSearchBar({
  className,
  placeholder = "Search for a word...",
}: {
  className?: string;
  placeholder?: string;
}) {
  const haptic = useHaptics();

  return (
    <div
      className={cn(
        "relative paper-surface border border-border rounded-3xl shadow-card overflow-hidden",
        className
      )}
    >
      <div className="flex items-center gap-3 px-4 py-3.5">
        <Search
          className="w-5 h-5 text-foreground/60 shrink-0"
          strokeWidth={1.75}
        />
        <input
          type="text"
          inputMode="search"
          placeholder={placeholder}
          onFocus={() => haptic("light")}
          className="flex-1 bg-transparent outline-none text-sm font-body text-foreground placeholder:text-muted-foreground/70"
        />
        <button
          aria-label="Image search"
          onClick={() => haptic("light")}
          className="w-8 h-8 grid place-items-center rounded-full text-foreground/60 active:scale-95 transition-transform"
        >
          <Camera className="w-[18px] h-[18px]" strokeWidth={1.75} />
        </button>
        <button
          aria-label="Voice search"
          onClick={() => haptic("light")}
          className="w-8 h-8 grid place-items-center rounded-full text-foreground/60 active:scale-95 transition-transform"
        >
          <Mic className="w-[18px] h-[18px]" strokeWidth={1.75} />
        </button>
      </div>
    </div>
  );
}
