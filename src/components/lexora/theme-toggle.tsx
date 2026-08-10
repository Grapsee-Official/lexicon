"use client";

import { useEffect, useState } from "react";
import { useTheme } from "next-themes";
import { Sun, Moon } from "lucide-react";
import { useHaptics } from "@/hooks/use-haptics";
import { cn } from "@/lib/utils";

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
        "w-9 h-9 grid place-items-center rounded-full border border-border bg-card/50 text-foreground/80 active:scale-95 transition-transform",
        className
      )}
    >
      {mounted ? (
        isDark ? (
          <Sun className="w-4 h-4" strokeWidth={1.75} />
        ) : (
          <Moon className="w-4 h-4" strokeWidth={1.75} />
        )
      ) : (
        <span className="w-4 h-4" />
      )}
    </button>
  );
}
