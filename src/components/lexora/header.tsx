"use client";

import Link from "next/link";
import { Menu, Bell } from "lucide-react";
import { useHaptics } from "@/hooks/use-haptics";
import { ThemeToggle } from "@/components/lexora/theme-toggle";
import { cn } from "@/lib/utils";

/**
 * Lexora navigation bar — per Design Instruction Map:
 * - Same background as hero (NO dividing line, NO paper-surface, NO border-b)
 * - Background flows continuously into hero below
 * - Logo LARGE and clear; tagline smaller and softer
 * - Icons: thin stroke, warm tone
 * - 16–20px padding top/sides
 */
export function LexoraHeader({ className }: { className?: string }) {
  const haptic = useHaptics();

  return (
    <header
      className={cn(
        "sticky top-0 z-30 w-full pt-safe bg-background",
        className
      )}
    >
      <div className="px-5 pt-3 pb-2 flex items-center gap-3">
        {/* LEFT: hamburger + logo + brand wordmark */}
        <button
          aria-label="Open menu"
          onClick={() => haptic("light")}
          className="w-9 h-9 grid place-items-center rounded-full text-foreground/80 active:scale-95 transition-transform shrink-0"
        >
          <Menu className="w-[19px] h-[19px]" strokeWidth={1.5} />
        </button>

        <Link
          href="/"
          onClick={() => haptic("light")}
          className="flex items-center gap-2 mr-auto min-w-0"
        >
          {/* Circular logo icon — large, clear presence */}
          <img
            src="/icon-192.png"
            alt="Lexora"
            className="w-9 h-9 rounded-full object-cover shrink-0 ring-1 ring-border"
          />
          <div className="flex flex-col items-start leading-none min-w-0">
            <span className="font-display text-[18px] tracking-tight text-foreground leading-none">
              Lexora
            </span>
            <span className="font-body text-[9px] tracking-[0.12em] uppercase text-muted-foreground mt-1 italic">
              Words. Wisdom. Worlds.
            </span>
          </div>
        </Link>

        {/* RIGHT: theme + bell + avatar — thin stroke, warm tone */}
        <ThemeToggle />
        <button
          aria-label="Notifications"
          onClick={() => haptic("light")}
          className="relative w-9 h-9 grid place-items-center rounded-full text-foreground/80 active:scale-95 transition-transform shrink-0"
        >
          <Bell className="w-[19px] h-[19px]" strokeWidth={1.5} />
          <span className="absolute top-2 right-2.5 w-1.5 h-1.5 rounded-full bg-accent" />
        </button>
        <button
          aria-label="Profile"
          onClick={() => haptic("light")}
          className="w-8 h-8 rounded-full overflow-hidden ring-1 ring-border active:scale-95 transition-transform shrink-0"
        >
          <img
            src="/illustrations/avatar-default.png"
            alt="Profile"
            className="w-full h-full object-cover"
          />
        </button>
      </div>
    </header>
  );
}
