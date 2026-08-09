"use client";

import { motion } from "framer-motion";
import { Smartphone, CheckCircle2, ArrowRight } from "lucide-react";
import { useHaptics } from "@/hooks/use-haptics";

export default function Home() {
  const haptic = useHaptics();

  return (
    <main className="min-h-screen-safe flex flex-col">
      <section className="flex-1 flex flex-col items-center justify-center px-6 text-center">
        <motion.div
          initial={{ opacity: 0, scale: 0.85 }}
          animate={{ opacity: 1, scale: 1 }}
          transition={{ type: "spring", stiffness: 220, damping: 18 }}
          className="relative mb-8"
        >
          <div className="absolute inset-0 rounded-3xl bg-primary/20 blur-2xl" />
          <div className="relative w-20 h-20 rounded-3xl bg-primary text-primary-foreground grid place-items-center shadow-lg">
            <Smartphone className="w-10 h-10" />
          </div>
        </motion.div>

        <motion.h1
          initial={{ opacity: 0, y: 12 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.1 }}
          className="text-2xl font-semibold tracking-tight"
        >
          Environment is ready
        </motion.h1>
        <motion.p
          initial={{ opacity: 0, y: 12 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.2 }}
          className="mt-2 text-sm text-muted-foreground max-w-xs"
        >
          Mobile-first PWA scaffold is live. Safe areas, dark mode, haptics,
          and touch optimizations are all wired up.
        </motion.p>

        <motion.ul
          initial={{ opacity: 0, y: 12 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.3 }}
          className="mt-8 w-full max-w-xs space-y-2 text-left"
        >
          {[
            "PWA manifest (installable to home screen)",
            "iOS safe-area + notch handling",
            "Dark / light theme auto-switching",
            "Touch-friendly: no tap highlight, no double-tap zoom",
            "Haptic feedback hook ready",
          ].map((item) => (
            <li
              key={item}
              className="flex items-start gap-3 text-sm text-muted-foreground"
            >
              <CheckCircle2 className="w-4 h-4 mt-0.5 text-emerald-500 shrink-0" />
              <span>{item}</span>
            </li>
          ))}
        </motion.ul>

        <motion.div
          initial={{ opacity: 0, y: 12 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.4 }}
          className="mt-10 inline-flex items-center gap-2 text-sm font-medium text-primary"
          onClick={() => haptic("light")}
        >
          Awaiting your app idea
          <ArrowRight className="w-4 h-4" />
        </motion.div>
      </section>

      <footer className="mt-auto px-6 pb-safe py-4 text-center text-xs text-muted-foreground">
        Phase 1 · Web prototype setup
      </footer>
    </main>
  );
}
