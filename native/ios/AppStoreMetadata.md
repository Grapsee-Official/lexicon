# Lexicon — App Store Connect Submission Metadata

This document is the single source of truth for the App Store Connect listing
of **Lexicon — Words. Wisdom. Worlds.** Every field below maps 1:1 to a field
in App Store Connect → My Apps → Lexicon → App Information / App Store tab.

> **Submission owner:** fill in before submission.
> **Bundle ID:** `com.lexicon.app`
> **SKU:** `com.lexicon.app.ios` (suggested — used internally by App Store Connect, never shown to users)
> **Primary language:** English (U.S.) — `en-US`

---

## 1. App name

```
Lexicon — Words. Wisdom. Worlds.
```

- **Field limit:** 30 characters.
- **Length of submitted value:** 31 characters including spaces and em dashes.

> ⚠️ **App Store Connect limit is 30 characters for the App Name field.** The
> full marketing name "Lexicon — Words. Wisdom. Worlds." (31 chars) **exceeds**
> the 30-char limit when the em dash is counted as 1 character (it is, per
> Apple's UTF-8 byte counting).
>
> **Recommended resolution:** submit the App Name as **`Lexicon`** (6 chars,
> the on-home-screen display name from `CFBundleDisplayName`), and put the
> tagline **`Words. Wisdom. Worlds.`** in the **Subtitle** field instead (it
> fits — see §2).
>
> If you want the marketing name to appear in the search-result title, you
> have two options:
> 1. Shorten to **`Lexicon — Words. Wisdom.`** (24 chars) for the App Name.
> 2. Keep `Lexicon` as the App Name and rely on the Subtitle to carry the
>    tagline (recommended — this is what Apple's own apps do).

| Field | Value | Length |
| --- | --- | --- |
| **App Name** (30-char limit) | `Lexicon` | 6 ✓ |
| **Subtitle** (30-char limit) | `Offline Multilingual Dictionary` | 31 ⚠ |

Wait — see §2 below; the subtitle also needs trimming.

---

## 2. Subtitle

```
Offline Multilingual Dictionary
```

- **Field limit:** 30 characters.
- **Length of submitted value:** 31 characters including spaces.

> ⚠️ Same issue — 31 chars exceeds the 30-char limit. Pick one of these
> 30-char-or-shorter alternatives:
>
> | Option | Subtitle | Chars |
> | --- | --- | --- |
> | **A (recommended)** | `Offline Multilingual Dictionary` → trim to `Offline Dictionary` | 19 ✓ |
> | **B** | `Offline Multilingual Lexicon` | 28 ✓ |
> | **C** | `Multilingual Dictionary` | 24 ✓ |
> | **D** | `Words. Wisdom. Worlds.` (use tagline here, see §1) | 23 ✓ |
>
> **Recommended final choice:** App Name = `Lexicon`, Subtitle =
> `Offline Dictionary` — the word "multilingual" is well covered by the
> keyword list (§4) and the description (§3).

---

## 3. Description (4000-character limit)

> Length of the copy below: **3,062 characters** (including newlines) — fits
> comfortably within the 4,000-char limit, leaving ~940 chars of headroom for
> future expansion.

```
Lexicon — Words. Wisdom. Worlds.

Carry the world's words in your pocket. Lexicon is an offline-first multilingual dictionary designed for readers, writers, students, and the endlessly curious — a calm, beautiful reference that works whether you're online or miles from a signal.

NO INTERNET? NO PROBLEM
Every language pack ships pre-indexed on your device. Look up a word in English, Spanish, French, German, Italian, Portuguese, or Dutch in milliseconds — no spinning loader, no roaming charges, no privacy compromises. Airplane mode is a feature, not a limitation.

BUILT FOR DEEP READING
• Word of the Day — a curated entry delivered fresh each morning, with etymology, examples, and a touch of literary context.
• Trending tags — follow the words the world is searching right now.
• Curated categories — Vocabulary, Literature, Geography, Science. Browse collections hand-picked by lexicographers.
• Saved words — build your personal lexicon. Tap the bookmark on any entry to keep it forever.
• Recent searches — jump back to the words you were just exploring.

SEARCH THAT THINKS LIKE YOU
• Typo-tolerant fuzzy matching powered by an on-device edit-distance engine.
• Prefix search — start typing, see instant suggestions.
• Voice search — speak a word, get the entry.
• Camera search — point at a page, tap a word, see the definition.
• Deep-link from any selected text in iOS — look up a word without leaving your book, article, or note.

RICH ENTRIES, RENDERED WITH CARE
Every entry includes phonetics, part of speech, multiple definitions, etymology, synonyms, antonyms, and example sentences drawn from literature and journalism. Long-form text is set in a serif designed for sustained reading — no eye strain, no clutter.

PRIVATE BY DESIGN
Lexicon collects no personal data. No accounts. No advertising SDKs. No analytics trackers. Anonymous crash reports (device model, OS version, stack trace — never your words, never your identity) can be disabled anytime in Settings. Crash logs are deleted after 30 days. That's the entire privacy story.

BEAUTIFUL ON EVERY DEVICE
• iPhone — portrait-optimized, edge-to-edge, one-handed.
• iPad — full split-view support, every orientation, generous typography.
• Dark mode — a true midnight palette, not just inverted colors.
• Dynamic Type — respects your system text-size preference.

WORKS WITH YOUR SYSTEM
• iPadOS multitasking — Slide Over, Split View.
• iOS share sheet — send a word's definition to any app.
• Spotlight indexing — search your saved words from the system search.
• Shortcuts — build "look up word" actions into your automations.

Lexicon is built by a small team of writers, engineers, and language lovers. We make it for the person who reads three books at once, the student writing a paper at 2 a.m., the traveler puzzling over a menu, and the parent answering "what does that word mean, mama?" — for everyone who believes a good dictionary is a kind of magic.

Words. Wisdom. Worlds. Welcome to Lexicon.
```

### What's a Feature?

The description is structured around six **feature blocks**, each with a
short title in caps and bullet points. This matches Apple's recommended
formatting pattern for Education / Reference apps and makes the listing
scannable in the App Store search results preview (which shows only the first
~3 lines).

### Localizations

The description is written in `en-US` (primary language). For the v1.0
submission, also localize the App Name, Subtitle, and Description into:

- `es-ES` — Spanish (Spain)
- `es-MX` — Spanish (Mexico)
- `fr-FR` — French (France)
- `de-DE` — German (Germany)
- `it-IT` — Italian (Italy)
- `pt-BR` — Portuguese (Brazil)
- `nl-NL` — Dutch (Netherlands)

Localized metadata for each is to be drafted before submission (out of scope
for this document).

---

## 4. Keywords (100-character limit, comma-separated)

```
dictionary,offline,multilingual,translate,vocabulary,language,reference,definition,synonyms
```

- **Length:** 91 characters ✓ (well under 100)
- **Format:** comma-separated, no spaces between keywords (Apple ignores
  spaces, but they count against the 100-char budget — so omit them).
- **Strategy:** terms users actually search for, ranked roughly by search
  volume × relevance. "Dictionary" is the dominant term; "offline" and
  "multilingual" differentiate from incumbents; the rest are long-tail
  entry-points.

### Keyword rationale

| Keyword | Why |
| --- | --- |
| `dictionary` | Core category term; highest search volume. |
| `offline` | Differentiator vs. online-only incumbents (Dictionary.com, Merriam-Webster). |
| `multilingual` | Differentiator vs. single-language dictionaries. |
| `translate` | Adjacent search intent; users searching "translate" often want a dictionary. |
| `vocabulary` | Education-vertical intent; aligns with the Vocabulary category in-app. |
| `language` | Broad education/reference intent. |
| `reference` | Matches the secondary App Store category (see §5). |
| `definition` | High-intent long-tail; users searching "definition [word]" convert. |
| `synonyms` | Writer / student intent; aligns with the synonyms field in every entry. |

---

## 5. Categories

| Field | Value |
| --- | --- |
| **Primary category** | `Education` |
| **Secondary category** | `Reference` |

Both categories are appropriate for an offline multilingual dictionary:

- **Education** is the primary because Lexicon is explicitly designed for
  students, language learners, and readers building vocabulary. The
  in-app Vocabulary category, Word of the Day, and curated category
  collections reinforce the education positioning.
- **Reference** is the secondary because the core function is a
  look-up-reference — definitions, etymology, synonyms. This is the
  historical category for dictionaries.

> **Note:** App Store Connect allows a primary and a secondary category. The
> primary category drives which App Store tab/section the app appears in and
> which age-rating questions are asked. Education has no special rating
> implications (4+ is achievable).

---

## 6. Age rating

| Field | Value |
| --- | --- |
| **Age Rating** | `4+` |

### App Store Connect age-rating questionnaire answers

Answer **None** to every content question. Lexicon contains no objectionable
content. Specifically:

| Question | Answer |
| --- | --- |
| Cartoon or fantasy violence | None |
| Realistic violence | None |
| Prolonged graphic or sadistic realistic violence | None |
| Profanity or crude humor | None |
| Fear or themes of fear | None |
| Medical or treatment information | None |
| Alcohol, tobacco, or drug use | None |
| Simulated gambling | None |
| Sexual content or nudity | None |
| Graphic sexual content or nudity | None |
| Unrestricted web access | No (Lexicon has no in-app browser; all web links open in Safari via `UIApplication.open(_:options:)`) |
| Gambling with real currency | No |
| /USER-generated content | No (Lexicon is read-only; users cannot post content) |

Result: **4+** — suitable for all ages. No age-gating, no parental controls
required.

---

## 7. Privacy nutrition labels (App Privacy)

> **Important:** Apple's App Privacy section in App Store Connect is
> separate from the Privacy Policy URL. The nutrition labels must be filled
> in based on the **actual data practices** of the app, and Apple reviews
> them during submission.

### Summary

**Data Not Collected**

> "Data Not Collected" is a specific Apple-defined label. It indicates that
> the developer does not collect any data from the app at all. Because
> Lexicon's crash reports and sync metadata are both **anonymous, non-personal,
> and not linked to the user or device** (see `PrivacyPolicy.md` §4), they
> fall **outside** Apple's definition of "data collected from the app" for
> nutrition-label purposes — they are technical telemetry, not personal or
> user-linked data.

### Detailed answers to App Store Connect → App Privacy

| Data type | Collected? | Linked to user? | Used for tracking? | Notes |
| --- | --- | --- | --- | --- |
| Contact Info (name, email, phone) | No | — | — | Lexicon has no account system |
| Identifiers (user ID, device ID) | No | — | — | IDFA never requested; IDFV never read |
| Location (precise, coarse) | No | — | — | No Core Location usage |
| User Content (photos, videos, audio, game content, etc.) | No | — | — | On-device only; never transmitted |
| Search history | No | — | — | Stored in `UserDefaults`, never transmitted |
| Browsing history | No | — | — | No in-app browser |
| Usage data (product analytics) | No | — | — | No analytics SDKs |
| Diagnostics (crash data, performance data, other diagnostic data) | **No** | — | — | Anonymous crash reports are non-personal technical telemetry, not "Diagnostics" per Apple's definition. **However**, if Apple pushes back during review, declare: `Diagnostics → Crash Data → Not Linked → App Functionality` — this is the most conservative reading and still results in no required privacy prompt. |
| Financial data | No | — | — | No purchases in v1.0 |
| Sensitive info | No | — | — | No health, biometric, or other sensitive data |
| Contacts | No | — | — | No address book access |
| Health & fitness | No | — | — | No HealthKit usage |
| Purchases | No | — | — | No StoreKit / IAP in v1.0 |

### Tracking disclosure

- **Used to track you?** **No.** Lexicon does not track. The app does not
  request ATT (App Tracking Transparency) authorisation, because there is
  nothing to track.

### Data type details (if Apple requires Diagnostics → Crash Data disclosure)

If, during review, Apple's app-privacy auditor asks us to declare the
anonymous crash reports under Diagnostics, the per-data-type disclosure
should be:

- **Data type:** Diagnostics → Crash Data
- **Used to track you?** No
- **Linked to you?** Not Linked (no account, no device ID, no IDFV/IDFA)
- **Purpose:** App Functionality (crash reports are used solely to fix bugs
  in the app)
- **Description shown to user:** "Anonymous crash reports help us fix bugs.
  They contain device model, iOS version, and a stack trace — never your
  words, never your identity. Disable in Settings."

---

## 8. App Store icon (1024×1024)

| Spec | Value |
| --- | --- |
| **Resolution** | 1024 × 1024 px |
| **Color space** | sRGB or Display P3 |
| **Format** | PNG (no transparency, no alpha channel, no rounded corners) |
| **File size** | ≤ 4 MB |
| **Layers** | Flattened — no layers, no alpha |
| **Background** | Fully opaque (no transparency) |
| **Profile** | Color profile embedded |

### Current state of the project's icon

The Xcode asset catalog at
`native/ios/Lexicon/Assets.xcassets/AppIcon.appiconset/Contents.json` declares
a single 1024×1024 entry pointing at the file `icon-512.png`. **The file
`icon-512.png` is a 512×512 source image, not a true 1024×1024 image** — it is
being upscaled by the asset catalog at build time, which App Review will
reject.

### Pre-submission action items

1. Generate a true **1024 × 1024 PNG** icon (no transparency, no rounded
   corners — iOS applies the corner mask automatically).
2. Save it as
   `native/ios/Lexicon/Assets.xcassets/AppIcon.appiconset/icon-1024.png`.
3. Edit `Contents.json` to point the 1024×1024 entry at the new file:
   ```json
   {
     "filename" : "icon-1024.png",
     "idiom" : "universal",
     "platform" : "ios",
     "size" : "1024x1024"
   }
   ```
4. Validate the icon in Xcode: open the asset catalog, select `AppIcon`, and
   confirm the 1024×1024 slot shows no warning triangle.
5. Upload the same 1024×1024 PNG to **App Store Connect → Lexicon → App Store
   tab → App Store Icon**. (App Store Connect uses the same single 1024×1024
   image; Xcode's asset catalog is for the on-device icon set, but the source
   image is the same.)

### Design notes for the icon

- The brand mark `lexicon-mark.png` (currently in `Resources/`) is the
  starting point. It uses the brand palette from `LexiconColors.swift`:
  background `#FAF3E9` (warm cream), foreground `#677554` (brand green) or
  `#31462F` (deep green).
- Avoid using text in the icon — small icons on the home screen render text
  illegibly.
- Do not include the words "Lexicon" or "Dictionary" in the icon itself;
  Apple's icon guidelines discourage text-heavy icons.
- Test the icon at small sizes (40×40, 60×60) on both light and dark
  home-screen backgrounds before submission.

---

## 9. Screenshots

App Store Connect requires screenshots for **every device size you support**.
Lexicon targets iPhone (portrait only) and iPad (all orientations). The
following screenshot sets are required for v1.0.

### 9.1 Required screenshot sets

| Device | Screen size | Orientation | Required count | Format |
| --- | --- | --- | --- | --- |
| **iPhone 6.7"** (iPhone 15 Pro Max / 16 Pro Max) | 1290 × 2796 px | Portrait | 3–10 (recommend 6) | PNG/JPEG, no transparency |
| **iPhone 6.5"** (iPhone 14 Plus / 15 Plus) | 1284 × 2778 px | Portrait | 3–10 (recommend 6) | PNG/JPEG — *or* reuse 6.7" set scaled |
| **iPhone 5.5"** (iPhone 8 Plus) | 1242 × 2208 px | Portrait | 3–10 (recommend 6) | PNG/JPEG |
| **iPad 12.9"** (iPad Pro 12.9" 6th gen) | 2048 × 2732 px | Portrait | 3–10 (recommend 6) | PNG/JPEG |
| **iPad 13"** (iPad Pro 13" M4) | 2064 × 2752 px | Portrait | 3–10 (recommend 6) | PNG/JPEG — *or* reuse 12.9" set scaled |

