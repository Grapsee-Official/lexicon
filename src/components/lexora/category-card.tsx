"use client";

import { useHaptics } from "@/hooks/use-haptics";
import { useAppStore } from "@/lib/app-store";
import { cn } from "@/lib/utils";
import type { Category } from "@/lib/lexora-data";

export function CategoryCard({
  category,
  className,
}: {
  category: Category;
  className?: string;
}) {
  const haptic = useHaptics();
  const setActiveTab = useAppStore((s) => s.setActiveTab);

  return (
    <button
      onClick={() => {
        haptic("light");
        setActiveTab("dictionary");
      }}
      className={cn(
        "shrink-0 w-32 flex flex-col items-center text-center gap-2 p-3 rounded-2xl bg-card border border-border shadow-card active:scale-95 transition-transform",
        className
      )}
    >
      <div className="w-full aspect-square rounded-xl overflow-hidden bg-background/40 grid place-items-center">
        <img
          src={category.illustration}
          alt={`${category.title} illustration`}
          className="w-full h-full object-cover"
          loading="lazy"
        />
      </div>
      <div className="flex flex-col gap-0.5">
        <h4 className="font-display text-sm font-semibold text-foreground leading-tight">
          {category.title}
        </h4>
        <p className="font-body text-[11px] text-muted-foreground leading-tight">
          {category.subtitle}
        </p>
      </div>
    </button>
  );
}
