"use client";

import { Clock, Bookmark } from "lucide-react";
import { useHaptics } from "@/hooks/use-haptics";
import { cn } from "@/lib/utils";
import type { Article } from "@/lib/lexora-data";

export function ArticleCard({
  article,
  className,
}: {
  article: Article;
  className?: string;
}) {
  const haptic = useHaptics();

  return (
    <button
      onClick={() => haptic("light")}
      className={cn(
        "flex w-40 shrink-0 flex-col gap-2 rounded-2xl bg-card p-2.5 text-left shadow-card transition-transform active:scale-95",
        className
      )}
    >
      {/* Vertical image on top */}
      <div className="aspect-[4/3] w-full overflow-hidden rounded-xl">
        <img
          src={article.thumbnail}
          alt=""
          aria-hidden
          className="h-full w-full object-cover"
          loading="lazy"
        />
      </div>
      {/* Text content stacked below image */}
      <div className="flex flex-col gap-1.5">
        <h4 className="font-display text-[12px] font-semibold leading-snug text-foreground line-clamp-2">
          {article.title}
        </h4>
        <div className="flex items-center justify-between text-muted-foreground">
          <span className="inline-flex items-center gap-1 font-body text-[9px]">
            <Clock className="h-2.5 w-2.5" strokeWidth={1.5} />
            {article.readMinutes} min read
          </span>
          <Bookmark
            className="h-3 w-3"
            strokeWidth={1.5}
            fill={article.bookmarked ? "currentColor" : "none"}
          />
        </div>
      </div>
    </button>
  );
}