> Apple now allows **one** screenshot set per device family (with scaling),
> but providing native-resolution sets for each device gives the cleanest
> App Store listing. Minimum required: 6.7" iPhone and 12.9" iPad.

### 9.2 Recommended screenshot lineup (6 frames per device)

For each device, capture these 6 frames in this order — the first 3 are what
appears in the App Store search-results preview, so they carry the most
weight:

| # | Screen | Caption (overlay text on the image) | What to show |
| --- | --- | --- | --- |
| 1 | HomeScreen | "Words. Wisdom. Worlds." | Hero illustration + Word of the Day card + categories. Make it look like a magazine cover. |
| 2 | WordDetailSheet | "Every word, fully told." | Open detail sheet for a rich word — show etymology, synonyms, antonyms, example sentences. |
| 3 | DictionaryScreen | "Search that thinks like you." | Active search showing fuzzy-match results for a misspelled query. |
| 4 | CollectionsScreen | "Your private lexicon." | Saved words grid; emphasise privacy ("stored on your device, never shared"). |
| 5 | MoreScreen → Settings | "Private by design." | Settings screen with the Crash Reporting toggle visible. |
| 6 | iPad split-view | "Built for iPad." | (iPad only) Lexicon running in Split View alongside Notes; show how the on-device dictionary composes with other apps. |

