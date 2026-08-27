//
//  LexiconData.swift
//  Lexicon
//
//  Mock data — straight translation of `src/lib/lexicon-data.ts`.
//  Contains all 18 dictionary entries, 4 categories, 3 articles,
//  trending tags, and the Word of the Day.
//

import Foundation

enum LexiconData {
    // MARK: - Trending tags
    static let trendingTags: [TrendingTag] = [
        TrendingTag(label: "Ephemeral",  wordId: "ephemeral"),
        TrendingTag(label: "Serendipity", wordId: "serendipity"),
        TrendingTag(label: "Limerence",  wordId: "limerence"),
        TrendingTag(label: "Ethereal",   wordId: "ethereal"),
    ]

    // MARK: - Word of the Day
    static let wordOfTheDay: WordEntry = WordEntry(
        id: "wanderlust",
        word: "Wanderlust",
        phonetic: "/ˈwɒndərˌlʌst/",
        partOfSpeech: .noun,
        definitions: [
            "A strong desire or urge to wander or travel and explore the world.",
            "An irresistible impulse to travel and explore, often without a fixed destination.",
        ],
        etymology: "Borrowed from German Wanderlust (literally 'hiking desire'), from wandern ('to hike, roam') + Lust ('desire, pleasure'). First attested in English c. 1902, the term captured the romantic German ideal of the Wandervogel youth movement.",
        synonyms: ["itchy feet", "restlessness", "travel bug", "nomadism"],
        antonyms: ["homebody", "stay-at-home"],
        examples: [
            "After years of corporate life, her wanderlust could no longer be ignored, and she sold everything to sail around the Mediterranean.",
            "The novel captures the wanderlust of a generation that refused to settle.",
        ],
        language: "English (borrowed from German)",
        illustration: "wotd-bg-light"
    )

