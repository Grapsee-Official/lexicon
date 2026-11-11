"use client";

import { ArrowRight } from "lucide-react";
import { useHaptics } from "@/hooks/use-haptics";
import { cn } from "@/lib/utils";

export function SectionHeader({
  title,
  actionLabel = "View all",
  onAction,
  divider = false,
  className,
}: {
  title: string;
  actionLabel?: string;
  onAction?: () => void;
  divider?: boolean;
  className?: string;
}) {
  const haptic = useHaptics();

  return (
    <div className={cn("flex items-center gap-2.5", className)}>
      <h2 className="font-display text-[18px] font-normal text-foreground tracking-[-0.01em] whitespace-nowrap">
        {title}
      </h2>
      {divider && <div className="flex-1 h-px bg-[#31462F]/12 dark:bg-[#9EA6B2]/16" />}
      <button
        onClick={() => {
          haptic("light");
          onAction?.();
        }}
        className={cn(
          "shrink-0 inline-flex items-center gap-1.5 text-[12px] font-body font-normal text-[#A58A61] dark:text-[#D4A45A] active:scale-95 transition-transform",
          divider ? "" : "ml-auto"
        )}
      >
        {actionLabel}
        <ArrowRight className="w-3.5 h-3.5" strokeWidth={1.7} />
      </button>
    </div>
  );
}
