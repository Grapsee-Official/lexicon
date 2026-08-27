import type { Metadata, Viewport } from "next";
import { Fraunces, Inter, Lora, Geist_Mono } from "next/font/google";
import "./globals.css";
import { Toaster } from "@/components/ui/toaster";
import { ThemeProvider } from "@/components/theme-provider";
import { Toaster as SonnerToaster } from "@/components/ui/sonner";

const fraunces = Fraunces({
  variable: "--font-fraunces",
  subsets: ["latin"],
  display: "swap",
  axes: ["SOFT", "WONK", "opsz"],
});

const inter = Inter({
  variable: "--font-inter",
  subsets: ["latin"],
  display: "swap",
});

const lora = Lora({
  variable: "--font-lora",
  subsets: ["latin"],
  display: "swap",
});

const geistMono = Geist_Mono({
  variable: "--font-geist-mono",
  subsets: ["latin"],
  display: "swap",
});

export const metadata: Metadata = {
  title: "Lexicon — Words. Wisdom. Worlds.",
  description:
    "A timeless dictionary crafted for curious minds. Explore meanings, origins, and stories behind every word. Offline-first, system-wide on iOS & Android.",
  applicationName: "Lexicon",
  manifest: "/manifest.json",
  icons: {
    icon: [
      { url: "/favicon.png", type: "image/png" },
      { url: "/icon-192.png", sizes: "192x192", type: "image/png" },
      { url: "/icon-512.png", sizes: "512x512", type: "image/png" },
    ],
    apple: [{ url: "/apple-touch-icon.png", sizes: "180x180" }],
  },
  appleWebApp: {
    capable: true,
    statusBarStyle: "black-translucent",
    title: "Lexicon",
  },
  formatDetection: { telephone: false },
  keywords: [
    "Lexicon",
    "dictionary",
    "multilingual",
    "offline",
    "language",
    "etymology",
    "translation",
  ],
};

export const viewport: Viewport = {
  themeColor: [
    { media: "(prefers-color-scheme: light)", color: "#F3EBDD" },
    { media: "(prefers-color-scheme: dark)", color: "#1A1F28" },
  ],
  width: "device-width",
  initialScale: 1,
  maximumScale: 1,
  userScalable: false,
  viewportFit: "cover",
};

export default function RootLayout({
  children,
}: Readonly<{ children: React.ReactNode }>) {
  return (
    <html lang="en" suppressHydrationWarning>
      <body
        className={`${fraunces.variable} ${inter.variable} ${lora.variable} ${geistMono.variable} antialiased bg-background text-foreground font-body`}
      >
        <ThemeProvider>
          {children}
          <Toaster />
          <SonnerToaster position="top-center" />
        </ThemeProvider>
      </body>
    </html>
  );
}