    // MARK: - Master dictionary (18 entries, incl. WOTD)
    static let dictionary: [WordEntry] = [
        wordOfTheDay,
        WordEntry(
            id: "ephemeral",
            word: "Ephemeral",
            phonetic: "/ɪˈfɛm(ə)rəl/",
            partOfSpeech: .adjective,
            definitions: [
                "Lasting for a very short time; transitory.",
                "(Of a plant) withering and dying in a single season or after a brief flowering period.",
            ],
            etymology: "From Greek ephēmeros ('lasting only a day'), from epi- ('on') + hēmera ('day'). Entered English via Latin ephemerus in the late 16th century, originally used to describe fevers that lasted a single day.",
            synonyms: ["transient", "fleeting", "momentary", "short-lived", "evanescent"],
            antonyms: ["permanent", "enduring", "perpetual"],
            examples: [
                "The ephemeral beauty of cherry blossoms is what makes them so precious.",
                "Social media stories are the perfect emblem of ephemeral culture.",
            ],
            language: "English (from Greek)"
        ),
        WordEntry(
            id: "serendipity",
            word: "Serendipity",
            phonetic: "/ˌsɛr(ə)nˈdɪpɪti/",
            partOfSpeech: .noun,
            definitions: [
                "The occurrence and development of events by chance in a happy or beneficial way.",
                "The faculty of making fortunate discoveries by accident.",
            ],
            etymology: "Coined in 1754 by Horace Walpole, after the Persian fairy tale 'The Three Princes of Serendip' (Serendip being an old name for Sri Lanka), whose heroes were always making discoveries by accident and sagacity.",
            synonyms: ["chance", "fortune", "kismet", "providence", "fluke"],
            antonyms: ["misfortune", "design", "intention"],
            examples: [
                "Their meeting in a Tokyo bookshop was pure serendipity.",
                "Many scientific breakthroughs — penicillin, X-rays, Teflon — are products of serendipity.",
            ],
            language: "English (coined)"
        ),
        WordEntry(
            id: "limerence",
            word: "Limerence",
            phonetic: "/ˈlɪmərəns/",
            partOfSpeech: .noun,
            definitions: [
                "The state of being infatuated or obsessed with another person, typically involuntarily and characterised by a strong desire for reciprocation.",
            ],
            etymology: "Coined in 1977 by psychologist Dorothy Tennov, who wanted a neutral term distinct from 'love' or 'infatuation'. She derived it from the nonce verb 'to limer', which she invented to capture the obsessive quality of romantic longing.",
            synonyms: ["infatuation", "obsession", "crush", "passionate attachment"],
            antonyms: ["indifference", "detachment", "apathy"],
            examples: [
                "Limerence, unlike mature love, hinges on uncertainty about the other person's feelings.",
                "She mistook her limerence for love, only to lose interest the moment her feelings were returned.",
            ],
            language: "English (coined)"
        ),
        WordEntry(
            id: "ethereal",
            word: "Ethereal",
            phonetic: "/ɪˈθɪərɪəl/",
            partOfSpeech: .adjective,
            definitions: [
                "Extremely delicate and light in a way that seems almost too perfect for this world.",
                "Of or relating to the celestial regions; heavenly.",
                "(Chemistry, historical) of or containing a volatile ether.",
            ],
            etymology: "From Latin aetherius ('heavenly, of the upper air'), from Greek aitherios, from aithēr ('upper air, pure air'). Entered English in the early 16th century, originally in alchemical and astronomical contexts before evolving its modern delicate sense.",
            synonyms: ["delicate", "airy", "insubstantial", "celestial", "otherworldly"],
            antonyms: ["heavy", "solid", "earthly", "coarse"],
            examples: [
                "Her ethereal voice seemed to come from somewhere beyond the room.",
                "Morning mist gave the valley an ethereal, almost dreamlike quality.",
            ],
            language: "English (from Latin/Greek)"
        ),
        WordEntry(
            id: "petrichor",
            word: "Petrichor",
            phonetic: "/ˈpɛtrɪkɔː/",
            partOfSpeech: .noun,
            definitions: [
                "The pleasant, earthy smell produced when rain falls on dry soil, especially after a prolonged dry spell.",
            ],
            etymology: "Coined in 1964 by Australian geologists Isabel Joy Bear and Richard Grenfell Thomas, from Greek petros ('stone') + ichor ('the fluid that flows in the veins of the gods' in Greek mythology). A word invented by science to name an ancient sensory experience.",
            synonyms: ["rain-smell", "earth-scent"],
            antonyms: [],
            examples: [
                "The first monsoon rain released a wave of petrichor that made everyone pause and breathe in.",
                "Petrichor is produced by a compound called geosmin, released by soil bacteria.",
            ],
            language: "English (scientific coinage)"
        ),
        WordEntry(
            id: "saudade",
            word: "Saudade",
            phonetic: "/sɔːˈdɑːdə/",
            partOfSpeech: .noun,
            definitions: [
                "(Portuguese) A deep emotional state of nostalgic or profound melancholic longing for something or someone absent, with the repressed knowledge that the longed-for object may never return.",
            ],
            etymology: "Portuguese, of uncertain origin. Some scholars link it to the Latin solitas ('solitude'), others to the Arabic saudāʾ ('black bile', melancholy). It is a cornerstone of Portuguese and Brazilian culture, central to fado music.",
            synonyms: ["longing", "nostalgia", "melancholy", "yearning"],
            antonyms: ["contentment", "fulfilment"],
            examples: [
                "Fado singers give voice to saudade, the bittersweet ache of longing for what cannot return.",
                "She felt a deep saudade for the village of her childhood, knowing it no longer existed as she remembered it.",
            ],
            language: "Portuguese"
        ),
        WordEntry(
            id: "hygge",
            word: "Hygge",
            phonetic: "/ˈh(j)uːɡə/",
            partOfSpeech: .noun,
            definitions: [
                "(Danish) A quality of cosiness and comfortable conviviality that engenders a feeling of contentment or well-being; regarded as a defining characteristic of Danish culture.",
            ],
            etymology: "Danish, from Norwegian hygga ('to comfort, console'), related to Old Norse hygga ('to think'). The concept became a cultural touchstone in Denmark in the 20th century and was exported globally as a lifestyle trend in the 2010s.",
            synonyms: ["cosiness", "comfort", "warmth", "conviviality"],
            antonyms: [],
            examples: [
                "Candlelight, woollen blankets, and good company — the essence of hygge.",
                "Winter in Copenhagen is endured through deliberate, ritualised hygge.",
            ],
            language: "Danish"
        ),
        WordEntry(
            id: "ineffable",
            word: "Ineffable",
            phonetic: "/ɪnˈɛfəb(ə)l/",
            partOfSpeech: .adjective,
            definitions: [
                "Too great or extreme to be expressed or described in words.",
            ],
            etymology: "From Latin ineffabilis, from in- ('not') + effari ('to speak out'), from ex- ('out') + fari ('to speak'). Has been used in theology for centuries to describe that which is beyond human capacity to articulate, particularly the divine.",
            synonyms: ["indescribable", "inexpressible", "unutterable", "beyond words"],
            antonyms: ["expressible", "describable", "utterable"],
            examples: [
                "He experienced an ineffable sense of peace watching the sun rise over the Himalayas.",
            ],
            language: "English (from Latin)"
        ),
        WordEntry(
            id: "sonder",
            word: "Sonder",
            phonetic: "/ˈsɒndə/",
            partOfSpeech: .noun,
            definitions: [
                "(Neologism) The realisation that each random passerby is living a life as vivid and complex as your own — populated with their own ambitions, friends, routines, and inherited worries.",
            ],
            etymology: "Coined by John Koenig in 'The Dictionary of Obscure Sorrows' (2009), derived from French sonder ('to probe, sound, fathom') and the Old French sonde ('plumb line'). Part of a contemporary project to name previously unarticulated emotions.",
            synonyms: ["empathic realisation", "epiphany of shared humanity"],
            antonyms: [],
            examples: [
                "Stuck in traffic, a sudden attack of sonder made him wonder about the lives of every driver around him.",
            ],
            language: "English (neologism)"
        ),
        WordEntry(
            id: "querencia",
            word: "Querencia",
            phonetic: "/kɛˈrɛnsɪə/",
            partOfSpeech: .noun,
            definitions: [
                "(Spanish) A place where one feels safe; a haven, refuge, or home. In bullfighting, the spot in the ring where the wounded bull returns to gather strength.",
            ],
            etymology: "Spanish, from querer ('to want, to love'), from Latin quaerere ('to seek'). The word captures both the seeking and the finding — a place one chooses instinctively, where the self feels anchored.",
            synonyms: ["haven", "sanctuary", "refuge", "safe place"],
            antonyms: ["threat", "danger zone"],
            examples: [
                "The little stone cottage in the Cotswolds became her querencia after years of city life.",
            ],
            language: "Spanish"
        ),
        WordEntry(
            id: "komorebi",
            word: "Komorebi",
            phonetic: "/kɔmɔˈɹɛbi/",
            partOfSpeech: .noun,
            definitions: [
                "(Japanese) The interplay of sunlight as it filters through the leaves of trees; the dappled light and shadow patterns this creates on the ground below.",
            ],
            etymology: "Japanese 木漏れ日, from 木 (ko, 'tree') + 漏れ (more, 'leaking, filtering') + 日 (bi, 'sun, light'). One of many untranslatable Japanese compound nouns that name precise natural phenomena, reflecting the culture's deep aesthetic attention to the natural world.",
            synonyms: ["sunlight through leaves", "dappled light"],
            antonyms: [],
            examples: [
                "The path through the ancient oak wood was bathed in shifting komorebi.",
            ],
            language: "Japanese"
        ),
        WordEntry(
            id: "mellifluous",
            word: "Mellifluous",
            phonetic: "/mɪˈlɪflʊəs/",
            partOfSpeech: .adjective,
            definitions: [
                "(Of a voice or music) pleasingly smooth and musical to hear; sweet-sounding.",
            ],
            etymology: "From Late Latin mellifluus, from mel ('honey') + fluere ('to flow'). Literally 'flowing with honey'. Entered English in the 15th century.",
            synonyms: ["sweet-sounding", "honeyed", "dulcet", "mellow", "euphonious"],
            antonyms: ["harsh", "grating", "discordant"],
            examples: [
                "Her mellifluous voice made even the driest lecture feel like a lullaby.",
            ],
            language: "English (from Latin)"
        ),
        WordEntry(
            id: "halcyon",
            word: "Halcyon",
            phonetic: "/ˈhalsɪən/",
            partOfSpeech: .adjective,
            definitions: [
                "Denoting a period of time in the past that was idyllically happy and peaceful.",
                "(Of weather) calm, peaceful, tranquil.",
            ],
            etymology: "From Greek halkyōn, originally referring to the kingfisher bird. In Greek mythology, Alcyone threw herself into the sea after her husband drowned; the gods took pity and turned them both into kingfishers. Aeolus, god of winds, calmed the seas for fourteen days each winter so the birds could nest — the 'halcyon days'.",
            synonyms: ["golden", "idyllic", "peaceful", "tranquil", "serene"],
            antonyms: ["turbulent", "chaotic", "stormy"],
            examples: [
                "She often thought back to the halcyon summers of her childhood, spent barefoot in the meadows.",
            ],
            language: "English (from Greek)"
        ),
        WordEntry(
            id: "redolent",
            word: "Redolent",
            phonetic: "/ˈrɛdələnt/",
            partOfSpeech: .adjective,
            definitions: [
                "Strongly reminiscent or suggestive of something.",
                "Fragrant; sweet-smelling.",
            ],
            etymology: "From Latin redolens, redolent-, present participle of redolere ('to emit a smell'), from re- ('back') + olere ('to smell'). Has evolved from purely olfactory to figurative usage, as in 'redolent of memory'.",
            synonyms: ["fragrant", "aromatic", "evocative", "reminiscent"],
            antonyms: ["odourless", "unscented"],
            examples: [
                "The attic was redolent with the scent of old paper and forgotten summers.",
            ],
            language: "English (from Latin)"
        ),
        WordEntry(
            id: "susurrus",
            word: "Susurrus",
            phonetic: "/sʊˈsʌrəs/",
            partOfSpeech: .noun,
            definitions: [
                "Whispering, murmuring, or a low rustling sound; the soft noise of leaves, wind, or distant conversation.",
            ],
            etymology: "Latin susurrus ('a whisper, murmur'), from susurrare ('to whisper'), an onomatopoeic word imitating the sound itself. A rare example where the word sounds exactly like what it describes.",
            synonyms: ["whisper", "murmur", "rustle", "sough"],
            antonyms: [],
            examples: [
                "The susurrus of the river over stones was the only sound in the valley.",
            ],
            language: "English (from Latin)"
        ),
        WordEntry(
            id: "numinous",
            word: "Numinous",
            phonetic: "/ˈnjuːmɪnəs/",
            partOfSpeech: .adjective,
            definitions: [
                "Having a strong religious or spiritual quality; indicating the presence of a divinity.",
                "Awe-inspiring, mysterious, and arousing elevated or spiritual feeling.",
            ],
            etymology: "Coined in 1917 by German theologian Rudolf Otto in 'The Idea of the Holy', from Latin numen ('divine power, deity'). Otto used it to describe the non-rational, awe-tinged, mysterium tremendum et fascinans of encounters with the divine.",
            synonyms: ["spiritual", "sacred", "transcendent", "mystical", "divine"],
            antonyms: ["profane", "secular", "mundane"],
            examples: [
                "The standing stones at dawn had a numinous quality that silenced all speech.",
            ],
            language: "English (theological coinage)"
        ),
        WordEntry(
            id: "lissom",
            word: "Lissom",
            phonetic: "/ˈlɪs(ə)m/",
            partOfSpeech: .adjective,
            definitions: [
                "(Of a person or body) supple, agile, and graceful in movement.",
            ],
            etymology: "From Middle English lithesome, from lithe ('supple') + -some ('characterised by'). The 'lissom' spelling emerged in the 18th century, perhaps influenced by 'liss' ('soft, smooth'), an obsolete word of obscure origin.",
            synonyms: ["supple", "limber", "lithe", "agile", "graceful"],
            antonyms: ["stiff", "rigid", "clumsy"],
            examples: [
                "The dancer's lissom frame moved as if boneless across the stage.",
            ],
            language: "English"
        ),
        WordEntry(
            id: "vellichor",
            word: "Vellichor",
            phonetic: "/ˈvɛlɪkɔː/",
            partOfSpeech: .noun,
            definitions: [
                "(Neologism) The strange wistfulness of used bookstores, which are somehow infused with the passage of time.",
            ],
            etymology: "Coined by John Koenig in 'The Dictionary of Obscure Sorrows'. Combines the Latin vellum ('parchment') with the suffix -ichor, evoking the divine, fluid quality of time itself.",
            synonyms: ["bookstore melancholy", "temporal wistfulness"],
            antonyms: [],
            examples: [
                "He wandered the dusty shelves of the second-hand bookshop, surrendering to its vellichor.",
            ],
            language: "English (neologism)"
        ),
    ]

