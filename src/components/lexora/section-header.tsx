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
  /** When true, renders a thin divider line between title and action. */
  divider?: boolean;
  className?: string;
}) {
  const haptic = useHaptics();

  return (
    <div className={cn("flex items-center gap-2", className)}>
      <h2 className="font-display text-[15px] font-semibold text-foreground tracking-tight whitespace-nowrap">
        {title}
      </h2>
      {divider && <div className="flex-1 h-px bg-border" />}
      <button
        onClick={() => {
          haptic("light");
          onAction?.();
        }}
        className={cn(
          "shrink-0 inline-flex items-center gap-1 text-[11px] font-body font-medium text-highlight dark:text-accent active:scale-95 transition-transform",
          divider ? "" : "ml-auto"
        )}
      >
        {actionLabel}
        <ArrowRight className="w-3 h-3" strokeWidth={2} />
      </button>
    </div>
  );
}
