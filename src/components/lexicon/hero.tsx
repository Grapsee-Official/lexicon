"use client";

import { useHaptics } from "@/hooks/use-haptics";
import { useAppStore } from "@/lib/app-store";
import { FloatingSearchBar } from "@/components/lexicon/floating-search-bar";
import { cn } from "@/lib/utils";

export function BotanicalSprig({ className }: { className?: string }) {
  return (
    <svg
      viewBox="0 0 32 32"
      fill="none"
      aria-hidden="true"
      className={cn("h-6 w-6 shrink-0", className)}
    >
      <path
        d="M5 27 Q14 17 24 7"
        stroke="currentColor"
        strokeWidth="1.4"
        strokeLinecap="round"
      />
      <g fill="currentColor" fillOpacity="0.92">
        <path d="M7.2 16.5 Q11.5 12.9 15.8 16.5 Q11.5 20.1 7.2 16.5 Z" transform="rotate(-72 11.5 16.5)" />
        <path d="M13.5 10.5 Q17.5 7.1 21.5 10.5 Q17.5 13.9 13.5 10.5 Z" transform="rotate(-66 17.5 10.5)" />
        <path d="M11.2 20.5 Q15.5 16.9 19.8 20.5 Q15.5 24.1 11.2 20.5 Z" transform="rotate(12 15.5 20.5)" />
        <path d="M17.1 14.8 Q21 11.5 24.9 14.8 Q21 18.1 17.1 14.8 Z" transform="rotate(8 21 14.8)" />
        <path d="M21.4 6.2 Q25 3.1 28.6 6.2 Q25 9.3 21.4 6.2 Z" transform="rotate(-38 25 6.2)" />
      </g>
    </svg>
  );
}

export function Hero({ className }: { className?: string }) {
  const haptic = useHaptics();
  const openSearch = useAppStore((s) => s.openSearch);

  return (
    <section className={cn("relative h-[276px] w-full overflow-hidden", className)}>
      {/* Right-anchored full-scene botanical cottage + open book + pen */}
      <div
        onClick={() => {
          haptic("light");
          openSearch();
        }}
        className="absolute right-[-8px] bottom-[-6px] h-[276px] w-[70%] cursor-pointer select-none"
      >
        <img
          src="/illustrations/hero-light.png"
          alt=""
          aria-hidden
          draggable={false}
          className="h-full w-full object-contain object-bottom-right dark:hidden"
        />
        <img
          src="/illustrations/hero-dark.png"
          alt=""
          aria-hidden
          draggable={false}
          className="hidden h-full w-full object-contain object-bottom-right dark:block"
        />
      </div>

      {/* Left editorial headline + 4-line paragraph */}
      <div className="relative z-10 flex w-[49%] flex-col gap-2.5 pt-2.5 pl-[18px]">
        <h1 className="font-display text-[31px] font-normal leading-[1.15] tracking-[-0.01em] text-[#31462F] dark:text-[#EDE4D2]">
          Discover the
          <br />
          World of
          <br />
          <span className="inline-flex items-center gap-2.5 text-[#657D5A] dark:text-[#7D9372]">
            Words
            <BotanicalSprig className="h-[26px] w-[26px] text-[#A58A61] dark:text-[#D4A45A]" />
          </span>
        </h1>

        <p className="font-body text-[12px] font-normal leading-[1.45] text-[#31462F]/78 dark:text-[#9EA6B2]">
          A timeless dictionary crafted for curious minds. Explore meanings,
          origins, and stories behind every word.
        </p>
      </div>

      {/* Overlapping pill search bar (~79% width so the fountain pen stays visible on right!) */}
      <div className="absolute bottom-[2px] left-4 z-20 w-[79%]">
        <FloatingSearchBar />
      </div>
    </section>
  );
}
