"use client";

import React from "react";
import {
  Bell,
  Camera,
  Grid2x2,
  Menu,
  Mic,
  Moon,
  Sun,
  Search,
} from "lucide-react";
import { useTheme } from "next-themes";
import "./LexoraHero.css";

const popularWords = [
  "Ephemeral",
  "Serendipity",
  "Limerence",
  "Ethereal",
];

export default function LexoraHero() {
  const { resolvedTheme } = useTheme();
  const darkMode = resolvedTheme === "dark";

  return (
    <section
      className={`lexora-hero ${darkMode ? "lexora-hero--dark" : ""}`}
      aria-label="Lexora home hero"
    >
      {/* =========================================================
          NAVIGATION
         ========================================================= */}
      <header className="lexora-nav">
        <button className="lexora-icon-button" aria-label="Open menu">
          <Menu size={26} strokeWidth={1.6} />
        </button>

        <div className="lexora-brand">
          <div className="lexora-brand-mark">
            <img
              src="/assets/lexora-mark.svg"
              alt=""
              draggable={false}
            />
          </div>

          <div className="lexora-brand-copy">
            <span className="lexora-brand-name">Lexora</span>
            <span className="lexora-brand-tagline">
              Words. Wisdom. Worlds.
            </span>
          </div>
        </div>

        <div className="lexora-nav-actions">
          <button
            className="lexora-icon-button"
            aria-label="Toggle appearance"
          >
            {darkMode ? (
              <Moon size={24} strokeWidth={1.6} />
            ) : (
              <Sun size={24} strokeWidth={1.6} />
            )}
          </button>

          <button
            className="lexora-icon-button lexora-notification"
            aria-label="Notifications"
          >
            <Bell size={24} strokeWidth={1.6} />
            <span className="lexora-notification-dot" />
          </button>

          <button
            className="lexora-profile"
            aria-label="Open profile"
          >
            <img
              src="/illustrations/avatar-default.png"
              alt="Profile"
              draggable={false}
            />
          </button>
        </div>
      </header>

      {/* =========================================================
          HERO CONTENT
         ========================================================= */}
      <div className="lexora-hero-inner">

        {/* Left editorial/content column */}
        <div className="lexora-content">

          <div className="lexora-copy">
            <h1>
              Discover the
              <br />
              World of{" "}
              <span>
                Words
                <svg
                  className="lexora-leaf"
                  viewBox="0 0 40 40"
                  aria-hidden="true"
                >
                  {/* Delicate 3-leaf botanical sprig.
                      Uses <ellipse>/<line> (not <path>) so it isn't hidden
                      by the Pass 03B rule `.lexora-leaf path { display: none }`. */}
                  <line
                    x1="20"
                    y1="36"
                    x2="20"
                    y2="6"
                    stroke="currentColor"
                    strokeWidth="1.1"
                    strokeLinecap="round"
                  />
                  <ellipse
                    cx="12"
                    cy="23"
                    rx="7"
                    ry="3.4"
                    fill="currentColor"
                    opacity="0.82"
                    transform="rotate(-32 12 23)"
                  />
                  <ellipse
                    cx="28"
                    cy="16"
                    rx="7"
                    ry="3.4"
                    fill="currentColor"
                    opacity="0.82"
                    transform="rotate(32 28 16)"
                  />
                  <ellipse
                    cx="20"
                    cy="8"
                    rx="4.2"
                    ry="2.6"
                    fill="currentColor"
                    opacity="0.9"
                  />
                </svg>
              </span>
            </h1>

            <p>
              A timeless dictionary crafted for curious minds.
              Explore meanings, origins, and stories behind every
              word.
            </p>
          </div>

          {/* Search */}
          <div className="lexora-search-shell">
            <Search
              className="lexora-search-icon"
              size={23}
              strokeWidth={1.7}
            />

            <input
              type="search"
              placeholder="Search for a word..."
              aria-label="Search for a word"
            />

            <div className="lexora-search-actions">
              <button aria-label="Search with camera">
                <Camera size={21} strokeWidth={1.6} />
              </button>

              <button aria-label="Search with microphone">
                <Mic size={21} strokeWidth={1.6} />
              </button>
            </div>
          </div>

          {/* Popular search chips */}
          <div className="lexora-popular">
            {popularWords.map((word) => (
              <button
                key={word}
                className="lexora-chip"
              >
                {word}
              </button>
            ))}

            <button
              className="lexora-chip lexora-chip--grid"
              aria-label="More searches"
            >
              <Grid2x2 size={17} strokeWidth={1.5} />
            </button>
          </div>
        </div>

        {/* =======================================================
            RIGHT-SIDE ILLUSTRATION
            The SVG asset has GENUINE organic watercolor transparent edges
            (verified via alpha channel inspection). NO CSS gradient mask
            is applied — the real edges show.
           ======================================================= */}
        <div className="lexora-art" aria-hidden="true">
          <img
            src="/assets/lexora-countryside-day.svg"
            alt=""
            draggable={false}
          />
        </div>
      </div>
    </section>
  );
}
