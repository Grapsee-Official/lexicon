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
        "flex w-full items-center gap-2.5 rounded-full border border-[#31462F]/8 dark:border-[#D4A45A]/24 bg-[#FCF8F0] dark:bg-[#141B25]/95 px-4 py-2.5 text-left shadow-card transition-transform active:scale-[0.99]",
        className
      )}
      aria-label="Open search"
    >
      <Search
        className="h-[18px] w-[18px] shrink-0 text-[#A58A61] dark:text-[#D4A45A]"
        strokeWidth={1.6}
      />
      <span className="flex-1 font-body text-[14px] text-[#A58A61]/85 dark:text-[#9EA6B2]">
        {placeholder}
      </span>
      <Camera
        className="h-[18px] w-[18px] shrink-0 text-[#A58A61] dark:text-[#D4A45A]"
        strokeWidth={1.5}
      />
      <Mic
        className="h-[18px] w-[18px] shrink-0 text-[#A58A61] dark:text-[#D4A45A]"
        strokeWidth={1.5}
      />
    </button>
  );
}