    // MARK: - Categories
    static let categories: [LexiconCategory] = [
        LexiconCategory(id: "vocabulary", title: "Vocabulary", subtitle: "Expand your words",     illustration: "cat-vocabulary"),
        LexiconCategory(id: "geography",  title: "Geography",  subtitle: "Places & names",         illustration: "cat-geography"),
        LexiconCategory(id: "literature", title: "Literature", subtitle: "Terms & references",     illustration: "cat-literature"),
        LexiconCategory(id: "science",    title: "Science",    subtitle: "Concepts & terms",       illustration: "cat-science"),
    ]

    // MARK: - Articles
    static let articles: [Article] = [
        Article(
            id: "a1",
            title: "The Fascinating Origin of Everyday Words",
            excerpt: "From 'sandwich' to 'boycott', the surprising stories behind words named after people.",
            readMinutes: 5,
            thumbnail: "article-1"
        ),
        Article(
            id: "a2",
            title: "Lost Words from the Past",
            excerpt: "Beautiful English words that have fallen out of use — and deserve a revival.",
            readMinutes: 4,
            thumbnail: "article-2"
        ),
        Article(
            id: "a3",
            title: "The Language of Nature",
            excerpt: "How cultures around the world have named the natural world — and what those names reveal.",
            readMinutes: 6,
            thumbnail: "article-3"
        ),
    ]

