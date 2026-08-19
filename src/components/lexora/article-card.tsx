"use client";

import { Clock, Bookmark } from "lucide-react";
import { useHaptics } from "@/hooks/use-haptics";
import { cn } from "@/lib/utils";
import type { Article } from "@/lib/lexora-data";

/**
 * ArticleCard — Horizontal layout, uniform compact sizing.
 * Image on LEFT (w-[35%]), text on RIGHT (w-[65%]).
 * Strict fixed height h-[120px]. All cards identical size.
 */
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
        "flex h-[120px] w-56 shrink-0 flex-row overflow-hidden rounded-2xl bg-card text-left shadow-card transition-transform active:scale-95",
        className
      )}
    >
      {/* LEFT: Image — w-[35%] h-full, object-cover fills perfectly */}
      <div className="h-full w-[35%] shrink-0 overflow-hidden">
        <img
          src={article.thumbnail}
          alt=""
          aria-hidden
          className="h-full w-full object-cover"
          loading="lazy"
        />
      </div>

      {/* RIGHT: Text — w-[65%] flex flex-col justify-between p-4 */}
      <div className="flex w-[65%] flex-col justify-between p-4">
        <h4 className="font-display text-[12px] font-semibold leading-snug text-foreground line-clamp-3">
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
