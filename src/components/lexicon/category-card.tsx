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
  const darkSrc = category.illustration.replace(/\.png$/, "-dark.png");

  return (
    <button
      onClick={() => {
        haptic("light");
        setActiveTab("dictionary");
      }}
      className={cn(
        "flex w-[104px] shrink-0 flex-col items-center gap-2 rounded-[14px] border border-[#31462F]/10 dark:border-[#9EA6B2]/16 bg-[#FAF4E8] dark:bg-[#141B25] px-2 py-2.5 shadow-card transition-transform active:scale-95",
        className
      )}
    >
      <div className="aspect-square w-full overflow-hidden rounded-[10px]">
        <img
          src={category.illustration}
          alt={`${category.title} illustration`}
          className="h-full w-full object-cover dark:hidden"
          loading="lazy"
        />
        <img
          src={darkSrc}
          alt={`${category.title} illustration`}
          className="hidden h-full w-full object-cover dark:block"
          loading="lazy"
        />
      </div>
      <div className="flex w-full flex-col items-center gap-0.5">
        <h4 className="font-display text-[13px] font-medium leading-tight text-foreground">
          {category.title}
        </h4>
        <p className="font-body text-[9.5px] leading-tight text-muted-foreground">
          {category.subtitle}
        </p>
      </div>
    </button>
  );
}
