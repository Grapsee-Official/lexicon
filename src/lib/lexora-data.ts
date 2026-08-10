export type Category = {
  id: string;
  title: string;
  subtitle: string;
  illustration: string;
};

export type Article = {
  id: string;
  title: string;
  readMinutes: number;
  thumbnail: string;
  bookmarked?: boolean;
};

export type WordOfTheDay = {
  word: string;
  phonetic: string;
  partOfSpeech: string;
  definition: string;
};

export type TrendingTag = {
  label: string;
};

export const trendingTags: TrendingTag[] = [
  { label: "Ephemeral" },
  { label: "Serendipity" },
  { label: "Limerence" },
  { label: "Ethereal" },
];

export const wordOfTheDay: WordOfTheDay = {
  word: "Wanderlust",
  phonetic: "/ˈwɒndərˌlʌst/",
  partOfSpeech: "noun",
  definition:
    "A strong desire or urge to wander or travel and explore the world.",
};

export const categories: Category[] = [
  {
    id: "vocabulary",
    title: "Vocabulary",
    subtitle: "Expand your words",
    illustration: "/illustrations/cat-vocabulary.png",
  },
  {
    id: "geography",
    title: "Geography",
    subtitle: "Places & names",
    illustration: "/illustrations/cat-geography.png",
  },
  {
    id: "literature",
    title: "Literature",
    subtitle: "Terms & references",
    illustration: "/illustrations/cat-literature.png",
  },
  {
    id: "science",
    title: "Science",
    subtitle: "Concepts & terms",
    illustration: "/illustrations/cat-science.png",
  },
];

export const articles: Article[] = [
  {
    id: "a1",
    title: "The Fascinating Origin of Everyday Words",
    readMinutes: 5,
    thumbnail: "/illustrations/article-1.png",
  },
  {
    id: "a2",
    title: "Lost Words from the Past",
    readMinutes: 4,
    thumbnail: "/illustrations/article-2.png",
  },
  {
    id: "a3",
    title: "The Language of Nature",
    readMinutes: 6,
    thumbnail: "/illustrations/article-3.png",
  },
];
