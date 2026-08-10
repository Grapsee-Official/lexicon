"use client";

import { useHaptics } from "@/hooks/use-haptics";
import { cn } from "@/lib/utils";

export function Hero({
  className,
}: {
  className?: string;
}) {
  const haptic = useHaptics();

  return (
    <section className={cn("relative", className)}>
      {/* Watercolour backdrop */}
      <div className="absolute inset-0 -z-10 overflow-hidden rounded-3xl">
        <img
          src="/illustrations/hero-light.png"
          alt=""
          aria-hidden
          className="w-full h-full object-cover dark:hidden fade-edges"
        />
        <img
          src="/illustrations/hero-dark.png"
          alt=""
          aria-hidden
          className="w-full h-full object-cover hidden dark:block fade-edges"
        />
      </div>

      <div className="px-5 pt-6 pb-7">
        <h1 className="font-display text-[34px] leading-[1.1] font-semibold text-foreground tracking-tight max-w-[16ch]">
          Discover the World of{" "}
          <span className="text-primary dark:text-accent">Words</span>
        </h1>
        <p className="mt-3 font-body text-sm leading-relaxed text-foreground/70 max-w-[36ch]">
          A timeless dictionary crafted for curious minds. Explore meanings,
          origins, and stories behind every word.
        </p>
        <button
          onClick={() => haptic("light")}
          className="mt-4 inline-flex items-center gap-2 px-4 py-2 rounded-full bg-primary dark:bg-accent text-primary-foreground dark:text-accent-foreground text-xs font-body font-medium shadow-card active:scale-95 transition-transform"
        >
          Begin your journey
        </button>
      </div>
    </section>
  );
}
