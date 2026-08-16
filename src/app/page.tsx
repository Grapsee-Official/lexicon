"use client";

import { motion, AnimatePresence } from "framer-motion";
import { LexoraHeader } from "@/components/lexora/header";
import LexoraHero from "@/components/lexora/LexoraHero";
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
import { wordOfTheDay, categories, articles } from "@/lib/lexora-data";

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
      {/* Home tab uses the self-contained LexoraHero (nav + hero + search + chips).
          Other tabs use the slim LexoraHeader. */}
      {activeTab === "home" ? null : <LexoraHeader />}

      <main className="flex-1 flex flex-col gap-4 pb-24">
        {tabMeta[activeTab] ? (
          <div className="pt-2 pb-1 px-5">
            <motion.h1
              key={`title-${activeTab}`}
              initial={{ opacity: 0, y: 6 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.25 }}
              className="font-display text-xl font-semibold text-foreground"
            >
              {tabMeta[activeTab].title}
            </motion.h1>
            <p className="font-body text-[11px] text-muted-foreground mt-0.5">
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
              {/* Self-contained Nav + Hero + Search + Chips per spec */}
              <LexoraHero />

              {/* Word of the Day */}
              <div className="px-5">
                <WordOfTheDayCard word={wordOfTheDay} />
              </div>

              {/* Categories — no divider */}
              <section className="flex flex-col gap-2 px-5">
                <SectionHeader
                  title="Explore by Category"
                  actionLabel="View all"
                />
                <div className="flex gap-2 overflow-x-auto no-scrollbar -mx-5 px-5">
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

              {/* Library — WITH divider line */}
              <section className="flex flex-col gap-2 px-5">
                <SectionHeader
                  title="From the Library"
                  actionLabel="View all articles"
                  divider
                />
                <div className="flex gap-2 overflow-x-auto no-scrollbar -mx-5 px-5">
                  {articles.map((a, i) => (
                    <motion.div
                      key={a.id}
                      initial={{ opacity: 0, x: 8 }}
                      animate={{ opacity: 1, x: 0 }}
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
              className="px-5"
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
              className="px-5"
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
              className="px-5"
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
