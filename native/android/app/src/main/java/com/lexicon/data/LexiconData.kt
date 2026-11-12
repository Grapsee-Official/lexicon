package com.lexicon.data

object LexiconData {

    val trendingTags: List<TrendingTag> = listOf(
        TrendingTag("Ephemeral", "ephemeral"),
        TrendingTag("Serendipity", "serendipity"),
        TrendingTag("Limerence", "limerence"),
        TrendingTag("Ethereal", "ethereal"),
    )

    val serendipityEntry = WordEntry(
        id = "serendipity",
        word = "Serendipity",
        phonetic = "/ˌserənˈdɪpɪti/",
        partOfSpeech = PartOfSpeech.NOUN,
        definitions = listOf(
            "The occurrence of something good or useful by chance.",
            "A fortunate accident; a happy chance.",
            "A pleasant and unexpected discovery.",
        ),
        etymology = "Coined in the 18th century, from the fairy tale “The Three Princes of Serendip”, where the princes made fortunate discoveries by chance.",
        synonyms = listOf("luck", "happy chance", "fortuitous event", "coincidence"),
        antonyms = listOf("misfortune", "bad luck", "unfortunate event"),
        examples = listOf(
            "It was pure serendipity that we found this little book.",
            "The meeting was a serendipity.",
            "It was serendipity that brought them together.",
            "Finding the old letter was a happy serendipity.",
        ),
        language = "English",
        illustration = "ill_hero_bridge",
        originPeriod = "Late 18th c.",
        frequency = "High",
        level = "Intermediate",
        relatedWords = listOf("serendipitous", "serendipitously", "serendipitousness"),
        didYouKnow = "The word “serendipity” comes from a fairy tale about three princes from Serendip — an old name for Sri Lanka.",
    )

    val solitudeEntry = WordEntry(
        id = "solitude",
        word = "Solitude",
        phonetic = "/ˈsɒlɪtjuːd/",
        partOfSpeech = PartOfSpeech.NOUN,
        definitions = listOf(
            "The state of being alone or isolated.",
            "Quiet, peaceful seclusion away from noise and interruption.",
        ),
        etymology = "From Old French solitude, from Latin solitudo ('loneliness, being alone'), from solus ('alone').",
        synonyms = listOf("seclusion", "quietude", "peace", "sanctuary"),
        antonyms = listOf("company", "crowd", "commotion"),
        examples = listOf(
            "She savoured the quiet solitude of the honey-stone cottage at dawn.",
            "In the solitude of the meadow path, every birdsong felt clear.",
        ),
        language = "English",
        illustration = "ill_wotd_solitude",
        originPeriod = "14th c.",
        frequency = "High",
        level = "Intermediate",
        relatedWords = listOf("solitary", "solitarily", "solitariness"),
        didYouKnow = "Unlike 'loneliness', 'solitude' historically celebrated restorative peace chosen for reflection and study.",
    )

    val wordOfTheDay: WordEntry = WordEntry(
        id = "wanderlust",
        word = "Wanderlust",
        phonetic = "/ˈwɒndərˌlʌst/",
        partOfSpeech = PartOfSpeech.NOUN,
        definitions = listOf(
            "A strong desire or urge to wander or travel and explore the world.",
            "An irresistible impulse to travel and explore, often without a fixed destination.",
        ),
        etymology = "Borrowed from German Wanderlust (literally 'hiking desire'), from wandern ('to hike, roam') + Lust ('desire, pleasure'). First attested in English c. 1902.",
        synonyms = listOf("itchy feet", "restlessness", "travel bug", "nomadism"),
        antonyms = listOf("homebody", "stay-at-home"),
        examples = listOf(
            "After years of city life, her wanderlust led her down winding country lanes.",
            "The novel captures the wanderlust of a generation that refused to settle.",
        ),
        language = "English (German)",
        illustration = "wotd_bg_light",
        originPeriod = "Early 20th c.",
        frequency = "Medium",
        level = "Intermediate",
        relatedWords = listOf("wanderer", "wandering", "wander"),
        didYouKnow = "Entered English in 1902 from German hiking clubs who celebrated walking through forests and valleys.",
    )

