"use client";

import { create } from "zustand";
import { persist } from "zustand/middleware";
import type { WordEntry } from "@/lib/lexicon-data";

export type TabKey = "home" | "dictionary" | "search" | "collections" | "more";

type AppState = {
  /** Currently active bottom-nav tab. */
  activeTab: TabKey;
  /** Word currently shown in the detail bottom sheet (null = sheet closed). */
  selectedWord: WordEntry | null;
  /** Whether the full-screen search modal is open. */
  searchOpen: boolean;
  /** Whether the side menu (hamburger) is open. */
  menuOpen: boolean;
  /** Saved / bookmarked word IDs (persisted to localStorage). */
  savedWordIds: string[];
  /** Recent search queries (persisted, max 8). */
  recentSearches: string[];

  setActiveTab: (tab: TabKey) => void;
  openWord: (word: WordEntry) => void;
  closeWord: () => void;
  openSearch: () => void;
  closeSearch: () => void;
  openMenu: () => void;
  closeMenu: () => void;
  toggleSave: (id: string) => void;
  isSaved: (id: string) => boolean;
  addRecentSearch: (q: string) => void;
  clearRecentSearches: () => void;
};

export const useAppStore = create<AppState>()(
  persist(
    (set, get) => ({
      activeTab: "home",
      selectedWord: null,
      searchOpen: false,
      menuOpen: false,
      savedWordIds: [],
      recentSearches: [],

      setActiveTab: (tab) => set({ activeTab: tab }),
      openWord: (word) => set({ selectedWord: word }),
      closeWord: () => set({ selectedWord: null }),
      openSearch: () => set({ searchOpen: true }),
      closeSearch: () => set({ searchOpen: false }),
      openMenu: () => set({ menuOpen: true }),
      closeMenu: () => set({ menuOpen: false }),

      toggleSave: (id) =>
        set((s) => ({
          savedWordIds: s.savedWordIds.includes(id)
            ? s.savedWordIds.filter((x) => x !== id)
            : [...s.savedWordIds, id],
        })),
      isSaved: (id) => get().savedWordIds.includes(id),

      addRecentSearch: (q) =>
        set((s) => {
          const trimmed = q.trim();
          if (!trimmed) return s;
          const next = [trimmed, ...s.recentSearches.filter((r) => r.toLowerCase() !== trimmed.toLowerCase())];
          return { recentSearches: next.slice(0, 8) };
        }),
      clearRecentSearches: () => set({ recentSearches: [] }),
    }),
    {
      name: "lexicon-app-store",
      partialize: (s) => ({
        savedWordIds: s.savedWordIds,
        recentSearches: s.recentSearches,
      }),
    }
  )
);
