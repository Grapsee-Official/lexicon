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
                      aria-label="Open search"
                      onClick={() => {
                        haptic("medium");
                        openSearch();
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
                      setActiveTab(item.key);
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
