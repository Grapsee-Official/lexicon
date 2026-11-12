//
//  LexiconData.swift
//  Lexicon
//

import Foundation

enum LexiconData {
    static let trendingTags: [TrendingTag] = [
        TrendingTag(label: "Ephemeral", wordId: "ephemeral"),
        TrendingTag(label: "Serendipity", wordId: "serendipity"),
        TrendingTag(label: "Limerence", wordId: "limerence"),
        TrendingTag(label: "Ethereal", wordId: "ethereal"),
    ]

    static let serendipityEntry = WordEntry(
        id: "serendipity",
        word: "Serendipity",
        phonetic: "/ˌserənˈdɪpɪti/",
        partOfSpeech: .noun,
        definitions: [
            "The occurrence of something good or useful by chance.",
            "A fortunate accident; a happy chance.",
            "A pleasant and unexpected discovery.",
        ],
        etymology: "Coined in the 18th century, from the fairy tale “The Three Princes of Serendip”, where the princes made fortunate discoveries by chance.",
        synonyms: ["luck", "happy chance", "fortuitous event", "coincidence"],
        antonyms: ["misfortune", "bad luck", "unfortunate event"],
        examples: [
            "It was pure serendipity that we found this little book.",
            "The meeting was a serendipity.",
            "It was serendipity that brought them together.",
            "Finding the old letter was a happy serendipity.",
        ],
        language: "English",
        illustration: "ill-hero-bridge",
        originPeriod: "Late 18th c.",
        frequency: "High",
        level: "Intermediate",
        relatedWords: ["serendipitous", "serendipitously", "serendipitousness"],
        didYouKnow: "The word “serendipity” comes from a fairy tale about three princes from Serendip — an old name for Sri Lanka."
    )

    static let solitudeEntry = WordEntry(
        id: "solitude",
        word: "Solitude",
        phonetic: "/ˈsɒlɪtjuːd/",
        partOfSpeech: .noun,
        definitions: [
            "The state of being alone or isolated.",
            "Quiet, peaceful seclusion away from noise and interruption.",
        ],
        etymology: "From Old French solitude, from Latin solitudo ('loneliness, being alone'), from solus ('alone').",
        synonyms: ["seclusion", "quietude", "peace", "sanctuary"],
        antonyms: ["company", "crowd", "commotion"],
        examples: [
            "She savoured the quiet solitude of the honey-stone cottage at dawn.",
            "In the solitude of the meadow path, every birdsong felt clear.",
        ],
        language: "English",
        illustration: "ill-wotd-solitude",
        originPeriod: "14th c.",
        frequency: "High",
        level: "Intermediate",
        relatedWords: ["solitary", "solitarily", "solitariness"],
        didYouKnow: "Unlike 'loneliness', 'solitude' historically celebrated restorative peace chosen for reflection and study."
    )

    static let wordOfTheDay = WordEntry(
        id: "wanderlust",
        word: "Wanderlust",
        phonetic: "/ˈwɒndərˌlʌst/",
        partOfSpeech: .noun,
        definitions: [
            "A strong desire or urge to wander or travel and explore the world.",
            "An irresistible impulse to travel and explore, often without a fixed destination.",
        ],
        etymology: "Borrowed from German Wanderlust (literally 'hiking desire'), from wandern ('to hike, roam') + Lust ('desire, pleasure'). First attested in English c. 1902.",
        synonyms: ["itchy feet", "restlessness", "travel bug", "nomadism"],
        antonyms: ["homebody", "stay-at-home"],
        examples: [
            "After years of city life, her wanderlust led her down winding country lanes.",
            "The novel captures the wanderlust of a generation that refused to settle.",
        ],
        language: "English (German)",
        illustration: "wotd-bg-light",
        originPeriod: "Early 20th c.",
        frequency: "Medium",
        level: "Intermediate",
        relatedWords: ["wanderer", "wandering", "wander"],
        didYouKnow: "Entered English in 1902 from German hiking clubs who celebrated walking through forests and valleys."
    )

