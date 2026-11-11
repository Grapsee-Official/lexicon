"use client";

import Link from "next/link";
import { Menu, Bell } from "lucide-react";
import { useHaptics } from "@/hooks/use-haptics";
import { ThemeToggle } from "@/components/lexicon/theme-toggle";
import { useAppStore } from "@/lib/app-store";
import { cn } from "@/lib/utils";

export function LexiconHeader({ className }: { className?: string }) {
  const haptic = useHaptics();
  const setActiveTab = useAppStore((s) => s.setActiveTab);

  return (
    <header className={cn("sticky top-0 z-30 w-full bg-background/90 backdrop-blur-xs pt-safe", className)}>
      <div className="flex items-center gap-2 px-4 py-2.5">
        <button
          aria-label="Open menu"
          onClick={() => haptic("light")}
          className="grid h-9 w-9 shrink-0 place-items-center rounded-full text-[#31462F]/75 dark:text-[#D4A45A]/90 transition-transform active:scale-95"
        >
          <Menu className="h-[21px] w-[21px]" strokeWidth={1.5} />
        </button>

        <Link
          href="/"
          onClick={() => {
            haptic("light");
            setActiveTab("home");
          }}
          className="mr-auto flex min-w-0 items-center gap-2.5"
        >
          <img
            src="/illustrations/lexicon-mark.png"
            alt=""
            aria-hidden
            className="h-11 w-11 shrink-0 object-contain dark:hidden"
          />
          <img
            src="/illustrations/lexicon-mark-dark.png"
            alt=""
            aria-hidden
            className="hidden h-11 w-11 shrink-0 object-contain dark:block"
          />
          <div className="flex min-w-0 flex-col justify-center">
            <span className="font-display text-[22px] font-normal leading-none tracking-[-0.01em] text-[#657D5A] dark:text-[#EDE4D2]">
              Lexora
            </span>
            <span className="mt-1 font-body text-[9.5px] font-normal leading-none tracking-[0.02em] text-[#31462F]/75 dark:text-[#EDE4D2]/80">
              Words. Wisdom. Worlds.
            </span>
          </div>
        </Link>

        <ThemeToggle />

        <button
          aria-label="Notifications"
          onClick={() => haptic("light")}
          className="relative grid h-9 w-9 shrink-0 place-items-center rounded-full text-[#31462F]/75 dark:text-[#D4A45A]/90 transition-transform active:scale-95"
        >
          <Bell className="h-[20px] w-[20px]" strokeWidth={1.5} />
          <span className="absolute right-1.5 top-1.5 h-1.5 w-1.5 rounded-full bg-[#A58A61] dark:bg-[#F0D89A]" />
        </button>

        <button
          aria-label="Profile"
          onClick={() => {
            haptic("light");
            setActiveTab("more");
          }}
          className="h-[38px] w-[38px] shrink-0 overflow-hidden rounded-full ring-1 ring-[#31462F]/16 dark:ring-[#D4A45A]/35 transition-transform active:scale-95"
        >
          <img
            src="/illustrations/avatar-default.png"
            alt="Profile"
            className="h-full w-full object-cover dark:hidden"
          />
          <img
            src="/illustrations/avatar-dark.png"
            alt="Profile"
            className="hidden h-full w-full object-cover dark:block"
          />
        </button>
      </div>
    </header>
  );
}