    val dictionary: List<WordEntry> = listOf(
        wordOfTheDay,
        serendipityEntry,
        solitudeEntry,
        WordEntry(
            id = "ephemeral",
            word = "Ephemeral",
            phonetic = "/ɪˈfɛm(ə)rəl/",
            partOfSpeech = PartOfSpeech.ADJECTIVE,
            definitions = listOf(
                "Lasting for a very short time; transitory.",
                "Withering and fading within a single brief season.",
            ),
            etymology = "From Greek ephēmeros ('lasting only a day'), from epi- ('on') + hēmera ('day').",
            synonyms = listOf("transient", "fleeting", "momentary", "evanescent"),
            antonyms = listOf("permanent", "enduring", "perpetual"),
            examples = listOf("The ephemeral beauty of wildflower blossoms makes spring precious."),
            language = "English",
            originPeriod = "Late 16th c.",
            frequency = "Medium",
            level = "Advanced",
            relatedWords = listOf("ephemera", "ephemerality", "ephemerally"),
            didYouKnow = "Originally a medical and botanical term describing flowers and fevers lasting just a single day.",
        ),
        WordEntry(
            id = "limerence",
            word = "Limerence",
            phonetic = "/ˈlɪmərəns/",
            partOfSpeech = PartOfSpeech.NOUN,
            definitions = listOf(
                "The state of being deeply infatuated with another person, marked by a vivid longing for reciprocation.",
            ),
            etymology = "Coined in 1977 by psychologist Dorothy Tennov to describe involuntary romantic longing.",
            synonyms = listOf("infatuation", "adoration", "yearning"),
            antonyms = listOf("indifference", "detachment"),
            examples = listOf("Limerence turns every small letter into an event of the heart."),
            language = "English",
            originPeriod = "1977",
            frequency = "Rare",
            level = "Advanced",
            relatedWords = listOf("limerent", "non-limerent"),
            didYouKnow = "Dorothy Tennov invented 'limerence' from scratch so it would carry no prior etymological baggage.",
        ),
        WordEntry(
            id = "ethereal",
            word = "Ethereal",
            phonetic = "/ɪˈθɪərɪəl/",
            partOfSpeech = PartOfSpeech.ADJECTIVE,
            definitions = listOf(
                "Extremely delicate and light in a way that seems almost too perfect for this world.",
                "Of or relating to the upper regions of the sky; celestial.",
            ),
            etymology = "From Latin aetherius ('of the upper pure air'), from Greek aithēr.",
            synonyms = listOf("delicate", "celestial", "otherworldly", "luminous"),
            antonyms = listOf("earthly", "heavy", "mundane"),
            examples = listOf("Morning mist gave the ancient stone bridge an ethereal glow."),
            language = "English",
            originPeriod = "Early 16th c.",
            frequency = "High",
            level = "Intermediate",
            relatedWords = listOf("ethereally", "ethereality", "ether"),
            didYouKnow = "Ancient Greeks distinguished aer (lower misty air) from aither (the radiant upper sky).",
        ),
        WordEntry(
            id = "eloquence",
            word = "Eloquence",
            phonetic = "/ˈeləkwəns/",
            partOfSpeech = PartOfSpeech.NOUN,
            definitions = listOf(
                "Fluent, graceful, and persuasive speaking or writing.",
                "The art of expressing thought with clarity and beauty.",
            ),
            etymology = "From Old French eloquence, from Latin eloquentia, from eloqui ('to speak out').",
            synonyms = listOf("fluency", "oratory", "articulateness", "poise"),
            antonyms = listOf("hesitation", "inarticulateness"),
            examples = listOf("Her quiet eloquence lingered long after the candlelit reading ended."),
            language = "English",
            originPeriod = "Late 14th c.",
            frequency = "High",
            level = "Intermediate",
            relatedWords = listOf("eloquent", "eloquently"),
            didYouKnow = "In Renaissance rhetoric, eloquence joined wisdom and style as the highest ideal of scholarship.",
        ),
        WordEntry(
            id = "nostalgia",
            word = "Nostalgia",
            phonetic = "/nɒˈstældʒə/",
            partOfSpeech = PartOfSpeech.NOUN,
            definitions = listOf(
                "A sentimental longing or wistful affection for a period or place in the past.",
            ),
            etymology = "Coined in 1688 by Johannes Hofer from Greek nostos ('homecoming') + algos ('ache, longing').",
            synonyms = listOf("wistfulness", "longing", "reminiscence", "homesickness"),
            antonyms = listOf("anticipation", "indifference"),
            examples = listOf("The scent of pressed lavender inside the old dictionary stirred warm nostalgia."),
            language = "English",
            originPeriod = "Late 17th c.",
            frequency = "High",
            level = "Intermediate",
            relatedWords = listOf("nostalgic", "nostalgically"),
            didYouKnow = "Coined originally in Switzerland to describe soldiers longing for their mountain valleys.",
        ),
        WordEntry(
            id = "tranquility",
            word = "Tranquility",
            phonetic = "/træŋˈkwɪlɪti/",
            partOfSpeech = PartOfSpeech.NOUN,
            definitions = listOf(
                "The quality or state of being tranquil; calm, quiet serenity.",
            ),
            etymology = "From Latin tranquillitas ('quietness, stillness, calm sea').",
            synonyms = listOf("serenity", "peacefulness", "stillness", "repose"),
            antonyms = listOf("turmoil", "commotion", "restlessness"),
            examples = listOf("Dusk settled over the church spire in complete tranquility."),
            language = "English",
            originPeriod = "Late 14th c.",
            frequency = "High",
            level = "Intermediate",
            relatedWords = listOf("tranquil", "tranquilly", "tranquilize"),
            didYouKnow = "Both 'tranquility' and 'tranquillity' have coexisted in English printing since the 1500s.",
        ),
        WordEntry(
            id = "mellifluous",
            word = "Mellifluous",
            phonetic = "/mɪˈlɪflʊəs/",
            partOfSpeech = PartOfSpeech.ADJECTIVE,
            definitions = listOf("Pleasingly smooth and musical to hear; sweet-sounding."),
            etymology = "From Late Latin mellifluus, from mel ('honey') + fluere ('to flow').",
            synonyms = listOf("dulcet", "honeyed", "euphonious", "melodious"),
            antonyms = listOf("harsh", "discordant", "grating"),
            examples = listOf("A mellifluous blackbird song echoed across the orchard."),
            language = "English",
            originPeriod = "15th c.",
            frequency = "Medium",
            level = "Advanced",
            relatedWords = listOf("mellifluously", "mellifluence"),
            didYouKnow = "Literally translates from Latin as 'flowing with honey'.",
        ),
        WordEntry(
            id = "quixotic",
            word = "Quixotic",
            phonetic = "/kwɪkˈsɒtɪk/",
            partOfSpeech = PartOfSpeech.ADJECTIVE,
            definitions = listOf("Exceedingly idealistic, romantic, and impractical."),
            etymology = "Named after Don Quixote, hero of Miguel de Cervantes' 1605 chivalric romance.",
            synonyms = listOf("idealistic", "visionary", "romantic", "chivalrous"),
            antonyms = listOf("pragmatic", "realistic", "practical"),
            examples = listOf("Cataloguing every dialect word of the shire was a noble, quixotic quest."),
            language = "English",
            originPeriod = "Late 18th c.",
            frequency = "Medium",
            level = "Advanced",
            relatedWords = listOf("quixotically", "quixotism"),
            didYouKnow = "One of English's best-loved literary eponyms, alongside 'pickwickian' and 'utopian'.",
        ),
        WordEntry(
            id = "petrichor",
            word = "Petrichor",
            phonetic = "/ˈpɛtrɪkɔː/",
            partOfSpeech = PartOfSpeech.NOUN,
            definitions = listOf("The pleasant, earthy smell produced when rain falls on dry soil."),
            etymology = "Coined in 1964 by Bear & Thomas from Greek petros ('stone') + ichor.",
            synonyms = listOf("rain-scent", "earth-aroma"),
            antonyms = emptyList(),
            examples = listOf("Summer rain over the limestone wall released a wave of petrichor."),
            language = "English",
            originPeriod = "1964",
            frequency = "Medium",
            level = "Intermediate",
            relatedWords = listOf("geosmin"),
            didYouKnow = "Published in the journal Nature in 1964 to name the scent of rain on warm stone.",
        ),
        WordEntry(
            id = "halcyon",
            word = "Halcyon",
            phonetic = "/ˈhalsɪən/",
            partOfSpeech = PartOfSpeech.ADJECTIVE,
            definitions = listOf("Denoting a period of time in the past that was idyllically happy and peaceful."),
            etymology = "From Greek halkyōn ('kingfisher'), fabled to calm the winter seas.",
            synonyms = listOf("golden", "idyllic", "serene", "peaceful"),
            antonyms = listOf("stormy", "turbulent"),
            examples = listOf("She recalled halcyon afternoons reading beside the millpond."),
            language = "English",
            originPeriod = "14th c.",
            frequency = "Medium",
            level = "Advanced",
            relatedWords = listOf("halcyon days"),
            didYouKnow = "'Halcyon days' originally meant 14 calm winter days around the solstice.",
        ),
        WordEntry(
            id = "vellichor",
            word = "Vellichor",
            phonetic = "/ˈvɛlɪkɔː/",
            partOfSpeech = PartOfSpeech.NOUN,
            definitions = listOf("The strange wistfulness of used bookshops, infused with the passage of time."),
            etymology = "Modern literary coinage combining vellum ('parchment') + ichor.",
            synonyms = listOf("bookshop nostalgia", "bibliophilic wonder"),
            antonyms = emptyList(),
            examples = listOf("Stepping into the village bookshop wrapped him in quiet vellichor."),
            language = "English",
            originPeriod = "21st c.",
            frequency = "Rare",
            level = "Advanced",
            relatedWords = listOf("vellum", "petrichor"),
            didYouKnow = "Echoes 'petrichor' while honouring old vellum-bound volumes.",
        ),
        WordEntry(
            id = "komorebi",
            word = "Komorebi",
            phonetic = "/kɔmɔˈɹɛbi/",
            partOfSpeech = PartOfSpeech.NOUN,
            definitions = listOf("Sunlight filtering gently through the leaves of trees."),
            etymology = "Japanese 木漏れ日 (tree + filtering through + sunlight).",
            synonyms = listOf("dappled light", "leaf-shadow"),
            antonyms = emptyList(),
            examples = listOf("The woodland lane shimmered with golden komorebi."),
            language = "Japanese",
            originPeriod = "Classical",
            frequency = "Medium",
            level = "Intermediate",
            relatedWords = listOf("shinrin-yoku"),
            didYouKnow = "Captures the exact interplay between leaves, breeze, and sunbeams.",
        ),
        WordEntry(
            id = "saudade",
            word = "Saudade",
            phonetic = "/sɔːˈdɑːdə/",
            partOfSpeech = PartOfSpeech.NOUN,
            definitions = listOf("A deep, poetic state of nostalgic longing for someone or something absent."),
            etymology = "Portuguese, from Latin solitas ('solitude').",
            synonyms = listOf("longing", "yearning", "wistfulness"),
            antonyms = listOf("contentment", "fulfilment"),
            examples = listOf("Letters tied in ribbon carried a tender saudade across the sea."),
            language = "Portuguese",
            originPeriod = "15th c.",
            frequency = "Medium",
            level = "Advanced",
            relatedWords = listOf("saudosismo"),
            didYouKnow = "Shares its ancient Latin root with 'solitude'.",
        ),
        WordEntry(
            id = "ineffable",
            word = "Ineffable",
            phonetic = "/ɪnˈɛfəb(ə)l/",
            partOfSpeech = PartOfSpeech.ADJECTIVE,
            definitions = listOf("Too great, subtle, or beautiful to be expressed in words."),
            etymology = "From Latin ineffabilis ('unutterable'), from in- + effari ('to speak out').",
            synonyms = listOf("indescribable", "beyond words", "transcendent"),
            antonyms = listOf("definable", "ordinary"),
            examples = listOf("Twilight over the hills held an ineffable hush."),
            language = "English",
            originPeriod = "15th c.",
            frequency = "Medium",
            level = "Advanced",
            relatedWords = listOf("ineffably", "ineffability"),
            didYouKnow = "Paradoxically, 'ineffable' is a word created specifically for what words cannot say.",
        ),
        WordEntry(
            id = "numinous",
            word = "Numinous",
            phonetic = "/ˈnjuːmɪnəs/",
            partOfSpeech = PartOfSpeech.ADJECTIVE,
            definitions = listOf("Awe-inspiring, mysterious, and quietly sacred in atmosphere."),
            etymology = "From Latin numen ('presiding spirit, nod of divinity').",
            synonyms = listOf("sacred", "mystical", "transcendent"),
            antonyms = listOf("mundane", "prosaic"),
            examples = listOf("Lantern light beneath the ancient yew felt quietly numinous."),
            language = "English",
            originPeriod = "17th c.",
            frequency = "Rare",
            level = "Advanced",
            relatedWords = listOf("numinously", "numen"),
            didYouKnow = "Popularised in 1917 to describe the hush felt in ancient libraries, cathedrals, and groves.",
        ),
    )

