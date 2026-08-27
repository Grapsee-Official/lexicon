"use client";

import { useEffect, useState } from "react";
import { useTheme } from "next-themes";
import { Sun, Moon } from "lucide-react";
import { useHaptics } from "@/hooks/use-haptics";
import { cn } from "@/lib/utils";

/**
 * ThemeToggle — Pure Tailwind. Thin stroke icon.
 */
export function ThemeToggle({ className }: { className?: string }) {
  const { theme, setTheme, resolvedTheme } = useTheme();
  const [mounted, setMounted] = useState(false);
  const haptic = useHaptics();

  // next-themes needs a mounted guard to avoid hydration mismatch on the icon.
  // eslint-disable-next-line react-hooks/set-state-in-effect
  useEffect(() => setMounted(true), []);

  const isDark = (resolvedTheme ?? theme) === "dark";

  return (
    <button
      aria-label={isDark ? "Switch to light mode" : "Switch to dark mode"}
      onClick={() => {
        haptic("medium");
        setTheme(isDark ? "light" : "dark");
      }}
      className={cn(
        "grid h-9 w-9 shrink-0 place-items-center rounded-full text-foreground/80 transition-transform active:scale-95",
        className
      )}
    >
      {mounted ? (
        isDark ? (
          <Sun className="h-[18px] w-[18px]" strokeWidth={0.8} />
        ) : (
          <Moon className="h-[18px] w-[18px]" strokeWidth={0.8} />
        )
      ) : (
        <span className="h-[18px] w-[18px]" />
      )}
    </button>
  );
}
