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
 *   - Surface tint:  #FBF4EA (subtle card bg)
 *   - Headline text: #677554 (brand green)
 *   - Leaf icon:     #677554
 *
 * Layout guardrails:
 *   - NO negative X-axis translation on the artwork.
 *   - Artwork anchored flush to the right margin (justify-end).
 *   - Left text container restricted to max-w-[45%] so it never collides
 *     with the right-side artwork.
 *   - Text column: z-10 relative (on top of any soft bleeds).
 *   - Image container: z-0 (behind text).
 *   - Headline scaled down (text-2xl / 24px) with tight leading.
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
      {/* 2-column grid: text (left, max 45%) | artwork (right, anchored).
          Natural document flow — no absolute positioning for layout. */}
      <div className="relative grid grid-cols-[45%_1fr] items-start gap-2 px-4 pt-4">
        {/* LEFT: Headline + paragraph.
            z-10 so text stays on top of any soft background bleeds.
            max-w-[45%] guarantees no collision with the right artwork. */}
        <div className="relative z-10 flex max-w-[45%] flex-col gap-2 pt-6">
          <h1 className="font-display text-2xl font-medium leading-tight tracking-tight text-[#677554]">
            Discover the
            <br />
            World of{" "}
            <span className="inline-flex items-center gap-1 text-[#677554]">
              Words
              {/* 3-leaf botanical sprig — brand green #677554 */}
              <svg
                className="h-4 w-4 text-[#677554]"
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

          <p className="max-w-[200px] font-body text-[11px] font-normal leading-relaxed text-foreground/60">
            A timeless dictionary crafted for curious minds. Explore meanings,
            origins, and stories behind every word.
          </p>
        </div>

        {/* RIGHT: Artwork — anchored flush to the right margin.
            z-0 so it sits behind the text column.
            NO negative X-axis translation. Scale up with positive X to
            push slightly toward the right edge (bleeding outward, not inward).
            Radial-gradient mask fades left + bottom edges into #FAF3E9. */}
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
            className="h-[260px] w-auto max-w-[200px] object-contain"
            style={{
              // Scale up + positive X translation (pushes RIGHT, away from text).
              // No negative X — never drags artwork into the text column.
              transform: "scale(1.25) translateX(16px)",
              transformOrigin: "right center",
              // Radial-gradient mask: fades left + bottom edges into #FAF3E9
              WebkitMaskImage:
                "radial-gradient(ellipse 100% 100% at 70% 40%, #000 45%, rgba(0,0,0,0.8) 65%, rgba(0,0,0,0.3) 85%, transparent 100%)",
              maskImage:
                "radial-gradient(ellipse 100% 100% at 70% 40%, #000 45%, rgba(0,0,0,0.8) 65%, rgba(0,0,0,0.3) 85%, transparent 100%)",
            }}
          />
        </div>
      </div>
    </section>
  );
}