    static let dictionary: [WordEntry] = [
        wordOfTheDay,
        serendipityEntry,
        solitudeEntry,
        WordEntry(
            id: "ephemeral",
            word: "Ephemeral",
            phonetic: "/ɪˈfɛm(ə)rəl/",
            partOfSpeech: .adjective,
            definitions: [
                "Lasting for a very short time; transitory.",
                "Withering and fading within a single brief season.",
            ],
            etymology: "From Greek ephēmeros ('lasting only a day'), from epi- ('on') + hēmera ('day').",
            synonyms: ["transient", "fleeting", "momentary", "evanescent"],
            antonyms: ["permanent", "enduring", "perpetual"],
            examples: ["The ephemeral beauty of wildflower blossoms makes spring precious."],
            language: "English",
            originPeriod: "Late 16th c.",
            frequency: "Medium",
            level: "Advanced",
            relatedWords: ["ephemera", "ephemerality", "ephemerally"],
            didYouKnow: "Originally a medical and botanical term describing flowers and fevers lasting just a single day."
        ),
        WordEntry(
            id: "limerence",
            word: "Limerence",
            phonetic: "/ˈlɪmərəns/",
            partOfSpeech: .noun,
            definitions: ["The state of being deeply infatuated with another person, marked by a vivid longing for reciprocation."],
            etymology: "Coined in 1977 by psychologist Dorothy Tennov to describe involuntary romantic longing.",
            synonyms: ["infatuation", "adoration", "yearning"],
            antonyms: ["indifference", "detachment"],
            examples: ["Limerence turns every small letter into an event of the heart."],
            language: "English",
            originPeriod: "1977",
            frequency: "Rare",
            level: "Advanced",
            relatedWords: ["limerent", "non-limerent"],
            didYouKnow: "Dorothy Tennov invented 'limerence' from scratch so it would carry no prior etymological baggage."
        ),
        WordEntry(
            id: "ethereal",
            word: "Ethereal",
            phonetic: "/ɪˈθɪərɪəl/",
            partOfSpeech: .adjective,
            definitions: [
                "Extremely delicate and light in a way that seems almost too perfect for this world.",
                "Of or relating to the upper regions of the sky; celestial.",
            ],
            etymology: "From Latin aetherius ('of the upper pure air'), from Greek aithēr.",
            synonyms: ["delicate", "celestial", "otherworldly", "luminous"],
            antonyms: ["earthly", "heavy", "mundane"],
            examples: ["Morning mist gave the ancient stone bridge an ethereal glow."],
            language: "English",
            originPeriod: "Early 16th c.",
            frequency: "High",
            level: "Intermediate",
            relatedWords: ["ethereally", "ethereality", "ether"],
            didYouKnow: "Ancient Greeks distinguished aer (lower misty air) from aither (the radiant upper sky)."
        ),
        WordEntry(
            id: "eloquence",
            word: "Eloquence",
            phonetic: "/ˈeləkwəns/",
            partOfSpeech: .noun,
            definitions: [
                "Fluent, graceful, and persuasive speaking or writing.",
                "The art of expressing thought with clarity and beauty.",
            ],
            etymology: "From Old French eloquence, from Latin eloquentia, from eloqui ('to speak out').",
            synonyms: ["fluency", "oratory", "articulateness", "poise"],
            antonyms: ["hesitation", "inarticulateness"],
            examples: ["Her quiet eloquence lingered long after the candlelit reading ended."],
            language: "English",
            originPeriod: "Late 14th c.",
            frequency: "High",
            level: "Intermediate",
            relatedWords: ["eloquent", "eloquently"],
            didYouKnow: "In Renaissance rhetoric, eloquence joined wisdom and style as the highest ideal of scholarship."
        ),
        WordEntry(
            id: "nostalgia",
            word: "Nostalgia",
            phonetic: "/nɒˈstældʒə/",
            partOfSpeech: .noun,
            definitions: ["A sentimental longing or wistful affection for a period or place in the past."],
            etymology: "Coined in 1688 by Johannes Hofer from Greek nostos ('homecoming') + algos ('ache, longing').",
            synonyms: ["wistfulness", "longing", "reminiscence", "homesickness"],
            antonyms: ["anticipation", "indifference"],
            examples: ["The scent of pressed lavender inside the old dictionary stirred warm nostalgia."],
            language: "English",
            originPeriod: "Late 17th c.",
            frequency: "High",
            level: "Intermediate",
            relatedWords: ["nostalgic", "nostalgically"],
            didYouKnow: "Coined originally in Switzerland to describe soldiers longing for their mountain valleys."
        ),
        WordEntry(
            id: "tranquility",
            word: "Tranquility",
            phonetic: "/træŋˈkwɪlɪti/",
            partOfSpeech: .noun,
            definitions: ["The quality or state of being tranquil; calm, quiet serenity."],
            etymology: "From Latin tranquillitas ('quietness, stillness, calm sea').",
            synonyms: ["serenity", "peacefulness", "stillness", "repose"],
            antonyms: ["turmoil", "commotion", "restlessness"],
            examples: ["Dusk settled over the church spire in complete tranquility."],
            language: "English",
            originPeriod: "Late 14th c.",
            frequency: "High",
            level: "Intermediate",
            relatedWords: ["tranquil", "tranquilly", "tranquilize"],
            didYouKnow: "Both 'tranquility' and 'tranquillity' have coexisted in English printing since the 1500s."
        ),
        WordEntry(
            id: "mellifluous",
            word: "Mellifluous",
            phonetic: "/mɪˈlɪflʊəs/",
            partOfSpeech: .adjective,
            definitions: ["Pleasingly smooth and musical to hear; sweet-sounding."],
            etymology: "From Late Latin mellifluus, from mel ('honey') + fluere ('to flow').",
            synonyms: ["dulcet", "honeyed", "euphonious", "melodious"],
            antonyms: ["harsh", "discordant", "grating"],
            examples: ["A mellifluous blackbird song echoed across the orchard."],
            language: "English",
            originPeriod: "15th c.",
            frequency: "Medium",
            level: "Advanced",
            relatedWords: ["mellifluously", "mellifluence"],
            didYouKnow: "Literally translates from Latin as 'flowing with honey'."
        ),
        WordEntry(
            id: "quixotic",
            word: "Quixotic",
            phonetic: "/kwɪkˈsɒtɪk/",
            partOfSpeech: .adjective,
            definitions: ["Exceedingly idealistic, romantic, and impractical."],
            etymology: "Named after Don Quixote, hero of Miguel de Cervantes' 1605 chivalric romance.",
            synonyms: ["idealistic", "visionary", "romantic", "chivalrous"],
            antonyms: ["pragmatic", "realistic", "practical"],
            examples: ["Cataloguing every dialect word of the shire was a noble, quixotic quest."],
            language: "English",
            originPeriod: "Late 18th c.",
            frequency: "Medium",
            level: "Advanced",
            relatedWords: ["quixotically", "quixotism"],
            didYouKnow: "One of English's best-loved literary eponyms, alongside 'pickwickian' and 'utopian'."
        ),
        WordEntry(
            id: "petrichor",
            word: "Petrichor",
            phonetic: "/ˈpɛtrɪkɔː/",
            partOfSpeech: .noun,
            definitions: ["The pleasant, earthy smell produced when rain falls on dry soil."],
            etymology: "Coined in 1964 by Bear & Thomas from Greek petros ('stone') + ichor.",
            synonyms: ["rain-scent", "earth-aroma"],
            antonyms: [],
            examples: ["Summer rain over the limestone wall released a wave of petrichor."],
            language: "English",
            originPeriod: "1964",
            frequency: "Medium",
            level: "Intermediate",
            relatedWords: ["geosmin"],
            didYouKnow: "Published in the journal Nature in 1964 to name the scent of rain on warm stone."
        ),
        WordEntry(
            id: "halcyon",
            word: "Halcyon",
            phonetic: "/ˈhalsɪən/",
            partOfSpeech: .adjective,
            definitions: ["Denoting a period of time in the past that was idyllically happy and peaceful."],
            etymology: "From Greek halkyōn ('kingfisher'), fabled to calm the winter seas.",
            synonyms: ["golden", "idyllic", "serene", "peaceful"],
            antonyms: ["stormy", "turbulent"],
            examples: ["She recalled halcyon afternoons reading beside the millpond."],
            language: "English",
            originPeriod: "14th c.",
            frequency: "Medium",
            level: "Advanced",
            relatedWords: ["halcyon days"],
            didYouKnow: "'Halcyon days' originally meant 14 calm winter days around the solstice."
        ),
        WordEntry(
            id: "vellichor",
            word: "Vellichor",
            phonetic: "/ˈvɛlɪkɔː/",
            partOfSpeech: .noun,
            definitions: ["The strange wistfulness of used bookshops, infused with the passage of time."],
            etymology: "Modern literary coinage combining vellum ('parchment') + ichor.",
            synonyms: ["bookshop nostalgia", "bibliophilic wonder"],
            antonyms: [],
            examples: ["Stepping into the village bookshop wrapped him in quiet vellichor."],
            language: "English",
            originPeriod: "21st c.",
            frequency: "Rare",
            level: "Advanced",
            relatedWords: ["vellum", "petrichor"],
            didYouKnow: "Echoes 'petrichor' while honouring old vellum-bound volumes."
        ),
        WordEntry(
            id: "komorebi",
            word: "Komorebi",
            phonetic: "/kɔmɔˈɹɛbi/",
            partOfSpeech: .noun,
            definitions: ["Sunlight filtering gently through the leaves of trees."],
            etymology: "Japanese 木漏れ日 (tree + filtering through + sunlight).",
            synonyms: ["dappled light", "leaf-shadow"],
            antonyms: [],
            examples: ["The woodland lane shimmered with golden komorebi."],
            language: "Japanese",
            originPeriod: "Classical",
            frequency: "Medium",
            level: "Intermediate",
            relatedWords: ["shinrin-yoku"],
            didYouKnow: "Captures the exact interplay between leaves, breeze, and sunbeams."
        ),
        WordEntry(
            id: "saudade",
            word: "Saudade",
            phonetic: "/sɔːˈdɑːdə/",
            partOfSpeech: .noun,
            definitions: ["A deep, poetic state of nostalgic longing for someone or something absent."],
            etymology: "Portuguese, from Latin solitas ('solitude').",
            synonyms: ["longing", "yearning", "wistfulness"],
            antonyms: ["contentment", "fulfilment"],
            examples: ["Letters tied in ribbon carried a tender saudade across the sea."],
            language: "Portuguese",
            originPeriod: "15th c.",
            frequency: "Medium",
            level: "Advanced",
            relatedWords: ["saudosismo"],
            didYouKnow: "Shares its ancient Latin root with 'solitude'."
        ),
        WordEntry(
            id: "ineffable",
            word: "Ineffable",
            phonetic: "/ɪnˈɛfəb(ə)l/",
            partOfSpeech: .adjective,
            definitions: ["Too great, subtle, or beautiful to be expressed in words."],
            etymology: "From Latin ineffabilis ('unutterable'), from in- + effari ('to speak out').",
            synonyms: ["indescribable", "beyond words", "transcendent"],
            antonyms: ["definable", "ordinary"],
            examples: ["Twilight over the hills held an ineffable hush."],
            language: "English",
            originPeriod: "15th c.",
            frequency: "Medium",
            level: "Advanced",
            relatedWords: ["ineffably", "ineffability"],
            didYouKnow: "Paradoxically, 'ineffable' is a word created specifically for what words cannot say."
        ),
        WordEntry(
            id: "numinous",
            word: "Numinous",
            phonetic: "/ˈnjuːmɪnəs/",
            partOfSpeech: .adjective,
            definitions: ["Awe-inspiring, mysterious, and quietly sacred in atmosphere."],
            etymology: "From Latin numen ('presiding spirit, nod of divinity').",
            synonyms: ["sacred", "mystical", "transcendent"],
            antonyms: ["mundane", "prosaic"],
            examples: ["Lantern light beneath the ancient yew felt quietly numinous."],
            language: "English",
            originPeriod: "17th c.",
            frequency: "Rare",
            level: "Advanced",
            relatedWords: ["numinously", "numen"],
            didYouKnow: "Popularised in 1917 to describe the hush felt in ancient libraries, cathedrals, and groves."
        ),
    ]

