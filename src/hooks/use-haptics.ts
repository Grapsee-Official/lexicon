"use client";

import { useCallback } from "react";

type HapticPattern = "light" | "medium" | "heavy" | "success" | "error" | "warning";

const patterns: Record<HapticPattern, number | number[]> = {
  light: 10,
  medium: 20,
  heavy: 40,
  success: [10, 50, 10],
  error: [30, 30, 30],
  warning: [20, 30, 20],
};

/**
 * Trigger device haptic feedback.
 * Uses the Vibration API on Android and is silently ignored on iOS Safari
 * (which doesn't support the Vibration API — wrap any UI feedback with a
 * visual ripple/scale animation as fallback).
 */
export function useHaptics() {
  return useCallback((pattern: HapticPattern = "light") => {
    if (typeof window === "undefined") return;
    if (!("vibrate" in navigator)) return;
    try {
      navigator.vibrate(patterns[pattern]);
    } catch {
      /* no-op */
    }
  }, []);
}
