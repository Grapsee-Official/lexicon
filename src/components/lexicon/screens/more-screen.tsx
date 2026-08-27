"use client";

import { motion } from "framer-motion";
import {
  Moon,
  Sun,
  Languages,
  Database,
  Download,
  Bell,
  Eye,
  Info,
  Heart,
  FileText,
  ChevronRight,
  Github,
} from "lucide-react";
import { useTheme } from "next-themes";
import { useAppStore } from "@/lib/app-store";
import { useHaptics } from "@/hooks/use-haptics";
import { useState, useEffect } from "react";
import { cn } from "@/lib/utils";

export function MoreScreen() {
  const { theme, setTheme, resolvedTheme } = useTheme();
  const clearRecentSearches = useAppStore((s) => s.clearRecentSearches);
  const recentCount = useAppStore((s) => s.recentSearches.length);
  const savedCount = useAppStore((s) => s.savedWordIds.length);
  const haptic = useHaptics();
  const [mounted, setMounted] = useState(false);
  // eslint-disable-next-line react-hooks/set-state-in-effect
  useEffect(() => setMounted(true), []);

  const isDark = (resolvedTheme ?? theme) === "dark";

  return (
    <motion.div
      initial={{ opacity: 0, y: 12 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.3 }}
      className="flex flex-col gap-6"
    >
      {/* Profile header */}
      <section className="flex items-center gap-4 p-4 rounded-2xl bg-card border border-border shadow-card">
        <div className="w-16 h-16 rounded-full overflow-hidden border-2 border-primary/30 dark:border-accent/30 shrink-0">
          <img
            src="/illustrations/avatar-default.png"
            alt="Profile"
            className="w-full h-full object-cover"
          />
        </div>
        <div className="flex-1 min-w-0">
          <h2 className="font-display text-lg font-semibold text-foreground">
            Curious Soul
          </h2>
          <p className="font-body text-xs text-muted-foreground">
            Lexicon Companion · since 2026
          </p>
          <div className="flex gap-4 mt-1.5">
            <span className="font-body text-[11px] text-muted-foreground">
              <strong className="font-semibold text-foreground">
                {savedCount}
              </strong>{" "}
              saved
            </span>
            <span className="font-body text-[11px] text-muted-foreground">
              <strong className="font-semibold text-foreground">
                {recentCount}
              </strong>{" "}
              recent
            </span>
          </div>
        </div>
      </section>

      {/* Appearance */}
      <SettingsGroup title="Appearance">
        <SettingsRow
          icon={isDark ? Moon : Sun}
          label="Theme"
          value={mounted ? (isDark ? "Dark" : "Light") : "System"}
          onClick={() => {
            haptic("medium");
            setTheme(isDark ? "light" : "dark");
          }}
        />
        <SettingsRow icon={Eye} label="Text size" value="Default" onClick={() => haptic("light")} />
        <SettingsRow icon={Bell} label="Notifications" value="On" onClick={() => haptic("light")} />
      </SettingsGroup>

      {/* Dictionary */}
      <SettingsGroup title="Dictionary">
        <SettingsRow icon={Languages} label="Language pairs" value="36 installed" onClick={() => haptic("light")} />
        <SettingsRow icon={Database} label="Storage" value="412 MB" onClick={() => haptic("light")} />
        <SettingsRow icon={Download} label="Download language packs" value="" onClick={() => haptic("light")} />
      </SettingsGroup>

      {/* Privacy & Data */}
      <SettingsGroup title="Privacy & Data">
        <SettingsRow
          icon={FileText}
          label="Clear recent searches"
          value={recentCount > 0 ? `${recentCount} items` : "Empty"}
          onClick={() => {
            haptic("medium");
            clearRecentSearches();
          }}
        />
        <SettingsRow icon={Database} label="Clear cache" value="412 MB" onClick={() => haptic("light")} />
      </SettingsGroup>

      {/* About */}
      <SettingsGroup title="About">
        <SettingsRow icon={Info} label="About Lexicon" value="v1.0.0" onClick={() => haptic("light")} />
        <SettingsRow icon={FileText} label="Privacy policy" value="" onClick={() => haptic("light")} />
        <SettingsRow icon={Github} label="Open source licences" value="" onClick={() => haptic("light")} />
      </SettingsGroup>

      {/* Footer credit */}
      <div className="flex flex-col items-center gap-1 py-10">
        <div className="flex items-center gap-1.5 text-muted-foreground">
          <Heart
            className="w-3.5 h-3.5 text-accent"
            strokeWidth={1.75}
            fill="currentColor"
          />
          <span className="font-body text-[11px]">
            Crafted in the spirit of the Cotswolds
          </span>
        </div>
        <p className="font-body text-[10px] text-muted-foreground/60">
          Lexicon · Words. Wisdom. Worlds.
        </p>
      </div>
    </motion.div>
  );
}

function SettingsGroup({
  title,
  children,
}: {
  title: string;
  children: React.ReactNode;
}) {
  return (
    <section className="flex flex-col gap-2">
      <h3 className="font-body text-[11px] font-semibold tracking-[0.14em] uppercase text-muted-foreground px-1">
        {title}
      </h3>
      <ul className="bg-card border border-border rounded-2xl overflow-hidden shadow-card">
        {children}
      </ul>
    </section>
  );
}

function SettingsRow({
  icon: Icon,
  label,
  value,
  onClick,
}: {
  icon: typeof Moon;
  label: string;
  value: string;
  onClick: () => void;
}) {
  return (
    <li>
      <button
        onClick={onClick}
        className={cn(
          "w-full flex items-center gap-3 px-4 py-3.5 text-left active:bg-background/50 transition-colors group"
        )}
      >
        <div className="w-8 h-8 rounded-lg bg-primary/10 dark:bg-accent/10 grid place-items-center shrink-0">
          <Icon
            className="w-4 h-4 text-primary dark:text-accent"
            strokeWidth={1.75}
          />
        </div>
        <span className="flex-1 font-body text-sm text-foreground">{label}</span>
        {value && (
          <span className="font-body text-xs text-muted-foreground">{value}</span>
        )}
        <ChevronRight
          className="w-4 h-4 text-muted-foreground/60 group-hover:translate-x-0.5 transition-transform"
          strokeWidth={1.75}
        />
      </button>
    </li>
  );
}