    static let categories: [LexiconCategory] = [
        LexiconCategory(id: "vocabulary", title: "Vocabulary", subtitle: "Expand your words", illustration: "cat-vocabulary"),
        LexiconCategory(id: "geography", title: "Geography", subtitle: "Places & names", illustration: "cat-geography"),
        LexiconCategory(id: "literature", title: "Literature", subtitle: "Terms & references", illustration: "cat-literature"),
        LexiconCategory(id: "science", title: "Science", subtitle: "Concepts & terms", illustration: "cat-science"),
    ]

    static let exploreTopics: [ExploreTopic] = [
        ExploreTopic(id: "categories", title: "Categories", subtitle: "Browse by topic", illustration: "ill-exp-categories"),
        ExploreTopic(id: "languages", title: "Languages", subtitle: "Explore all languages", illustration: "ill-exp-languages"),
        ExploreTopic(id: "phrases", title: "Phrases", subtitle: "Useful expressions", illustration: "ill-exp-phrases"),
        ExploreTopic(id: "idioms", title: "Idioms", subtitle: "Figures of speech", illustration: "ill-exp-idioms"),
        ExploreTopic(id: "examples", title: "Examples", subtitle: "Word in context", illustration: "ill-exp-examples"),
        ExploreTopic(id: "etymology", title: "Etymology", subtitle: "Word origins", illustration: "ill-exp-etymology"),
    ]

