"use client";

import { Sprout } from "lucide-react";
import { useHaptics } from "@/hooks/use-haptics";
import { useAppStore } from "@/lib/app-store";
import { cn } from "@/lib/utils";

/**
 * Lexora hero section — per Design Instruction Map:
 * - Continuous background (same as nav, no dividing line)
 * - Illustration placed on the RIGHT, blending into the background
 * - Text aligned LEFT with ample breathing space (NOT overlaying illustration)
 * - Large headline in soft serif
 * - Compact search bar (max-width ~420-480px), soft shadow
 * - Tag chips below search bar, small and balanced
 */
export function Hero({
  className,
}: {
  className?: string;
}) {
  const haptic = useHaptics();
  const openSearch = useAppStore((s) => s.openSearch);

  return (
    <section className={cn("relative w-full overflow-hidden", className)}>
      {/* Illustration on the RIGHT — positioned absolutely, blends into bg */}
      <div className="absolute right-0 top-0 bottom-0 w-[55%] pointer-events-none">
        <img
          src="/illustrations/hero-light.png"
          alt=""
          aria-hidden
          className="w-full h-full object-cover dark:hidden"
          style={{
            maskImage:
              "linear-gradient(to left, #000 0%, #000 50%, transparent 100%)",
            WebkitMaskImage:
              "linear-gradient(to left, #000 0%, #000 50%, transparent 100%)",
          }}
        />
        <img
          src="/illustrations/hero-dark.png"
          alt=""
          aria-hidden
          className="w-full h-full object-cover hidden dark:block"
          style={{
            maskImage:
              "linear-gradient(to left, #000 0%, #000 50%, transparent 100%)",
            WebkitMaskImage:
              "linear-gradient(to left, #000 0%, #000 50%, transparent 100%)",
          }}
        />
      </div>

      {/* LEFT-side content: headline + subtext, with breathing space */}
      <div className="relative px-5 pt-6 pb-8 max-w-[60%]">
        <h1 className="font-display text-[30px] leading-[1.12] font-semibold text-foreground tracking-tight">
          Discover the World of{" "}
          <span className="text-primary dark:text-accent inline-flex items-center gap-1.5">
            Words
            <Sprout
              className="w-5 h-5 text-highlight dark:text-accent"
              strokeWidth={1.5}
            />
          </span>
        </h1>
        <p className="mt-3 font-body text-[13px] leading-relaxed text-foreground/60 max-w-[32ch]">
          A timeless dictionary crafted for curious minds. Explore meanings,
          origins, and stories behind every word.
        </p>
      </div>

      {/* Compact search bar — per spec: compact, soft shadow, doesn't overpower.
          Constrained width so it feels compact on mobile, centered. */}
      <div className="relative pb-3 flex justify-center">
        <button
          type="button"
          onClick={() => {
            haptic("light");
            openSearch();
          }}
          className="flex items-center gap-2 px-3.5 py-2 rounded-full bg-card border border-border shadow-card text-left active:scale-[0.99] transition-transform"
          style={{ width: "calc(100% - 80px)", maxWidth: "380px" }}
          aria-label="Open search"
        >
          <Search
            className="w-3.5 h-3.5 text-foreground/50 shrink-0"
            strokeWidth={1.5}
          />
          <span className="flex-1 text-[12px] font-body text-muted-foreground/70 truncate">
            Search for a word...
          </span>
          <Camera
            className="w-3.5 h-3.5 text-foreground/40 shrink-0"
            strokeWidth={1.5}
          />
          <Mic
            className="w-3.5 h-3.5 text-foreground/40 shrink-0"
            strokeWidth={1.5}
          />
        </button>
      </div>

      {/* Tag chips below search bar — small, balanced */}
      <TrendingTagRowInline />
    </section>
  );
}

/* Inline import to keep hero self-contained per spec */
import { TrendingTagRow } from "@/components/lexora/trending-tag-row";
import { Search, Camera, Mic } from "lucide-react";
import { trendingTags } from "@/lib/lexora-data";

function TrendingTagRowInline() {
  return (
    <div className="px-5 pt-1 pb-6">
      <TrendingTagRow tags={trendingTags} />
    </div>
  );
}
