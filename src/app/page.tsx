"use client";

import { motion, AnimatePresence } from "framer-motion";
import { LexoraHeader } from "@/components/lexora/header";
import { Hero } from "@/components/lexora/hero";
import { FloatingSearchBar } from "@/components/lexora/floating-search-bar";
import { TrendingTagRow } from "@/components/lexora/trending-tag-row";
import { WordOfTheDayCard } from "@/components/lexora/word-of-the-day-card";
import { CategoryCard } from "@/components/lexora/category-card";
import { ArticleCard } from "@/components/lexora/article-card";
import { SectionHeader } from "@/components/lexora/section-header";
import { BottomNav } from "@/components/lexora/bottom-nav";
import { WordDetailSheet } from "@/components/lexora/word-detail-sheet";
import { SearchModal } from "@/components/lexora/search-modal";
import { DictionaryScreen } from "@/components/lexora/screens/dictionary-screen";
import { CollectionsScreen } from "@/components/lexora/screens/collections-screen";
import { MoreScreen } from "@/components/lexora/screens/more-screen";
import { useAppStore } from "@/lib/app-store";
import { useHaptics } from "@/hooks/use-haptics";
import {
  trendingTags,
  wordOfTheDay,
  categories,
  articles,
} from "@/lib/lexora-data";

const tabMeta: Record<
  string,
  { title?: string; subtitle?: string }
> = {
  dictionary: { title: "Dictionary", subtitle: "Browse the full lexicon" },
  collections: { title: "Collections", subtitle: "Your saved words" },
  more: { title: "Settings", subtitle: "Make Lexora yours" },
};

export default function Home() {
  const activeTab = useAppStore((s) => s.activeTab);

  return (
    <div className="min-h-screen-safe flex flex-col bg-background">
      <LexoraHeader />

      <main className="flex-1 flex flex-col gap-6 pb-32 px-5">
        {/* Sub-header for non-home tabs */}
        {tabMeta[activeTab] && (
          <div className="pt-2 pb-1">
            <motion.h1
              key={`title-${activeTab}`}
              initial={{ opacity: 0, y: 6 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.25 }}
              className="font-display text-2xl font-semibold text-foreground"
            >
              {tabMeta[activeTab].title}
            </motion.h1>
            <p className="font-body text-xs text-muted-foreground mt-0.5">
              {tabMeta[activeTab].subtitle}
            </p>
          </div>
        )}

        <AnimatePresence mode="wait">
          {activeTab === "home" && (
            <motion.div
              key="home"
              initial={{ opacity: 0, y: 8 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0, y: -8 }}
              transition={{ duration: 0.22 }}
              className="flex flex-col gap-6 -mx-5"
            >
              <Hero />

              <div className="px-5 flex flex-col gap-4">
                <FloatingSearchBar />
                <TrendingTagRow tags={trendingTags} />
              </div>

              <div className="px-5">
                <WordOfTheDayCard word={wordOfTheDay} />
              </div>

              <section className="flex flex-col gap-3">
                <div className="px-5">
                  <SectionHeader
                    title="Explore by Category"
                    actionLabel="View all"
                  />
                </div>
                <div className="flex gap-3 overflow-x-auto no-scrollbar px-5">
                  {categories.map((c, i) => (
                    <motion.div
                      key={c.id}
                      initial={{ opacity: 0, x: 12 }}
                      animate={{ opacity: 1, x: 0 }}
                      transition={{ delay: 0.05 * i, duration: 0.3 }}
                    >
                      <CategoryCard category={c} />
                    </motion.div>
                  ))}
                </div>
              </section>

              <section className="flex flex-col gap-3">
                <div className="px-5">
                  <SectionHeader
                    title="From the Library"
                    actionLabel="View all articles"
                  />
                </div>
                <div className="flex gap-3 overflow-x-auto no-scrollbar px-5">
                  {articles.map((a, i) => (
                    <motion.div
                      key={a.id}
                      initial={{ opacity: 0, x: 12 }}
                      animate={{ opacity: 1, x: 0 }}
                      transition={{ delay: 0.05 * i, duration: 0.3 }}
                    >
                      <ArticleCard article={a} />
                    </motion.div>
                  ))}
                </div>
              </section>
            </motion.div>
          )}

          {activeTab === "dictionary" && (
            <motion.div
              key="dictionary"
              initial={{ opacity: 0, y: 8 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0, y: -8 }}
              transition={{ duration: 0.22 }}
            >
              <DictionaryScreen />
            </motion.div>
          )}

          {activeTab === "collections" && (
            <motion.div
              key="collections"
              initial={{ opacity: 0, y: 8 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0, y: -8 }}
              transition={{ duration: 0.22 }}
            >
              <CollectionsScreen />
            </motion.div>
          )}

          {activeTab === "more" && (
            <motion.div
              key="more"
              initial={{ opacity: 0, y: 8 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0, y: -8 }}
              transition={{ duration: 0.22 }}
            >
              <MoreScreen />
            </motion.div>
          )}
        </AnimatePresence>
      </main>

      <BottomNav />

      {/* Overlays */}
      <WordDetailSheet />
      <SearchModal />

      <footer className="sr-only">
        <p>
          Lexora — Words. Wisdom. Worlds. A timeless dictionary for curious
          minds.
        </p>
      </footer>
    </div>
  );
}