    static let articles: [Article] = [
        Article(id: "a1", title: "The Fascinating Origin of Everyday Words", excerpt: "Surprising stories behind everyday English words.", readMinutes: 5, thumbnail: "article-1"),
        Article(id: "a2", title: "Lost Words from the Past", excerpt: "Beautiful English words that deserve a quiet revival.", readMinutes: 4, thumbnail: "article-2"),
        Article(id: "a3", title: "The Language of Nature", excerpt: "How countrysides and seasons shaped our lexicon.", readMinutes: 6, thumbnail: "article-3"),
    ]

    static let defaultNotes: [NoteItem] = [
        NoteItem(id: "n1", category: .wordNote, title: "Serendipity", body: "“The occurrence of something good or useful by chance.”", dateLabel: "Sep 16, 2026", bookmarked: false, illustration: "ill-note-flowers"),
        NoteItem(id: "n2", category: .study, title: "Etymology", body: "From Latin 'serendipitas' and the Persian tale of Serendip...", dateLabel: "Sep 15, 2026", bookmarked: true, illustration: "ill-col-bookmarks"),
        NoteItem(id: "n3", category: .personal, title: "Favourite Quote", body: "“Words are bridges between minds.”", dateLabel: "Sep 14, 2026", bookmarked: false, illustration: "ill-col-notes"),
        NoteItem(id: "n4", category: .idea, title: "App Feature", body: "Add a daily word notification at morning tea.", dateLabel: "Sep 13, 2026", bookmarked: true, illustration: "ill-note-lantern"),
    ]

