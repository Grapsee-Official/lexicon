"use client";

import Link from "next/link";
import { Menu, Bell } from "lucide-react";
import { useHaptics } from "@/hooks/use-haptics";
import { ThemeToggle } from "@/components/lexora/theme-toggle";
import { cn } from "@/lib/utils";

export function LexoraHeader({ className }: { className?: string }) {
  const haptic = useHaptics();

  return (
    <header
      className={cn(
        "sticky top-0 z-30 w-full pt-safe",
        className
      )}
    >
      <div className="px-4 py-2 flex items-center justify-between paper-surface border-b border-border">
        <button
          aria-label="Open menu"
          onClick={() => haptic("light")}
          className="w-8 h-8 grid place-items-center rounded-full text-foreground/80 active:scale-95 transition-transform"
        >
          <Menu className="w-[18px] h-[18px]" strokeWidth={1.75} />
        </button>

        <Link
          href="/"
          onClick={() => haptic("light")}
          className="flex items-center gap-1.5"
        >
          <img
            src="/icon-192.png"
            alt="Lexora"
            className="w-6 h-6 rounded-md object-cover shrink-0"
          />
          <div className="flex flex-col items-center leading-none">
            <span className="font-display text-[16px] tracking-tight text-foreground">
              Lexora
            </span>
            <span className="font-body text-[7px] tracking-[0.16em] uppercase text-muted-foreground mt-0.5">
              Words · Wisdom · Worlds
            </span>
          </div>
        </Link>

        <div className="flex items-center gap-0.5">
          <ThemeToggle />
          <button
            aria-label="Notifications"
            onClick={() => haptic("light")}
            className="relative w-8 h-8 grid place-items-center rounded-full text-foreground/80 active:scale-95 transition-transform"
          >
            <Bell className="w-[18px] h-[18px]" strokeWidth={1.75} />
            <span className="absolute top-1.5 right-2 w-1.5 h-1.5 rounded-full bg-accent" />
          </button>
          <button
            aria-label="Profile"
            onClick={() => haptic("light")}
            className="w-7 h-7 rounded-full overflow-hidden border border-border active:scale-95 transition-transform"
          >
            <img
              src="/illustrations/avatar-default.png"
              alt="Profile"
              className="w-full h-full object-cover"
            />
          </button>
        </div>
      </div>
    </header>
  );
}
