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
      <div className="px-5 py-3 flex items-center justify-between paper-surface border-b border-border">
        <button
          aria-label="Open menu"
          onClick={() => haptic("light")}
          className="w-10 h-10 grid place-items-center rounded-full text-foreground/80 active:scale-95 transition-transform"
        >
          <Menu className="w-5 h-5" strokeWidth={1.75} />
        </button>

        <Link
          href="/"
          onClick={() => haptic("light")}
          className="flex flex-col items-center leading-none"
        >
          <span className="font-display text-xl tracking-tight text-foreground">
            Lexora
          </span>
          <span className="font-body text-[10px] tracking-[0.18em] uppercase text-muted-foreground mt-0.5">
            Words · Wisdom · Worlds
          </span>
        </Link>

        <div className="flex items-center gap-1">
          <ThemeToggle />
          <button
            aria-label="Notifications"
            onClick={() => haptic("light")}
            className="relative w-10 h-10 grid place-items-center rounded-full text-foreground/80 active:scale-95 transition-transform"
          >
            <Bell className="w-5 h-5" strokeWidth={1.75} />
            <span className="absolute top-2 right-2.5 w-1.5 h-1.5 rounded-full bg-accent" />
          </button>
          <button
            aria-label="Profile"
            onClick={() => haptic("light")}
            className="w-9 h-9 rounded-full overflow-hidden border border-border active:scale-95 transition-transform"
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