    val categories: List<Category> = listOf(
        Category("vocabulary", "Vocabulary", "Expand your words", "cat_vocabulary"),
        Category("geography", "Geography", "Places & names", "cat_geography"),
        Category("literature", "Literature", "Terms & references", "cat_literature"),
        Category("science", "Science", "Concepts & terms", "cat_science"),
    )

    // Page 1: 6 Explore Words cards
    val exploreTopics: List<ExploreTopic> = listOf(
        ExploreTopic("categories", "Categories", "Browse by topic", "ill_exp_categories"),
        ExploreTopic("languages", "Languages", "Explore all languages", "ill_exp_languages"),
        ExploreTopic("phrases", "Phrases", "Useful expressions", "ill_exp_phrases"),
        ExploreTopic("idioms", "Idioms", "Figures of speech", "ill_exp_idioms"),
        ExploreTopic("examples", "Examples", "Word in context", "ill_exp_examples"),
        ExploreTopic("etymology", "Etymology", "Word origins", "ill_exp_etymology"),
    )

    val articles: List<Article> = listOf(
        Article("a1", "The Fascinating Origin of Everyday Words", "Surprising stories behind everyday English words.", 5, "article_1"),
        Article("a2", "Lost Words from the Past", "Beautiful English words that deserve a quiet revival.", 4, "article_2"),
        Article("a3", "The Language of Nature", "How countrysides and seasons shaped our lexicon.", 6, "article_3"),
    )