    // MARK: - Helper functions (mirrors src/lib/lexicon-data.ts)

    /// Lookup a word entry by id (used by trending tags, search, etc.).
    static func getWordById(_ id: String) -> WordEntry? {
        dictionary.first(where: { $0.id == id })
    }

    /// Search the dictionary by word text (case-insensitive prefix or substring match,
    /// also matches synonyms). Mirrors `searchWords`.
    static func searchWords(_ query: String, limit: Int = 10) -> [WordEntry] {
        let q = query.trimmingCharacters(in: .whitespacesAndNewlines).lowercased()
        guard !q.isEmpty else { return [] }
        return dictionary.filter { entry in
            let w = entry.word.lowercased()
            let matchPrefix = w.hasPrefix(q)
            let matchContains = w.contains(q)
            let matchSynonym = entry.synonyms.contains(where: { $0.lowercased().contains(q) })
            return matchPrefix || matchContains || matchSynonym
        }
        .prefix(limit)
        .map { $0 }
    }

    /// Get the alphabet letters that have entries — for the Dictionary screen index.
    static func getAlphabetSections() -> [(letter: String, entries: [WordEntry])] {
        let grouped = Dictionary(grouping: dictionary) { entry in
            String(entry.word.prefix(1)).uppercased()
        }
        return grouped
            .keys
            .sorted()
            .map { letter in
                (letter: letter,
                 entries: (grouped[letter] ?? []).sorted(by: { $0.word < $1.word }))
            }
    }
}
