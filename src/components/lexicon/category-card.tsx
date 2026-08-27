"use client";

import { useHaptics } from "@/hooks/use-haptics";
import { useAppStore } from "@/lib/app-store";
import { cn } from "@/lib/utils";
import type { Category } from "@/lib/lexicon-data";

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
        "flex w-24 shrink-0 flex-col items-center gap-2 rounded-2xl bg-transparent p-2 transition-transform active:scale-95",
        className
      )}
    >
      {/* Seamless image — no border, no shadow, bg matches image */}
      <div className="aspect-square w-full overflow-hidden rounded-2xl">
        <img
          src={category.illustration}
          alt={`${category.title} illustration`}
          className="h-full w-full object-cover"
          loading="lazy"
        />
      </div>
      {/* Title + subtitle perfectly centered below image */}
      <div className="flex w-full flex-col items-center gap-0.5">
        <h4 className="font-display text-[11px] font-semibold leading-tight text-foreground">
          {category.title}
        </h4>
        <p className="font-body text-[8px] leading-tight text-muted-foreground">
          {category.subtitle}
        </p>
      </div>
    </button>
  );
}