    val defaultNotes: List<NoteItem> = listOf(
        NoteItem(
            id = "n1",
            category = NoteCategory.WORD_NOTE,
            title = "Serendipity",
            body = "“The occurrence of something good or useful by chance.”",
            dateLabel = "Sep 16, 2026",
            bookmarked = false,
            illustration = "ill_note_flowers",
        ),
        NoteItem(
            id = "n2",
            category = NoteCategory.STUDY,
            title = "Etymology",
            body = "From Latin 'serendipitas' and the Persian tale of Serendip...",
            dateLabel = "Sep 15, 2026",
            bookmarked = true,
            illustration = "ill_col_bookmarks",
        ),
        NoteItem(
            id = "n3",
            category = NoteCategory.PERSONAL,
            title = "Favourite Quote",
            body = "“Words are bridges between minds.”",
            dateLabel = "Sep 14, 2026",
            bookmarked = false,
            illustration = "ill_col_notes",
        ),
        NoteItem(
            id = "n4",
            category = NoteCategory.IDEA,
            title = "App Feature",
            body = "Add a daily word notification at morning tea.",
            dateLabel = "Sep 13, 2026",
            bookmarked = true,
            illustration = "ill_note_lantern",
        ),
    )

    val defaultStudyLists: List<StudyListItem> = listOf(
        StudyListItem("sl1", "Daily Vocabulary", 50, 40, "ill_thumb_lavender", listOf("serendipity", "solitude", "ephemeral", "eloquence")),
        StudyListItem("sl2", "Exam Preparation", 120, 78, "ill_thumb_cottage", listOf("mellifluous", "quixotic", "ineffable", "halcyon")),
        StudyListItem("sl3", "Advanced Words", 80, 34, "ill_thumb_books", listOf("limerence", "vellichor", "numinous", "petrichor")),
        StudyListItem("sl4", "Travel & Nature", 60, 12, "ill_thumb_meadow", listOf("wanderlust", "komorebi", "tranquility", "ethereal")),
    )