    static let defaultStudyLists: [StudyListItem] = [
        StudyListItem(id: "sl1", title: "Daily Vocabulary", totalWords: 50, completedWords: 40, thumbnail: "ill-thumb-lavender", wordIds: ["serendipity", "solitude", "ephemeral", "eloquence"]),
        StudyListItem(id: "sl2", title: "Exam Preparation", totalWords: 120, completedWords: 78, thumbnail: "ill-thumb-cottage", wordIds: ["mellifluous", "quixotic", "ineffable", "halcyon"]),
        StudyListItem(id: "sl3", title: "Advanced Words", totalWords: 80, completedWords: 34, thumbnail: "ill-thumb-books", wordIds: ["limerence", "vellichor", "numinous", "petrichor"]),
        StudyListItem(id: "sl4", title: "Travel & Nature", totalWords: 60, completedWords: 12, thumbnail: "ill-thumb-meadow", wordIds: ["wanderlust", "komorebi", "tranquility", "ethereal"]),
    ]

    static let defaultTemplates: [VocabTemplate] = [
        VocabTemplate(id: "tpl1", title: "Daily Vocabulary", subtitle: "Learn 10 new words a day.", category: .learning, illustration: "ill-tpl-daily", starred: true, sampleWords: ["Serendipity", "Solitude", "Eloquence"]),
        VocabTemplate(id: "tpl2", title: "Exam Preparation", subtitle: "Be exam-ready with key words.", category: .exam, illustration: "ill-tpl-exam", starred: true, sampleWords: ["Quixotic", "Mellifluous", "Ineffable"]),
        VocabTemplate(id: "tpl3", title: "Writing Assistant", subtitle: "Find the perfect words.", category: .writing, illustration: "ill-tpl-write", starred: true, sampleWords: ["Ethereal", "Halcyon", "Vellichor"]),
        VocabTemplate(id: "tpl4", title: "Travel & Nature", subtitle: "Pack words for your journeys.", category: .dailyLife, illustration: "ill-tpl-travel", starred: true, sampleWords: ["Wanderlust", "Petrichor", "Komorebi"]),
    ]

