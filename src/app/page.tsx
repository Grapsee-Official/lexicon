"use client";

import { LexoraHeader } from "@/components/lexora/header";
import { Hero } from "@/components/lexora/hero";
import { FloatingSearchBar } from "@/components/lexora/floating-search-bar";
import { TrendingTagRow } from "@/components/lexora/trending-tag-row";
import { WordOfTheDayCard } from "@/components/lexora/word-of-the-day-card";
import { CategoryCard } from "@/components/lexora/category-card";
import { ArticleCard } from "@/components/lexora/article-card";
import { SectionHeader } from "@/components/lexora/section-header";
import { BottomNav } from "@/components/lexora/bottom-nav";
import {
  trendingTags,
  wordOfTheDay,
  categories,
  articles,
} from "@/lib/lexora-data";

export default function Home() {
  return (
    <div className="min-h-screen-safe flex flex-col bg-background">
      <LexoraHeader />

      <main className="flex-1 flex flex-col gap-6 pb-32">
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
            <SectionHeader title="Explore by Category" actionLabel="View all" />
          </div>
          <div className="flex gap-3 overflow-x-auto no-scrollbar px-5">
            {categories.map((c) => (
              <CategoryCard key={c.id} category={c} />
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
            {articles.map((a) => (
              <ArticleCard key={a.id} article={a} />
            ))}
          </div>
        </section>
      </main>

      <BottomNav active="home" />

      <footer className="sr-only">
        <p>
          Lexora — Words. Wisdom. Worlds. A timeless dictionary for curious
          minds.
        </p>
      </footer>
    </div>
  );
}