    val defaultTemplates: List<VocabTemplate> = listOf(
        VocabTemplate("tpl1", "Daily Vocabulary", "Learn 10 new words a day.", TemplateCategory.LEARNING, "ill_tpl_daily", true, listOf("Serendipity", "Solitude", "Eloquence")),
        VocabTemplate("tpl2", "Exam Preparation", "Be exam-ready with key words.", TemplateCategory.EXAM, "ill_tpl_exam", true, listOf("Quixotic", "Mellifluous", "Ineffable")),
        VocabTemplate("tpl3", "Writing Assistant", "Find the perfect words.", TemplateCategory.WRITING, "ill_tpl_write", true, listOf("Ethereal", "Halcyon", "Vellichor")),
        VocabTemplate("tpl4", "Travel & Nature", "Pack words for your journeys.", TemplateCategory.DAILY_LIFE, "ill_tpl_travel", true, listOf("Wanderlust", "Petrichor", "Komorebi")),
    )

    val idiomsAndPhrases: List<IdiomPhrase> = listOf(
        IdiomPhrase("ip1", "A breath of fresh air", "Someone or something refreshingly new and peaceful.", "The village bookshop was a breath of fresh air.", "Idiom"),
        IdiomPhrase("ip2", "Turn over a new leaf", "Begin anew with fresh resolve, like opening a clean page.", "With autumn arriving, he turned over a new leaf in his studies.", "Idiom"),
        IdiomPhrase("ip3", "Through thick and thin", "Originally 'through thicket and thin wood' along English forest paths.", "Old friends walk beside us through thick and thin.", "Phrase"),
        IdiomPhrase("ip4", "Halcyon days", "A period of calm, golden tranquility remembered fondly.", "Those meadow summers remain our halcyon days.", "Phrase"),
    )

