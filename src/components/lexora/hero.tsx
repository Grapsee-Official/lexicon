"use client";

import { useHaptics } from "@/hooks/use-haptics";
import { useAppStore } from "@/lib/app-store";
import { cn } from "@/lib/utils";

/**
 * Hero — Independent hero section.
 * Pure Tailwind. No CSS file. No !important. No absolute positioning.
 *
 * Layout: CSS Grid with 2 columns — text on left, artwork on right.
 * The artwork SVG has genuine organic transparent edges that blend
 * naturally into the background. No masking needed.
 */
export function Hero({
  className,
}: {
  className?: string;
}) {
  const haptic = useHaptics();
  const openSearch = useAppStore((s) => s.openSearch);

  return (
    <section className={cn("w-full", className)}>
      {/* 2-column grid: text (left) | artwork (right).
          Natural document flow — no absolute positioning. */}
      <div className="grid grid-cols-[1fr_auto] items-start gap-4 px-4 pt-4">
        {/* LEFT: Headline + paragraph */}
        <div className="flex flex-col gap-3 pt-4">
          <h1 className="font-display text-[40px] font-medium leading-[0.95] tracking-tight text-foreground">
            Discover the
            <br />
            World of{" "}
            <span className="inline-flex items-center gap-1 text-[#8FA392]">
              Words
              {/* 3-leaf botanical sprig */}
              <svg
                className="h-6 w-6 text-[#8FA392]"
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

          <p className="max-w-[260px] font-body text-[13px] font-normal leading-relaxed text-foreground/60">
            A timeless dictionary crafted for curious minds. Explore meanings,
            origins, and stories behind every word.
          </p>
        </div>

        {/* RIGHT: Artwork — SVG with genuine transparent edges.
            No absolute positioning, no mask. Just an img in the grid. */}
        <div
          className="flex justify-end pt-2"
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
            className="h-[280px] w-auto max-w-[180px] object-contain"
          />
        </div>
      </div>
    </section>
  );
}
