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
      <div className="px-4 pb-2.5 pt-1 pointer-events-auto">
        <div className="border border-[#31462F]/10 dark:border-[#9EA6B2]/16 bg-[#FAF4E8]/96 dark:bg-[#141B25]/96 rounded-full shadow-float px-2 py-1.5 backdrop-blur-xs">
          <ul className="flex items-center justify-between">
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
                      className="w-[46px] h-[46px] grid place-items-center rounded-full bg-[#657D5A] dark:bg-[#101620] text-[#F9F4EA] dark:text-[#F0D89A] border border-transparent dark:border-[#D4A45A]/55 shadow-card active:scale-95 transition-transform"
                    >
                      <Search className="w-5 h-5" strokeWidth={1.8} />
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
                    className="w-full py-1 flex flex-col items-center gap-1 active:scale-95 transition-transform"
                  >
                    <Icon
                      className={cn(
                        "w-[19px] h-[19px]",
                        isActive
                          ? "text-[#657D5A] dark:text-[#D4A45A]"
                          : "text-[#666052] dark:text-[#D4A45A]/78"
                      )}
                      strokeWidth={isActive ? 2 : 1.6}
                      fill={isActive && item.key !== "more" && item.key !== "dictionary" ? "currentColor" : "none"}
                    />
                    <span
                      className={cn(
                        "text-[10px] font-body leading-none",
                        isActive
                          ? "text-[#657D5A] dark:text-[#D4A45A] font-medium"
                          : "text-[#666052] dark:text-[#D4A45A]/78"
                      )}
                    >
                      {item.label}
                    </span>
                    <span
                      className={cn(
                        "w-1 h-1 rounded-full",
                        isActive
                          ? "bg-[#657D5A] dark:bg-[#F0D89A]"
                          : "bg-transparent"
                      )}
                    />
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