    val morphemeRoots: List<MorphemeRoot> = listOf(
        MorphemeRoot("sol-", "alone, single", "Latin solus", listOf("Solitude", "Solitary", "Soliloquy", "Console")),
        MorphemeRoot("loqu- / loc-", "to speak", "Latin loqui", listOf("Eloquence", "Loquacious", "Colloquial", "Elocution")),
        MorphemeRoot("luc- / lum-", "light, shine", "Latin lux / lumen", listOf("Luminous", "Lucid", "Elucidate", "Translucent")),
        MorphemeRoot("phil-", "love, affinity", "Greek philos", listOf("Bibliophile", "Philology", "Philosophy", "Euphemism")),
    )

    // 36 Languages Translation Generator for Page 10 (Expanded Word Exploration)
    private val thirtySixLanguages: List<Pair<String, String>> = listOf(
        "French" to "fr", "German" to "de", "Spanish" to "es", "Italian" to "it",
        "Portuguese" to "pt", "Dutch" to "nl", "Swedish" to "sv", "Norwegian" to "no",
        "Danish" to "da", "Finnish" to "fi", "Icelandic" to "is", "Irish" to "ga",
        "Welsh" to "cy", "Scottish Gaelic" to "gd", "Polish" to "pl", "Czech" to "cs",
        "Hungarian" to "hu", "Romanian" to "ro", "Greek" to "el", "Turkish" to "tr",
        "Russian" to "ru", "Ukrainian" to "uk", "Arabic" to "ar", "Persian" to "fa",
        "Hindi" to "hi", "Bengali" to "bn", "Urdu" to "ur", "Tamil" to "ta",
        "Japanese" to "ja", "Korean" to "ko", "Mandarin Chinese" to "zh", "Vietnamese" to "vi",
        "Thai" to "th", "Indonesian" to "id", "Swahili" to "sw", "Latin" to "la",
    )

