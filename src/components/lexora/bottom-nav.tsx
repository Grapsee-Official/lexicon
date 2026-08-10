"use client";

import { Home, BookOpen, Bookmark, MoreHorizontal, Search } from "lucide-react";
import { useHaptics } from "@/hooks/use-haptics";
import { cn } from "@/lib/utils";
import { useAppStore, type TabKey } from "@/lib/app-store";

export function BottomNav({ className }: { className?: string }) {
  const active = useAppStore((s) => s.activeTab);
  const setActiveTab = useAppStore((s) => s.setActiveTab);
  const openSearch = useAppStore((s) => s.openSearch);
  const haptic = useHaptics();

  const items: {
    key: TabKey;
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
      <div className="px-4 pb-2 pt-1 pointer-events-auto">
        <div className="paper-surface border border-border rounded-full shadow-float px-1 py-0.5">
          <ul className="flex items-stretch justify-between">
            {items.map((item) => {
              const isActive = item.key === active;
              const isCenter = item.key === "search";

              if (isCenter) {
                return (
                  <li key={item.key} className="flex-1 flex justify-center">
                    <button
                      aria-label="Open search"
                      onClick={() => {
                        haptic("medium");
                        openSearch();
                      }}
                      className="-mt-4 w-10 h-10 grid place-items-center rounded-full bg-primary dark:bg-accent text-primary-foreground dark:text-accent-foreground border-2 border-background shadow-float active:scale-95 transition-transform"
                    >
                      <Search className="w-4 h-4" strokeWidth={2} />
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
                      setActiveTab(item.key);
                    }}
                    className="w-full py-1.5 flex flex-col items-center gap-0.5 active:scale-95 transition-transform"
                  >
                    <Icon
                      className={cn(
                        "w-[16px] h-[16px]",
                        isActive
                          ? "text-primary dark:text-accent"
                          : "text-muted-foreground"
                      )}
                      strokeWidth={isActive ? 2.25 : 1.75}
                      fill={isActive ? "currentColor" : "none"}
                    />
                    <span
                      className={cn(
                        "text-[8px] font-body leading-none",
                        isActive
                          ? "text-primary dark:text-accent font-medium"
                          : "text-muted-foreground"
                      )}
                    >
                      {item.label}
                    </span>
                    {isActive && (
                      <span className="w-0.5 h-0.5 rounded-full bg-primary dark:bg-accent" />
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