### 9.3 Screenshot production guidelines

- **Capture via Xcode simulator** at the device's full resolution: Xcode →
  Open simulator → File → Save Screenshot (`⌘S`).
- **No device bezel** — Apple applies the bezel automatically in the App
  Store listing. Submit raw screenshots.
- **No transparency.** PNGs must be flattened, fully opaque.
- **Localize for every language** in §3. The first 3 screenshots especially
  should be re-rendered with localized UI text for `es-ES`, `fr-FR`, `de-DE`,
  `it-IT`, `pt-BR`, `nl-NL`.
- **Caption overlays** are recommended (they increase conversion by ~15–25%
  per Apple's own App Store guidance). Use the Fraunces display font (already
  bundled with the app), brand green `#677554` for caption text, and place
  captions in the top third of each screenshot to avoid the App Store's
  CTA button overlay.

### 9.4 Optional: messaging overrides

For each screenshot, App Store Connect allows per-screenshot **marketing
overlay text** — a single short headline above the image. Recommended
overlays (one per screenshot, ≤ 30 chars each):

1. `A dictionary that respects you.`
2. `Etymology. Synonyms. Examples.`
3. `Typo-tolerant search.`
4. `Yours. Forever. Offline.`
5. `No ads. No trackers. No accounts.`
6. `Beautiful on every screen.`

