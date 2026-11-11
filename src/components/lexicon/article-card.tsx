"use client";

import { Clock, Bookmark } from "lucide-react";
import { useHaptics } from "@/hooks/use-haptics";
import { cn } from "@/lib/utils";
import type { Article } from "@/lib/lexicon-data";

export function ArticleCard({
  article,
  className,
}: {
  article: Article;
  className?: string;
}) {
  const haptic = useHaptics();
  const darkThumb = article.thumbnail.replace(/\.png$/, "-dark.png");

  return (
    <button
      onClick={() => haptic("light")}
      className={cn(
        "flex h-[112px] w-[236px] shrink-0 flex-row overflow-hidden rounded-[14px] border border-[#31462F]/10 dark:border-[#9EA6B2]/16 bg-[#FAF4E8] dark:bg-[#141B25] text-left shadow-card transition-transform active:scale-95",
        className
      )}
    >
      <div className="h-full w-[36%] shrink-0 p-1.5">
        <div className="h-full w-full overflow-hidden rounded-[10px]">
          <img
            src={article.thumbnail}
            alt=""
            aria-hidden
            className="h-full w-full object-cover dark:hidden"
            loading="lazy"
          />
          <img
            src={darkThumb}
            alt=""
            aria-hidden
            className="hidden h-full w-full object-cover dark:block"
            loading="lazy"
          />
        </div>
      </div>

      <div className="flex w-[64%] flex-col justify-between py-3 pr-3 pl-2">
        <h4 className="font-display text-[13.5px] font-normal leading-snug text-foreground line-clamp-3">
          {article.title}
        </h4>
        <div className="flex items-center justify-between text-muted-foreground">
          <span className="inline-flex items-center gap-1 font-body text-[10px]">
            <Clock className="h-3 w-3 text-[#A58A61] dark:text-[#D4A45A]" strokeWidth={1.6} />
            {article.readMinutes} min read
          </span>
          <Bookmark
            className="h-3.5 w-3.5 text-[#A58A61] dark:text-[#D4A45A]"
            strokeWidth={1.5}
            fill={article.bookmarked ? "currentColor" : "none"}
          />
        </div>
      </div>
    </button>
  );
}
