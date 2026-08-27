"use client";

import { useHaptics } from "@/hooks/use-haptics";
import { useAppStore } from "@/lib/app-store";
import { cn } from "@/lib/utils";

/**
 * Hero — Independent hero section.
 * Pure Tailwind. No CSS file. No !important.
 *
 * SIMPLE 2-COLUMN LAYOUT:
 *   LEFT column: headline + paragraph (text)
 *   RIGHT column: image (contained, no bleed, no scale)
 *
 * Exact brand colors:
 *   - Background:    #FAF3E9 (warm cream)
 *   - Headline text: #677554 (brand green)
 *   - Leaf icon:     #677554
 *
 * The image SVG has genuine organic transparent edges — no mask needed.
 */
export function Hero({
  className,
}: {
  className?: string;
}) {
  const haptic = useHaptics();
  const openSearch = useAppStore((s) => s.openSearch);

  return (
    <section className={cn("w-full bg-[#FAF3E9] px-4 pt-2", className)}>
      {/* 2-column grid: text (left) | image (right). SEPARATE columns. */}
      <div className="grid grid-cols-2 items-center gap-2">
        {/* LEFT: Headline + paragraph */}
        <div className="flex flex-col gap-2">
          <h1 className="font-display text-[28px] font-medium leading-[1.05] tracking-tight text-[#677554]">
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
                <line x1="20" y1="36" x2="20" y2="4" stroke="currentColor" strokeWidth="0.8" strokeLinecap="round" />
                <ellipse cx="11" cy="22" rx="8" ry="3.2" fill="currentColor" opacity="0.85" transform="rotate(-35 11 22)" />
                <line x1="5.5" y1="24.5" x2="16.5" y2="19.5" stroke="currentColor" strokeWidth="0.4" opacity="0.5" />
                <ellipse cx="29" cy="15" rx="8" ry="3.2" fill="currentColor" opacity="0.85" transform="rotate(35 29 15)" />
                <line x1="23.5" y1="12.5" x2="34.5" y2="17.5" stroke="currentColor" strokeWidth="0.4" opacity="0.5" />
                <ellipse cx="20" cy="7" rx="4.5" ry="2.8" fill="currentColor" opacity="0.9" />
                <line x1="20" y1="5" x2="20" y2="9" stroke="currentColor" strokeWidth="0.4" opacity="0.5" />
              </svg>
            </span>
          </h1>

          <p className="font-body text-[11px] font-normal leading-relaxed text-foreground/60">
            A timeless dictionary crafted for curious minds.
          </p>
        </div>

        {/* RIGHT: Image — contained in its column. No scale, no bleed, no mask.
            The SVG has genuine organic transparent edges already. */}
        <div
          className="flex justify-end"
          onClick={() => {
            haptic("light");
            openSearch();
          }}
        >
          <img
            src="/assets/lexicon-countryside-day.svg"
            alt=""
            aria-hidden
            draggable={false}
            className="h-[200px] w-auto max-w-full object-contain"
          />
        </div>
      </div>
    </section>
  );
}