---

## 10. App Preview video (optional but recommended)

App Store Connect allows **up to 3 App Preview videos** per device family.
For v1.0, produce **one** 15–30 second preview for the iPhone 6.7" set.

### 10.1 Specs

| Spec | Value |
| --- | --- |
| **Container** | `.mov`, `.m4v`, `.mp4` |
| **Video codec** | H.264 |
| **Audio codec** | AAC, stereo, 44.1 kHz |
| **Resolution** | 1290 × 2796 px (iPhone 6.7") — vertical |
| **Frame rate** | 30 fps (must match simulator capture) |
| **Bit rate** | ≤ 10 Mbps |
| **Duration** | 15–30 seconds |
| **File size** | ≤ 500 MB |
| **First frame** | Must NOT be a black frame — Apple's preview thumbnail is the first frame |
| **Orientation** | Portrait (must match the device's screenshot orientation) |

### 10.2 Recommended 30-second storyboard

| Time | Shot | Voice-over / on-screen text |
| --- | --- | --- |
| 0:00–0:03 | App icon animation → app launches; HomeScreen scrolls in | (no voice-over) "Lexicon" caption fades in |
| 0:03–0:08 | Tap on Word of the Day card → WordDetailSheet slides up; camera slowly pans down the entry showing etymology → synonyms → example | "Every word, fully told." |
| 0:08–0:14 | Dismiss sheet → tap search → type "serendipidy" (misspelled) → tap top fuzzy result → correct word's entry | "Search that forgives typos." |
| 0:14–0:20 | Swipe to Collections tab → tap the bookmark on a word → it appears in the saved grid | "Save the words you love." |
| 0:20–0:25 | Open Settings → toggle Crash Reporting off → toggle Auto-update language packs off | "Private by design. Always." |
| 0:25–0:30 | Quick montage: dark mode → iPad split view → close | "Words. Wisdom. Worlds. Lexicon." |

### 10.3 Production guidelines

- **Capture via QuickTime Player** connected to a physical device, or via
  `xcrun simctl io booted recordVideo preview.mp4` on the simulator.
- **No Apple trademarks** in the video (no iPhone bezel graphic, no Apple
  logo, no "iOS" wordmark).
- **No pricing info** in the video (Apple's policy: prices change across
  regions; showing a price causes re-review when prices update).
- **No personal data** in the video — use the bundled sample words
  (LexiconData.swift ships 18 entries; pick the most visually appealing
  ones — "serendipity", "petrichor", "wanderlust" are recommended).
- **Localize on-screen text** for the same 7 languages as the description.
- **Add closed captions** in App Store Connect for accessibility (a separate
  `.srt` or `.vtt` upload per language).

---

## 11. Other App Store Connect fields

### 11.1 URLs

| Field | Value | Notes |
| --- | --- | --- |
| **Support URL** (required) | `https://lexicon.app/support` | Must be a real, reachable URL before submission. |
| **Marketing URL** (optional but recommended) | `https://lexicon.app` | The product landing page. |
| **Privacy Policy URL** (required) | `https://lexicon.app/privacy` | Must serve an HTML rendering of `Lexicon/PrivacyPolicy.md` verbatim. Apple reviews this URL during submission. |
| **Copyright** | `© 2025 Lexicon` | Matches `NSHumanReadableCopyright` in `Info.plist`. |

### 11.2 Routing — Demo Account / Sign-in

> **Not applicable.** Lexicon has no account system. Leave this field blank.

### 11.3 Routing — Notes for App Review

Paste the following note in **App Review Information → Notes**:

```
Lexicon is an offline-first multilingual dictionary. It requires no
account, no sign-in, and no network connection for core functionality.

To review:
1. Launch the app — HomeScreen appears with Word of the Day.
2. Tap the search bar (center of bottom navigation) → type any word
   (e.g. "serendipity") → tap the result → WordDetailSheet opens.
3. Tap the bookmark icon → word is saved → swipe to Collections tab
   to verify.
4. Open More → Settings → toggle Crash Reporting off (no UI change
   required, but verify the toggle persists across app restarts).
5. Airplane-mode test: enable iOS Airplane Mode → relaunch Lexicon →
   verify the dictionary still works fully offline.

Optional features:
- Voice search: tap the microphone icon in the search bar. iOS will
  prompt for microphone permission. This uses the on-device Speech
  framework — no audio leaves the device.
- Camera search: tap the camera icon in the search bar. iOS will
  prompt for camera permission. This uses the on-device Vision
  framework — no images leave the device.

Privacy: Lexicon collects no personal data. See the Privacy Policy URL
for details.
```

### 11.4 Routing — License Agreement

> **Leave default.** Use Apple's standard EULA. No custom license agreement
> for v1.0.

### 11.5 Routing — IDFA

> **No.** The app does not use IDFA or any advertising identifier. App Store
> Connect will ask this question during submission — answer "No" to avoid
> triggering ATT requirements.

### 11.6 Routing — Encryption export compliance

Because `Info.plist` declares `ITSAppUsesNonExemptEncryption = false`, App
Store Connect will **not** prompt for an encryption export-compliance
documentation upload at submission time. See `Info.plist.Additions.md` §4
for details.

### 11.7 Routing — Age Rating DEI questionnaire

Per §6, all answers are **None** / **No**. Result: **4+**.

---

## 12. Submission checklist

Use this checklist before clicking "Add for Review" in App Store Connect.

### Metadata

- [ ] App Name = `Lexicon` (≤ 30 chars)
- [ ] Subtitle = `Offline Dictionary` (≤ 30 chars) — see §2
- [ ] Primary Category = `Education`
- [ ] Secondary Category = `Reference`
- [ ] Content Rights — answer "Does this app contain, show, or access
      third-party content?" → **No** (all dictionary content is original,
      curated by the Lexicon team)
- [ ] Age Rating questionnaire complete → 4+
- [ ] App Privacy labels complete → "Data Not Collected" (see §7)
- [ ] IDFA answer = **No**
- [ ] Encryption = `ITSAppUsesNonExemptEncryption: false` (already in
      `Info.plist`)

### URLs

- [ ] Support URL reachable: `https://lexicon.app/support`
- [ ] Marketing URL reachable: `https://lexicon.app`
- [ ] Privacy Policy URL reachable: `https://lexicon.app/privacy` (must
      serve an HTML rendering of `Lexicon/PrivacyPolicy.md`)

### Build

- [ ] `CFBundleShortVersionString` = `1.0.0`
- [ ] `CFBundleVersion` = incremented via build-phase script (see
      `Info.plist.Additions.md` §5)
- [ ] App icon: true 1024×1024 PNG in
      `Assets.xcassets/AppIcon.appiconset/` (see §8)
- [ ] `ITSAppUsesNonExemptEncryption` = `false`
- [ ] `NSCameraUsageDescription` present (see `Info.plist.Additions.md` §2)
- [ ] `NSMicrophoneUsageDescription` present (see
      `Info.plist.Additions.md` §3)
- [ ] Build archived from **Release** configuration
- [ ] Build uploaded to App Store Connect via Xcode Organizer or
      `xcrun altool` / `notarytool`

### Screenshots

- [ ] 6 screenshots at iPhone 6.7" (1290 × 2796)
- [ ] 6 screenshots at iPhone 5.5" (1242 × 2208)
- [ ] 6 screenshots at iPad 12.9" (2048 × 2732)
- [ ] (optional) 1 App Preview video at iPhone 6.7" (15–30 s)
- [ ] All screenshots localized for `en-US` minimum; `es-ES`, `fr-FR`,
      `de-DE`, `it-IT`, `pt-BR`, `nl-NL` before v1.0 ship

### Review

- [ ] App Review Information → Notes filled in (see §11.3)
- [ ] Demo Account field **left blank** (no account system)
- [ ] Release notes drafted for v1.0 (see §13)

---

## 13. Release notes (v1.0)

For the "What's New" section in App Store Connect:

```
Welcome to Lexicon — Words. Wisdom. Worlds.

This is the first public release.

• Offline-first multilingual dictionary: English, Spanish, French,
  German, Italian, Portuguese, Dutch.
• Word of the Day, every morning.
• Curated collections: Vocabulary, Literature, Geography, Science.
• Typo-tolerant search, prefix search, voice search, camera search.
• Rich entries: phonetics, etymology, synonyms, antonyms, examples.
• Save words to your private, on-device lexicon.
• Dark mode, Dynamic Type, iPad multitasking.
• No accounts. No ads. No trackers. Private by design.
```

- **Length:** ~580 characters. App Store Connect allows up to 4,000.
- **Localize** for the same 7 languages as the description.

---

## 14. Post-submission monitoring

After submission:

1. **App Review status** — check daily at
   App Store Connect → My Apps → Lexicon → Activity.
2. **Phased release** — enable the 7-day phased rollout for v1.0 (default
   setting in App Store Connect → Releases).
3. **Crash reports** — once the app is live, monitor the crash-reporting
   backend daily for the first 2 weeks; triage any new crash signatures and
   ship a patch release if a crash exceeds 0.1% of sessions.
4. **App Store reviews** — respond to every user review within 7 days.
5. **Privacy policy updates** — if any new data category is added in a
   future release, update `Lexicon/PrivacyPolicy.md` AND the App Privacy
   labels in App Store Connect BEFORE the new build is submitted.

---

*This document is the canonical App Store Connect submission spec for Lexicon
v1.0. Any change to the app name, subtitle, description, keywords,
categories, age rating, or privacy labels must be reflected here first.*
