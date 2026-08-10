"use client";

import { useEffect, useState, useMemo } from "react";
import { useAppStore } from "@/lib/app-store";
import { useHaptics } from "@/hooks/use-haptics";
import { getAlphabetSections, type WordEntry } from "@/lib/lexora-data";
import { Volume2 } from "lucide-react";
import { cn } from "@/lib/utils";

export function DictionaryScreen() {
  const openWord = useAppStore((s) => s.openWord);
  const haptic = useHaptics();
  const sections = useMemo(() => getAlphabetSections(), []);
  const [activeLetter, setActiveLetter] = useState(0);

  // Sticky letter index — track which section is in view
  useEffect(() => {
    const observer = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting) {
            const letter = (entry.target as HTMLElement).dataset.letter;
            const idx = sections.findIndex((s) => s.letter === letter);
            if (idx !== -1) setActiveLetter(idx);
          }
        });
      },
      { rootMargin: "-20% 0px -70% 0px" }
    );
    sections.forEach((s) => {
      const el = document.getElementById(`dict-section-${s.letter}`);
      if (el) observer.observe(el);
    });
    return () => observer.disconnect();
  }, [sections]);

  return (
    <div className="flex flex-col gap-3">
      {/* A–Z quick index (horizontal) */}
      <div className="sticky top-[52px] z-20 -mx-4 px-4 py-1.5 paper-surface border-b border-border">
        <div className="flex gap-1 overflow-x-auto no-scrollbar">
          {sections.map((s, i) => (
            <button
              key={s.letter}
              onClick={() => {
                haptic("light");
                document
                  .getElementById(`dict-section-${s.letter}`)
                  ?.scrollIntoView({ behavior: "smooth", block: "start" });
              }}
              className={cn(
                "shrink-0 w-6 h-6 grid place-items-center rounded-full font-display text-[11px] transition-all",
                i === activeLetter
                  ? "bg-primary dark:bg-accent text-primary-foreground dark:text-accent-foreground font-semibold"
                  : "text-muted-foreground hover:text-foreground"
              )}
            >
              {s.letter}
            </button>
          ))}
        </div>
      </div>

      {/* Sections */}
      {sections.map((section) => (
        <section
          key={section.letter}
          id={`dict-section-${section.letter}`}
          data-letter={section.letter}
          className="scroll-mt-[140px]"
        >
          <div className="flex items-baseline gap-2 mb-1.5 px-1">
            <h2 className="font-display text-lg font-semibold text-primary dark:text-accent">
              {section.letter}
            </h2>
            <div className="flex-1 h-px bg-border" />
            <span className="font-body text-[10px] text-muted-foreground">
              {section.entries.length}{" "}
              {section.entries.length === 1 ? "word" : "words"}
            </span>
          </div>

          <ul className="bg-card border border-border rounded-xl overflow-hidden shadow-card">
            {section.entries.map((entry, idx) => (
              <DictionaryRow
                key={entry.id}
                entry={entry}
                isFirst={idx === 0}
                onClick={() => {
                  haptic("light");
                  openWord(entry);
                }}
              />
            ))}
          </ul>
        </section>
      ))}
    </div>
  );
}

function DictionaryRow({
  entry,
  isFirst,
  onClick,
}: {
  entry: WordEntry;
  isFirst: boolean;
  onClick: () => void;
}) {
  return (
    <li>
      <button
        onClick={onClick}
        className={cn(
          "w-full flex items-baseline gap-2.5 px-3 py-2.5 text-left active:bg-background/50 transition-colors",
          !isFirst && "border-t border-border"
        )}
      >
        <div className="flex-1 min-w-0">
          <div className="flex items-baseline gap-2 mb-0.5">
            <h4 className="font-display text-[14px] font-semibold text-foreground truncate">
              {entry.word}
            </h4>
            <span className="font-body italic text-[10px] text-muted-foreground lowercase shrink-0">
              {entry.partOfSpeech}
            </span>
          </div>
          <p className="font-long text-[12px] text-foreground/70 line-clamp-1 leading-snug">
            {entry.definitions[0]}
          </p>
        </div>
        <Volume2
          className="w-3.5 h-3.5 text-muted-foreground shrink-0"
          strokeWidth={1.75}
        />
      </button>
    </li>
  );
}
