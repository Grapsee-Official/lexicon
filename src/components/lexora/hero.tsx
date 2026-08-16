"use client";

import { useHaptics } from "@/hooks/use-haptics";
import { useAppStore } from "@/lib/app-store";
import { cn } from "@/lib/utils";

/**
 * Hero — Independent hero section.
 * Pure Tailwind. No CSS file. No !important.
 *
 * Exact brand colors:
 *   - Headline text: #677554 (brand green)
 *   - Leaf icon:    #677554
 *   - Background:    #FAF3E9 (warm cream)
 *
 * The hero artwork is dramatically scaled (scale-150) and translated so
 * it bleeds out of its grid cell, creating an immersive countryside
 * environment rather than a shrunken sticker. A radial-gradient mask
 * fades the edges seamlessly into the #FAF3E9 background.
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
      {/* 2-column grid: text (left) | artwork (right).
          Natural document flow — no absolute positioning for layout. */}
      <div className="relative grid grid-cols-[1fr_auto] items-start gap-4 px-4 pt-4">
        {/* LEFT: Headline + paragraph.
            Exact brand green #677554 for all headline text + leaf. */}
        <div className="relative z-10 flex flex-col gap-2 pt-6">
          <h1 className="font-display text-[34px] font-medium leading-[1.05] tracking-tight text-[#677554]">
            Discover the
            <br />
            World of{" "}
            <span className="inline-flex items-center gap-1 text-[#677554]">
              Words
              {/* 3-leaf botanical sprig — brand green #677554 */}
              <svg
                className="h-5 w-5 text-[#677554]"
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

          <p className="max-w-[250px] font-body text-[12px] font-normal leading-relaxed text-foreground/60">
            A timeless dictionary crafted for curious minds. Explore meanings,
            origins, and stories behind every word.
          </p>
        </div>

        {/* RIGHT: Artwork — dramatically scaled + translated to bleed.
            This creates an immersive countryside environment, not a sticker.
            Uses scale-150 + negative translate so the landscape expands
            beyond its grid cell. Radial-gradient mask fades edges into
            the #FAF3E9 background. */}
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
            className="h-[280px] w-auto max-w-[220px] object-contain"
            style={{
              // Dramatically scale up + translate so the landscape bleeds
              // out of its grid cell — immersive countryside environment.
              transform: "scale(1.5) translate(-32px, -16px)",
              transformOrigin: "center right",
              // Radial-gradient mask: fades left + bottom edges into #FAF3E9
              // so the artwork blends seamlessly into the warm cream background.
              WebkitMaskImage:
                "radial-gradient(ellipse 100% 100% at 65% 35%, #000 40%, rgba(0,0,0,0.8) 60%, rgba(0,0,0,0.3) 82%, transparent 100%)",
              maskImage:
                "radial-gradient(ellipse 100% 100% at 65% 35%, #000 40%, rgba(0,0,0,0.8) 60%, rgba(0,0,0,0.3) 82%, transparent 100%)",
            }}
          />
        </div>
      </div>
    </section>
  );
}