    static let idiomsAndPhrases: [IdiomPhrase] = [
        IdiomPhrase(id: "ip1", phrase: "A breath of fresh air", meaning: "Someone or something refreshingly new and peaceful.", example: "The village bookshop was a breath of fresh air.", kind: "Idiom"),
        IdiomPhrase(id: "ip2", phrase: "Turn over a new leaf", meaning: "Begin anew with fresh resolve, like opening a clean page.", example: "With autumn arriving, he turned over a new leaf in his studies.", kind: "Idiom"),
        IdiomPhrase(id: "ip3", phrase: "Through thick and thin", meaning: "Originally 'through thicket and thin wood' along English forest paths.", example: "Old friends walk beside us through thick and thin.", kind: "Phrase"),
        IdiomPhrase(id: "ip4", phrase: "Halcyon days", meaning: "A period of calm, golden tranquility remembered fondly.", example: "Those meadow summers remain our halcyon days.", kind: "Phrase"),
    ]

    static let morphemeRoots: [MorphemeRoot] = [
        MorphemeRoot(root: "sol-", meaning: "alone, single", origin: "Latin solus", derivatives: ["Solitude", "Solitary", "Soliloquy", "Console"]),
        MorphemeRoot(root: "loqu- / loc-", meaning: "to speak", origin: "Latin loqui", derivatives: ["Eloquence", "Loquacious", "Colloquial", "Elocution"]),
        MorphemeRoot(root: "luc- / lum-", meaning: "light, shine", origin: "Latin lux / lumen", derivatives: ["Luminous", "Lucid", "Elucidate", "Translucent"]),
        MorphemeRoot(root: "phil-", meaning: "love, affinity", origin: "Greek philos", derivatives: ["Bibliophile", "Philology", "Philosophy", "Euphemism"]),
    ]

    private static let thirtySixLanguages: [(String, String)] = [
        ("French", "fr"), ("German", "de"), ("Spanish", "es"), ("Italian", "it"),
        ("Portuguese", "pt"), ("Dutch", "nl"), ("Swedish", "sv"), ("Norwegian", "no"),
        ("Danish", "da"), ("Finnish", "fi"), ("Icelandic", "is"), ("Irish", "ga"),
        ("Welsh", "cy"), ("Scottish Gaelic", "gd"), ("Polish", "pl"), ("Czech", "cs"),
        ("Hungarian", "hu"), ("Romanian", "ro"), ("Greek", "el"), ("Turkish", "tr"),
        ("Russian", "ru"), ("Ukrainian", "uk"), ("Arabic", "ar"), ("Persian", "fa"),
        ("Hindi", "hi"), ("Bengali", "bn"), ("Urdu", "ur"), ("Tamil", "ta"),
        ("Japanese", "ja"), ("Korean", "ko"), ("Mandarin Chinese", "zh"), ("Vietnamese", "vi"),
        ("Thai", "th"), ("Indonesian", "id"), ("Swahili", "sw"), ("Latin", "la"),
    ]

