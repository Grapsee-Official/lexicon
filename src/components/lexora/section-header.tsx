"use client";

import { ArrowRight } from "lucide-react";
import { useHaptics } from "@/hooks/use-haptics";
import { cn } from "@/lib/utils";

export function SectionHeader({
  title,
  actionLabel = "View all",
  onAction,
  className,
}: {
  title: string;
  actionLabel?: string;
  onAction?: () => void;
  className?: string;
}) {
  const haptic = useHaptics();

  return (
    <div className={cn("flex items-end justify-between gap-3", className)}>
      <h2 className="font-display text-base font-semibold text-foreground tracking-tight truncate">
        {title}
      </h2>
      <button
        onClick={() => {
          haptic("light");
          onAction?.();
        }}
        className="shrink-0 inline-flex items-center gap-1 text-xs font-body font-medium text-highlight dark:text-accent active:scale-95 transition-transform"
      >
        {actionLabel}
        <ArrowRight className="w-3.5 h-3.5" strokeWidth={2} />
      </button>
    </div>
  );
}
