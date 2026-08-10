"use client";

import { Home, BookOpen, Bookmark, MoreHorizontal, Search } from "lucide-react";
import { useHaptics } from "@/hooks/use-haptics";
import { cn } from "@/lib/utils";

type NavKey = "home" | "dictionary" | "search" | "collections" | "more";

export function BottomNav({
  active = "home",
  onNavigate,
  className,
}: {
  active?: NavKey;
  onNavigate?: (key: NavKey) => void;
  className?: string;
}) {
  const haptic = useHaptics();

  const items: {
    key: NavKey;
    label: string;
    icon: typeof Home;
  }[] = [
    { key: "home", label: "Home", icon: Home },
    { key: "dictionary", label: "Dictionary", icon: BookOpen },
    { key: "search", label: "", icon: Search },
    { key: "collections", label: "Collections", icon: Bookmark },
    { key: "more", label: "More", icon: MoreHorizontal },
  ];

  return (
    <nav
      className={cn(
        "fixed bottom-0 inset-x-0 z-40 pb-safe pointer-events-none",
        className
      )}
    >
      <div className="px-4 pb-3 pt-2 pointer-events-auto">
        <div className="paper-surface border border-border rounded-[28px] shadow-float px-2 py-1.5">
          <ul className="flex items-stretch justify-between">
            {items.map((item) => {
              const isActive = item.key === active;
              const isCenter = item.key === "search";

              if (isCenter) {
                return (
                  <li key={item.key} className="flex-1 flex justify-center">
                    <button
                      aria-label="Search"
                      onClick={() => {
                        haptic("medium");
                        onNavigate?.(item.key);
                      }}
                      className="-mt-6 w-14 h-14 grid place-items-center rounded-full bg-primary dark:bg-accent text-primary-foreground dark:text-accent-foreground border-4 border-background shadow-float active:scale-95 transition-transform"
                    >
                      <Search className="w-6 h-6" strokeWidth={2} />
                    </button>
                  </li>
                );
              }

              const Icon = item.icon;
              return (
                <li key={item.key} className="flex-1">
                  <button
                    onClick={() => {
                      haptic("light");
                      onNavigate?.(item.key);
                    }}
                    className="w-full py-1.5 flex flex-col items-center gap-0.5 active:scale-95 transition-transform"
                  >
                    <Icon
                      className={cn(
                        "w-5 h-5",
                        isActive
                          ? "text-primary dark:text-accent"
                          : "text-muted-foreground"
                      )}
                      strokeWidth={isActive ? 2.25 : 1.75}
                      fill={isActive ? "currentColor" : "none"}
                    />
                    <span
                      className={cn(
                        "text-[10px] font-body",
                        isActive
                          ? "text-primary dark:text-accent font-medium"
                          : "text-muted-foreground"
                      )}
                    >
                      {item.label}
                    </span>
                    {isActive && (
                      <span className="w-1 h-1 rounded-full bg-primary dark:bg-accent" />
                    )}
                  </button>
                </li>
              );
            })}
          </ul>
        </div>
      </div>
    </nav>
  );
}
