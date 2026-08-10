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
      className={cn(
        "relative overflow-hidden w-full h-[280px]",
        className
      )}
      onClick={() => {
        haptic("light");
        openSearch();
      }}
    >
      {/* Edge-to-edge watercolour backdrop — NO rounded corners, NO padding */}
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
          Text sits in the upper-left quadrant. */}
      <div className="absolute inset-0 bg-gradient-to-br from-background via-background/70 to-transparent dark:from-background dark:via-background/60" />
      {/* Bottom fade into the page below */}
      <div className="absolute inset-x-0 bottom-0 h-20 bg-gradient-to-t from-background to-transparent" />

      {/* Text overlay — upper-left quadrant, small */}
      <div className="relative h-full flex flex-col justify-center px-5 max-w-[60%]">
        <h1 className="font-display text-[26px] leading-[1.12] font-semibold text-foreground tracking-tight">
          Discover the World of{" "}
          <span className="text-primary dark:text-accent inline-flex items-center gap-1">
            Words
            <Sprout
              className="w-4 h-4 text-highlight dark:text-accent"
              strokeWidth={1.75}
            />
          </span>
        </h1>
        <p className="mt-2 font-body text-[12px] leading-relaxed text-foreground/60 max-w-[28ch]">
          A timeless dictionary crafted for curious minds. Explore meanings,
          origins, and stories behind every word.
        </p>
      </div>
    </section>
  );
}
