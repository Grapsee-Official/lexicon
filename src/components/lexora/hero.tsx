"use client";

import { Sprout } from "lucide-react";
import { useHaptics } from "@/hooks/use-haptics";
import { useAppStore } from "@/lib/app-store";
import { cn } from "@/lib/utils";

export function Hero({
  className,
}: {
  className?: string;
}) {
  const haptic = useHaptics();
  const openSearch = useAppStore((s) => s.openSearch);

  return (
    <section
      className={cn("relative overflow-hidden rounded-2xl", className)}
      onClick={() => {
        haptic("light");
        openSearch();
      }}
    >
      {/* Prominent watercolour backdrop — the image IS the hero */}
      <div className="absolute inset-0">
        <img
          src="/illustrations/hero-light.png"
          alt=""
          aria-hidden
          className="w-full h-full object-cover dark:hidden"
        />
        <img
          src="/illustrations/hero-dark.png"
          alt=""
          aria-hidden
          className="w-full h-full object-cover hidden dark:block"
        />
      </div>

      {/* Left-side gradient scrim so text is readable over the image.
          Stronger on left, fades right — text sits on the left half. */}
      <div className="absolute inset-0 bg-gradient-to-r from-background via-background/80 to-background/10 dark:from-background dark:via-background/70 dark:to-transparent" />
      {/* Bottom fade so the hero blends into the page below */}
      <div className="absolute inset-x-0 bottom-0 h-16 bg-gradient-to-t from-background to-transparent" />

      {/* Text overlay — small, gentle, left-aligned */}
      <div className="relative px-4 pt-6 pb-12 max-w-[60%]">
        <h1 className="font-display text-[24px] leading-[1.15] font-semibold text-foreground tracking-tight">
          Discover the World of{" "}
          <span className="text-primary dark:text-accent inline-flex items-center gap-1">
            Words
            <Sprout className="w-4 h-4 text-highlight dark:text-accent" strokeWidth={1.75} />
          </span>
        </h1>
        <p className="mt-1.5 font-body text-[12px] leading-relaxed text-foreground/55 max-w-[28ch]">
          A timeless dictionary crafted for curious minds.
        </p>
      </div>
    </section>
  );
}