    private static let curatedSerendipity: [String: (String, String)] = [
        "fr": ("Sérendipité", "seh-rahn-dee-pee-tay"),
        "de": ("Serendipität", "ze-ren-di-pi-taet"),
        "es": ("Serendipia", "seh-ren-dee-pyah"),
        "it": ("Serendipità", "seh-ren-dee-pee-tah"),
        "pt": ("Serendipidade", "se-ren-dee-pee-da-de"),
        "nl": ("Serendipiteit", "se-ren-di-pi-teit"),
        "sv": ("Serendipitet", "se-ren-di-pi-tet"),
        "no": ("Serendipitet", "se-ren-di-pi-tet"),
        "da": ("Serendipitet", "se-ren-di-pi-tet"),
        "fi": ("Onnekas sattuma", "on-ne-kas sat-tu-ma"),
        "is": ("Heillafundur", "heid-la-fun-dur"),
        "ga": ("Seans maith", "shans mah"),
        "cy": ("Serendipedd", "se-ren-di-pedh"),
        "gd": ("Fortan gun dùil", "for-tan gun dool"),
        "pl": ("Szczęśliwy traf", "shchen-shlee-vy traf"),
        "cs": ("Šťastná náhoda", "shtyas-tnaa naa-ho-da"),
        "hu": ("Szerencsés véletlen", "se-ren-cheesh vee-let-len"),
        "ro": ("Serendipitate", "se-ren-di-pi-ta-te"),
        "el": ("Ευτυχής συγκυρία", "ef-ti-hees sin-gi-ree-a"),
        "tr": ("Mutlu tesadüf", "mut-lu te-sa-duf"),
        "ru": ("Интуитивная прозорливость", "in-tu-i-tiv-na-ya"),
        "uk": ("Щасливий випадок", "shchas-ly-vyi vy-pa-dok"),
        "ar": ("المصادفة السعيدة", "al-musādafa al-saʿīda"),
        "fa": ("سرندیپیتی (کشف خوشایند)", "serendipiti"),
        "hi": ("सुखद संयोग", "sukhad sanyog"),
        "bn": ("শুভ কাকতালীয় প্রাপ্তি", "shubho kaktaliyo prapti"),
        "ur": ("خوشگوار اتفاق", "khushgawar ittefaq"),
        "ta": ("நல்வாய்ப்பு", "nal-vaayp-pu"),
        "ja": ("セレンディピティ (偶発的発見)", "serendipiti"),
        "ko": ("세렌디피티 (뜻밖의 행운)", "se-ren-di-pi-ti"),
        "zh": ("意外发现珍奇 (机缘巧合)", "jī yuán qiǎo hé"),
        "vi": ("Sự tình cờ may mắn", "su tinh co may man"),
        "th": ("ความบังเอิญที่น่ายินดี", "khwam bang-oen"),
        "id": ("Kebetulan yang indah", "ke-be-tu-lan yang in-dah"),
        "sw": ("Bahati njema isiyotarajiwa", "ba-ha-ti nje-ma"),
        "la": ("Fortuna felix inopinata", "for-tu-na fe-lix"),
    ]

    static func translationsFor(_ word: WordEntry) -> [WordTranslation] {
        thirtySixLanguages.map { (lang, code) in
            if word.id == "serendipity", let p = curatedSerendipity[code] {
                return WordTranslation(language: lang, code: code, translated: p.0, transliteration: p.1)
            }
            return WordTranslation(language: lang, code: code, translated: "\(word.word) (\(lang))", transliteration: word.phonetic)
        }
    }

    static func getWordById(_ id: String) -> WordEntry? {
        dictionary.first { $0.id.caseInsensitiveCompare(id) == .orderedSame || $0.word.caseInsensitiveCompare(id) == .orderedSame }
    }

    static func searchWords(_ query: String, limit: Int = 12) -> [WordEntry] {
        let q = query.trimmingCharacters(in: .whitespacesAndNewlines).lowercased()
        guard !q.isEmpty else { return [] }
        return Array(
            dictionary.filter { entry in
                let w = entry.word.lowercased()
                return w.hasPrefix(q)
                    || w.contains(q)
                    || entry.definitions.contains { $0.lowercased().contains(q) }
                    || entry.synonyms.contains { $0.lowercased().contains(q) }
            }.prefix(limit)
        )
    }

    static func getAlphabetSections() -> [(letter: String, entries: [WordEntry])] {
        let grouped = Dictionary(grouping: dictionary) { entry in
            String(entry.word.prefix(1)).uppercased()
        }
        return grouped.keys.sorted().map { letter in
            (letter: letter, entries: (grouped[letter] ?? []).sorted { $0.word < $1.word })
        }
    }
}
