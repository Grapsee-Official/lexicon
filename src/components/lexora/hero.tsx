"use client";

import { Sprout } from "lucide-react";
import { Search, Camera, Mic } from "lucide-react";
import { useHaptics } from "@/hooks/use-haptics";
import { useAppStore } from "@/lib/app-store";
import { TrendingTagRow } from "@/components/lexora/trending-tag-row";
import { trendingTags } from "@/lib/lexora-data";
import { cn } from "@/lib/utils";

/**
 * Lexora hero — reproduces the spatial composition of the reference:
 *   Zone 1: Navigation (rendered by <LexoraHeader /> above, seamless, no border)
 *   Zone 2: Hero — full-width watercolour background, text in LEFT zone (~40%),
 *           illustration visible on the right (~60%)
 *   Zone 3: Search bar — OVERLAPS the bottom of the hero (negative margin, z-10),
 *           ~85% width centered
 *   Zone 4: Tag chips — below search in their own row, left-aligned
 *
 * The search bar is NOT below the hero. It sits on top of the hero's bottom edge.
 */
export function Hero({
  className,
}: {
  className?: string;
}) {
  const haptic = useHaptics();
  const openSearch = useAppStore((s) => s.openSearch);

  return (
    <section className={cn("relative w-full", className)}>
      {/* ZONE 2: Hero — full-width watercolour background */}
      <div className="relative w-full h-[300px] overflow-hidden">
        {/* Full-bleed illustration */}
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

        {/* Left-side gradient so text is readable on the left.
            Fades to transparent on the right so illustration is visible there. */}
        <div
          className="absolute inset-0"
          style={{
            background:
              "linear-gradient(to right, var(--background) 0%, var(--background) 35%, rgba(0,0,0,0) 70%)",
          }}
        />

        {/* Text in LEFT zone — ~40% width, breathing room */}
        <div className="relative h-full flex flex-col justify-center px-5 w-[45%]">
          <h1 className="font-display text-[28px] leading-[1.12] font-semibold text-foreground tracking-tight">
            Discover the World of{" "}
            <span className="text-primary dark:text-accent inline-flex items-center gap-1.5">
              Words
              <Sprout
                className="w-4 h-4 text-highlight dark:text-accent"
                strokeWidth={1.5}
              />
            </span>
          </h1>
          <p className="mt-2 font-body text-[12px] leading-relaxed text-foreground/60">
            A timeless dictionary crafted for curious minds.
          </p>
        </div>
      </div>

      {/* ZONE 3: Search bar — OVERLAPS the hero bottom (negative margin + z-10).
          ~85% width, centered. */}
      <div className="relative -mt-8 z-10 flex justify-center px-5">
        <button
          type="button"
          onClick={() => {
            haptic("light");
            openSearch();
          }}
          className="w-full max-w-[440px] flex items-center gap-2.5 px-4 py-2.5 rounded-full bg-card border border-border shadow-card text-left active:scale-[0.99] transition-transform"
          aria-label="Open search"
        >
          <Search
            className="w-4 h-4 text-foreground/50 shrink-0"
            strokeWidth={1.5}
          />
          <span className="flex-1 text-[13px] font-body text-muted-foreground/70">
            Search for a word...
          </span>
          <Camera
            className="w-4 h-4 text-foreground/40 shrink-0"
            strokeWidth={1.5}
          />
          <Mic
            className="w-4 h-4 text-foreground/40 shrink-0"
            strokeWidth={1.5}
          />
        </button>
      </div>

      {/* ZONE 4: Tag chips — below search, left-aligned, own row */}
      <div className="px-5 pt-3 pb-6">
        <TrendingTagRow tags={trendingTags} />
      </div>
    </section>
  );
}
