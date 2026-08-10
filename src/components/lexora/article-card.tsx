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
        "shrink-0 w-56 flex gap-2.5 p-2.5 rounded-xl bg-card border border-border shadow-card text-left active:scale-95 transition-transform",
        className
      )}
    >
      <div className="w-16 h-16 rounded-lg overflow-hidden bg-background/40 shrink-0">
        <img
          src={article.thumbnail}
          alt=""
          aria-hidden
          className="w-full h-full object-cover"
          loading="lazy"
        />
      </div>
      <div className="flex flex-col justify-between flex-1 min-w-0 py-0.5">
        <h4 className="font-display text-[13px] font-semibold text-foreground leading-snug line-clamp-3">
          {article.title}
        </h4>
        <div className="flex items-center justify-between text-muted-foreground">
          <span className="inline-flex items-center gap-1 text-[10px] font-body">
            <Clock className="w-2.5 h-2.5" strokeWidth={1.75} />
            {article.readMinutes} min read
          </span>
          <Bookmark
            className="w-3 h-3"
            strokeWidth={1.75}
            fill={article.bookmarked ? "currentColor" : "none"}
          />
        </div>
      </div>
    </button>
  );
}
