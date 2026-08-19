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
import { trendingTags, wordOfTheDay, categories, articles } from "@/lib/lexora-data";

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
    <div className="flex min-h-screen-safe flex-col bg-background">
      {/* Navigation — always rendered, manages its own state */}
      <LexoraHeader />

      <main className="flex flex-1 flex-col gap-4 pb-24">
        {/* Sub-header for non-home tabs */}
        {tabMeta[activeTab] ? (
          <div className="px-4 pb-1 pt-2">
            <motion.h1
              key={`title-${activeTab}`}
              initial={{ opacity: 0, y: 6 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.25 }}
              className="font-display text-xl font-semibold text-foreground"
            >
              {tabMeta[activeTab].title}
            </motion.h1>
            <p className="mt-0.5 font-body text-[11px] text-muted-foreground">
              {tabMeta[activeTab].subtitle}
            </p>
          </div>
        ) : null}

        <AnimatePresence mode="wait">
          {activeTab === "home" ? (
            <motion.div
              key="home"
              initial={{ opacity: 0, y: 6 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0, y: -6 }}
              transition={{ duration: 0.2 }}
              className="flex flex-col gap-4"
            >
              {/* Hero — headline + artwork (2-column grid, natural flow) */}
              <Hero />

              {/* Search bar — independent component below hero */}
              <div className="px-4">
                <FloatingSearchBar />
              </div>

              {/* Tag chips — independent component below search */}
              <div className="px-4">
                <TrendingTagRow tags={trendingTags} />
              </div>

              {/* Word of the Day */}
              <div className="px-4">
                <WordOfTheDayCard word={wordOfTheDay} />
              </div>

              {/* Categories — no divider */}
              <section className="flex flex-col gap-2 px-4">
                <SectionHeader
                  title="Explore by Category"
                  actionLabel="View all"
                />
                <div className="-mx-4 flex gap-2 overflow-x-auto px-4 no-scrollbar">
                  {categories.map((c, i) => (
                    <motion.div
                      key={c.id}
                      initial={{ opacity: 0, x: 8 }}
                      animate={{ opacity: 1, x: 0 }}
                      transition={{ delay: 0.04 * i, duration: 0.25 }}
                    >
                      <CategoryCard category={c} />
                    </motion.div>
                  ))}
                </div>
              </section>

              {/* Library — no divider. Grid with equal-width columns. */}
              <section className="flex flex-col gap-2 px-4">
                <SectionHeader
                  title="From the Library"
                  actionLabel="View all articles"
                />
                <div className="grid grid-cols-2 gap-2">
                  {articles.map((a, i) => (
                    <motion.div
                      key={a.id}
                      initial={{ opacity: 0, y: 8 }}
                      animate={{ opacity: 1, y: 0 }}
                      transition={{ delay: 0.04 * i, duration: 0.25 }}
                    >
                      <ArticleCard article={a} />
                    </motion.div>
                  ))}
                </div>
              </section>
            </motion.div>
          ) : null}

          {activeTab === "dictionary" ? (
            <motion.div
              key="dictionary"
              initial={{ opacity: 0, y: 6 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0, y: -6 }}
              transition={{ duration: 0.2 }}
              className="px-4"
            >
              <DictionaryScreen />
            </motion.div>
          ) : null}

          {activeTab === "collections" ? (
            <motion.div
              key="collections"
              initial={{ opacity: 0, y: 6 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0, y: -6 }}
              transition={{ duration: 0.2 }}
              className="px-4"
            >
              <CollectionsScreen />
            </motion.div>
          ) : null}

          {activeTab === "more" ? (
            <motion.div
              key="more"
              initial={{ opacity: 0, y: 6 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0, y: -6 }}
              transition={{ duration: 0.2 }}
              className="px-4"
            >
              <MoreScreen />
            </motion.div>
          ) : null}
        </AnimatePresence>
      </main>

      <BottomNav />

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
