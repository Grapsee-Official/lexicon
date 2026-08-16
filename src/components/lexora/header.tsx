"use client";

import Link from "next/link";
import { Menu, Bell } from "lucide-react";
import { useHaptics } from "@/hooks/use-haptics";
import { ThemeToggle } from "@/components/lexora/theme-toggle";
import { cn } from "@/lib/utils";

/**
 * LexoraHeader — Independent navigation component.
 * Pure Tailwind. No CSS file. No !important.
 * Transparent background so nav flows seamlessly into the hero below.
 *
 * Vertical alignment: all items use items-center on the flex row.
 * Logo mark + wordmark + tagline are vertically centered as a group.
 */
export function LexoraHeader({ className }: { className?: string }) {
  const haptic = useHaptics();

  return (
    <header
      className={cn(
        "sticky top-0 z-30 w-full pt-safe",
        className
      )}
    >
      <div className="flex items-center gap-2 px-4 py-3">
        {/* LEFT: hamburger */}
        <button
          aria-label="Open menu"
          onClick={() => haptic("light")}
          className="grid h-9 w-9 shrink-0 place-items-center rounded-full text-foreground/80 transition-transform active:scale-95"
        >
          <Menu className="h-[18px] w-[18px]" strokeWidth={0.8} />
        </button>

        {/* Brand lockup: SVG mark + wordmark + tagline.
            Vertically centered as a group via items-center on parent. */}
        <Link
          href="/"
          onClick={() => haptic("light")}
          className="mr-auto flex min-w-0 items-center gap-2"
        >
          <img
            src="/assets/lexora-mark.svg"
            alt="Lexora"
            className="h-10 w-10 shrink-0"
          />
          <div className="flex min-w-0 flex-col justify-center leading-none">
            <span className="font-display text-[18px] leading-none tracking-tight text-foreground">
              Lexora
            </span>
            <span className="mt-1 font-body text-[8px] font-normal leading-none tracking-[0.08em] text-muted-foreground">
              Words. Wisdom. Worlds.
            </span>
          </div>
        </Link>

        {/* RIGHT: theme toggle + bell + avatar.
            All same height (h-9) for perfect alignment. */}
        <ThemeToggle />
        <button
          aria-label="Notifications"
          onClick={() => haptic("light")}
          className="relative grid h-9 w-9 shrink-0 place-items-center rounded-full text-foreground/80 transition-transform active:scale-95"
        >
          <Bell className="h-[18px] w-[18px]" strokeWidth={0.8} />
          <span className="absolute right-2 top-2 h-1.5 w-1.5 rounded-full bg-accent" />
        </button>
        <button
          aria-label="Profile"
          onClick={() => haptic("light")}
          className="h-9 w-9 shrink-0 overflow-hidden rounded-full ring-1 ring-border transition-transform active:scale-95"
        >
          <img
            src="/illustrations/avatar-default.png"
            alt="Profile"
            className="h-full w-full object-cover"
          />
        </button>
      </div>
    </header>
  );
}
