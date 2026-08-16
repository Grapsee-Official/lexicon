"use client";

import { useHaptics } from "@/hooks/use-haptics";
import { useAppStore } from "@/lib/app-store";
import { cn } from "@/lib/utils";

/**
 * Hero — Independent hero section.
 * Pure Tailwind. No CSS file. No !important.
 *
 * Exact brand colors:
 *   - Background:    #FAF3E9 (warm cream)
 *   - Headline text: #677554 (brand green)
 *   - Leaf icon:     #677554
 *
 * Layout:
 *   - Text column: w-[55%] (healthy width, no harsh max-w constraints)
 *   - Image column: w-[45%]
 *   - z-index layering: text on top (z-10), image underneath (z-0)
 *   - Image scaled up (scale-150) + negative X translation so its soft
 *     radial-gradient edge bleeds BEHIND the text layer — layered, cohesive.
 *   - Headline large (text-5xl) with leading-none for compact stacking.
 */
export function Hero({
  className,
}: {
  className?: string;
}) {
  const haptic = useHaptics();
  const openSearch = useAppStore((s) => s.openSearch);

  return (
    <section className={cn("relative w-full overflow-hidden bg-[#FAF3E9]", className)}>
      {/* 2-column grid: text (55%) | image (45%).
          Image bleeds behind text via z-index layering, not rigid bounds. */}
      <div className="grid grid-cols-[55%_45%] items-start px-4 pt-4">
        {/* LEFT: Headline + paragraph.
            z-10 so text stays on top of the image bleed.
            w-[55%] gives healthy width — no harsh max-w squishing. */}
        <div className="relative z-10 flex w-[55%] flex-col gap-3 pt-6">
          <h1 className="font-display text-5xl font-medium leading-none tracking-tight text-[#677554]">
            Discover the
            <br />
            World of{" "}
            <span className="inline-flex items-center gap-1 text-[#677554]">
              Words
              {/* 3-leaf botanical sprig — brand green #677554 */}
              <svg
                className="h-8 w-8 text-[#677554]"
                viewBox="0 0 40 40"
                aria-hidden="true"
              >
                <line
                  x1="20"
                  y1="36"
                  x2="20"
                  y2="4"
                  stroke="currentColor"
                  strokeWidth="0.8"
                  strokeLinecap="round"
                />
                <ellipse
                  cx="11"
                  cy="22"
                  rx="8"
                  ry="3.2"
                  fill="currentColor"
                  opacity="0.85"
                  transform="rotate(-35 11 22)"
                />
                <line
                  x1="5.5"
                  y1="24.5"
                  x2="16.5"
                  y2="19.5"
                  stroke="currentColor"
                  strokeWidth="0.4"
                  opacity="0.5"
                />
                <ellipse
                  cx="29"
                  cy="15"
                  rx="8"
                  ry="3.2"
                  fill="currentColor"
                  opacity="0.85"
                  transform="rotate(35 29 15)"
                />
                <line
                  x1="23.5"
                  y1="12.5"
                  x2="34.5"
                  y2="17.5"
                  stroke="currentColor"
                  strokeWidth="0.4"
                  opacity="0.5"
                />
                <ellipse
                  cx="20"
                  cy="7"
                  rx="4.5"
                  ry="2.8"
                  fill="currentColor"
                  opacity="0.9"
                />
                <line
                  x1="20"
                  y1="5"
                  x2="20"
                  y2="9"
                  stroke="currentColor"
                  strokeWidth="0.4"
                  opacity="0.5"
                />
              </svg>
            </span>
          </h1>

          <p className="w-[200px] font-body text-[13px] font-normal leading-relaxed text-foreground/60">
            A timeless dictionary crafted for curious minds. Explore meanings,
            origins, and stories behind every word.
          </p>
        </div>

        {/* RIGHT: Image container — z-0 (underneath text).
            Image scaled up (scale-150) + negative X translation so its
            soft radial-gradient edge bleeds BEHIND the text layer.
            This creates a layered, cohesive environment without
            forcing the text to wrap. */}
        <div
          className="relative z-0 flex justify-end pt-2"
          onClick={() => {
            haptic("light");
            openSearch();
          }}
        >
          <img
            src="/assets/lexora-countryside-day.svg"
            alt=""
            aria-hidden
            draggable={false}
            className="h-[320px] w-auto max-w-[260px] object-contain"
            style={{
              // Scale up + negative X so the soft edge bleeds behind the
              // text layer (which has z-10, so text stays readable on top).
              transform: "scale(1.5) translateX(-48px)",
              transformOrigin: "right center",
              // Radial-gradient mask: soft fade on left + bottom edges into #FAF3E9
              WebkitMaskImage:
                "radial-gradient(ellipse 100% 100% at 65% 40%, #000 40%, rgba(0,0,0,0.85) 60%, rgba(0,0,0,0.4) 80%, transparent 100%)",
              maskImage:
                "radial-gradient(ellipse 100% 100% at 65% 40%, #000 40%, rgba(0,0,0,0.85) 60%, rgba(0,0,0,0.4) 80%, transparent 100%)",
            }}
          />
        </div>
      </div>
    </section>
  );
}