    private val curatedTranslations: Map<String, Map<String, Pair<String, String>>> = mapOf(
        "serendipity" to mapOf(
            "fr" to ("Sérendipité" to "seh-rahn-dee-pee-tay"),
            "de" to ("Serendipität" to "ze-ren-di-pi-taet"),
            "es" to ("Serendipia" to "seh-ren-dee-pyah"),
            "it" to ("Serendipità" to "seh-ren-dee-pee-tah"),
            "pt" to ("Serendipidade" to "se-ren-dee-pee-da-de"),
            "nl" to ("Serendipiteit" to "se-ren-di-pi-teit"),
            "sv" to ("Serendipitet" to "se-ren-di-pi-tet"),
            "no" to ("Serendipitet" to "se-ren-di-pi-tet"),
            "da" to ("Serendipitet" to "se-ren-di-pi-tet"),
            "fi" to ("Onnekas sattuma" to "on-ne-kas sat-tu-ma"),
            "is" to ("Heillafundur" to "heid-la-fun-dur"),
            "ga" to ("Seans maith" to "shans mah"),
            "cy" to ("Serendipedd" to "se-ren-di-pedh"),
            "gd" to ("Fortan gun dùil" to "for-tan gun dool"),
            "pl" to ("Szczęśliwy traf" to "shchen-shlee-vy traf"),
            "cs" to ("Šťastná náhoda" to "shtyas-tnaa naa-ho-da"),
            "hu" to ("Szerencsés véletlen" to "se-ren-cheesh vee-let-len"),
            "ro" to ("Serendipitate" to "se-ren-di-pi-ta-te"),
            "el" to ("Ευτυχής συγκυρία" to "ef-ti-hees sin-gi-ree-a"),
            "tr" to ("Mutlu tesadüf" to "mut-lu te-sa-duf"),
            "ru" to ("Интуитивная прозорливость" to "in-tu-i-tiv-na-ya"),
            "uk" to ("Щасливий випадок" to "shchas-ly-vyi vy-pa-dok"),
            "ar" to ("المصادفة السعيدة" to "al-musādafa al-saʿīda"),
            "fa" to ("سرندیپیتی (کشف خوشایند)" to "serendipiti"),
            "hi" to ("सुखद संयोग" to "sukhad sanyog"),
            "bn" to ("শুভ কাকতালীয় প্রাপ্তি" to "shubho kaktaliyo prapti"),
            "ur" to ("خوشگوار اتفاق" to "khushgawar ittefaq"),
            "ta" to ("நல்வாய்ப்பு" to "nal-vaayp-pu"),
            "ja" to ("セレンディピティ (偶発的発見)" to "serendipiti"),
            "ko" to ("세렌디피티 (뜻밖의 행운)" to "se-ren-di-pi-ti"),
            "zh" to ("意外发现珍奇 (机缘巧合)" to "jī yuán qiǎo hé"),
            "vi" to ("Sự tình cờ may mắn" to "su tinh co may man"),
            "th" to ("ความบังเอิญที่น่ายินดี" to "khwam bang-oen"),
            "id" to ("Kebetulan yang indah" to "ke-be-tu-lan yang in-dah"),
            "sw" to ("Bahati njema isiyotarajiwa" to "ba-ha-ti nje-ma"),
            "la" to ("Fortuna felix inopinata" to "for-tu-na fe-lix"),
        ),
        "solitude" to mapOf(
            "fr" to ("Solitude" to "so-lee-tyd"),
            "de" to ("Einsamkeit / Stille" to "ain-zam-kait"),
            "es" to ("Soledad" to "so-le-dad"),
            "it" to ("Solitudine" to "so-li-tu-di-ne"),
            "pt" to ("Solidão" to "so-li-daun"),
            "ja" to ("孤独・静寂" to "kodoku / seijaku"),
            "bn" to ("নির্জনতা" to "nirjonota"),
            "ar" to ("عُزلة هادئة" to "ʿuzla hādiʾa"),
            "la" to ("Solitudo" to "so-li-tu-do"),
        ),
    )

    fun translationsFor(word: WordEntry): List<WordTranslation> {
        val table = curatedTranslations[word.id] ?: curatedTranslations["serendipity"].orEmpty()
        return thirtySixLanguages.map { (lang, code) ->
            val pair = table[code]
            if (pair != null) {
                WordTranslation(lang, code, pair.first, pair.second)
            } else {
                WordTranslation(lang, code, "${word.word} ($lang)", word.phonetic)
            }
        }
    }

    fun getWordById(id: String): WordEntry? =
        dictionary.firstOrNull { it.id.equals(id, ignoreCase = true) || it.word.equals(id, ignoreCase = true) }

    fun searchWords(query: String, limit: Int = 12): List<WordEntry> {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return emptyList()
        return dictionary
            .filter { w ->
                w.word.lowercase().startsWith(q) ||
                    w.word.lowercase().contains(q) ||
                    w.definitions.any { it.lowercase().contains(q) } ||
                    w.synonyms.any { it.lowercase().contains(q) }
            }
            .take(limit)
    }

    fun getAlphabetSections(): List<AlphabetSection> {
        return dictionary
            .groupBy { it.word.first().uppercaseChar().toString() }
            .toSortedMap()
            .map { (letter, entries) ->
                AlphabetSection(letter, entries.sortedBy { it.word.lowercase() })
            }
    }
}
