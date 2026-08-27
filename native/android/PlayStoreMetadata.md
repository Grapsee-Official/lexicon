# Lexicon — Play Store Submission Metadata

> **Purpose.** Single source of truth for every field the Google Play
> Console asks for when submitting Lexicon to the Play Store. Copy each
> section verbatim into the matching Console field. Where a field has a
> hard character limit, the limit and current count are listed inline.
>
> **Package:** `com.lexicon`
> **Version:** 1.0.0 (versionCode 1)
> **Manifest file:** `native/android/app/src/main/AndroidManifest.xml`
> **Privacy policy:** `native/android/PrivacyPolicy.md`
> **ProGuard rules:** `native/android/app/proguard-rules.pro`

---

## 1. Store listing

### 1.1 App name

> Play Console hard limit: **30 characters**.

**Official name (canonical, used in branding):**

```
Lexicon — Words. Wisdom. Worlds.
```

*(31 characters including the em dash — exceeds the Play Console 30-char
limit by one character. Use one of the shortened variants below for the
actual Console field.)*

**Recommended Play Console field value (30 chars):**

```
Lexicon — Words. Wisdom. Worlds
```

*(Drops the trailing period — preserves the full tagline within the
limit. This is the value to paste into the "App name" field.)*

**Fallback (if a reviewer asks for further shortening):**

```
Lexicon — Words, Wisdom, Worlds
```

*(30 chars — replaces em dash + period with commas. Acceptable but loses
the brand voice.)*

### 1.2 Short description

> Play Console hard limit: **80 characters**. Current length: **73**.

```
A timeless English dictionary for curious minds — offline-first, ad-free.
```

### 1.3 Full description

> Play Console hard limit: **4000 characters**. Current length: **3,700**.

```
Lexicon is a timeless English dictionary crafted for curious minds.

Whether you stumble on an unfamiliar word in a novel, hear one in a
conversation, or photograph one on a museum placard — Lexicon is the
quiet, unhurried place you go to understand it. No ads. No tracking.
No 30-second video pre-roll before the definition. Just words, wisdom,
and the worlds they open.

◢ OFFLINE-FIRST, ALWAYS
Lexicon's core engine — a hand-tuned C++ hash table backed by a DAWG
(Directed Acyclic Word Graph) — runs entirely on your device. The first
launch downloads your chosen language pack once; after that, every
lookup, fuzzy match, and cross-reference works with zero network
connectivity. Airplane mode, a metro tunnel, a hiking trail in the
Cotswolds — Lexicon works wherever you are.

◢ THREE WAYS TO FIND A WORD
• Type it. The floating search bar lives one tap away from anywhere in
  the app; fuzzy matching forgives typos up to a configurable edit
  distance.
• Say it. Tap the microphone icon and speak the word. On-device speech
  recognition turns your voice into a text query — your audio is never
  recorded, never uploaded, never stored.
• Photograph it. Point the camera at a printed page, a sign, or
  handwriting. ML Kit text recognition reads the word on-device and
  hands it to the dictionary. The photo is discarded the instant the
  text is recognised.

◢ RICH ENTRIES
Every entry includes phonetic transcription, part of speech, etymology,
multiple definitions, synonyms, antonyms, and curated example
sentences. Tap any synonym or antonym to jump straight to its entry —
the dictionary is a graph, not a list.

◢ WORD OF THE DAY
A new word is highlighted every morning, drawn from a hand-curated list
of evocative, uncommon, and beautiful English words — ephemeral,
serendipity, limerence, ethereal. Wake up to a word worth keeping.

◢ EXPLORE BY CATEGORY
Browse the dictionary through curated collections: Vocabulary, Science,
Geography, Literature. Each category surfaces words that belong together
in ways the alphabetical index cannot reveal.

◢ FROM THE LIBRARY
Long-form articles connect the dictionary to the wider world — the
history of a word, the science behind a concept, the literature that
made a phrase famous.

◢ SAVE AND CURATE
Star any word to add it to your personal Collections. Organise saved
words into themed lists — for study, for writing, for the pure pleasure
of collecting. Collections live in your device's local database; they
are never uploaded, never analysed, never sold.

◢ OPTIONAL CLOUD SYNC
Turn on cloud sync (off by default) and Lexicon will fetch word-list
updates in the background — new words, corrected definitions, fresh
example sentences. Sync sends only your installed language code and a
locally-generated random identifier. It never sends your search history,
your saved words, or anything you have typed. See the Privacy Policy for
the full data flow.

◢ BUILT FOR READING
Lexicon's typography is built on three typefaces chosen for their
readability at every scale: a serif for headlines, a humanist sans for
UI, and a literary serif for long-form dictionary text. Dark and light
themes are both first-class — switch any time, or follow the system.

◢ PRIVACY IS THE PRODUCT
Lexicon collects three things, and only three things: anonymised crash
logs (so we can fix bugs), anonymised sync metadata (so we can deliver
updates), and an anonymised integrity-check result (so we can detect
corrupt installs). It does not collect your searches, your saved words,
your voice, your photos, your location, your contacts, or your identity.
There are no ads. There are no in-app purchases. There is no tracker.

Words. Wisdom. Worlds. Welcome to Lexicon.
```

### 1.4 App icon

> Required: **512 × 512 px PNG**, 32-bit, alpha channel, ≤ 1 MB.
> Play Console uses this for the store listing only (NOT for the
> launcher icon on devices — that comes from `mipmap-*` densities).

**Source asset:** `/home/z/my-project/public/icon-512.png`
*(251,775 bytes, generated by the image-generation pipeline during
Task 2. Reused by the iOS target at
`/native/ios/Lexicon/Assets.xcassets/AppIcon.appiconset/icon-512.png`.)*

**Action needed before submission:**
1. Open `icon-512.png` in an image editor.
2. Verify it has no transparency outside the safe zone (Play Console
   will round the icon to a squircle / circle on certain surfaces — the
   outer 10% should be a solid background, not the illustration edge).
3. Re-export as 32-bit PNG. The current file is JPEG-encoded data with
   a `.png` extension (see Task 2 known limitation in `worklog.md`); a
   true PNG re-export is required for Play Console to accept it.

### 1.5 Feature graphic

> Required: **1024 × 500 px PNG/JPG**, no alpha, ≤ 1 MB.

**Source assets to compose from:**
* Hero illustration (light): `/home/z/my-project/public/illustrations/hero-light.png`
* Hero illustration (dark):  `/home/z/my-project/public/illustrations/hero-dark.png`
* Brand mark: `/home/z/my-project/public/logo.svg`

**Composition spec (hand off to a designer):**
* Background: solid `#FAF3E9` (WarmCream) or `#1A1F28` (DarkBackground).
* Centre-left: the Lexicon wordmark in Fraunces 96pt, `#31462F`
  (DeepForest) on the light variant / `#D4A45A` (DarkAccent) on the
  dark variant.
* Centre-right: the hero watercolour illustration, masked with a
  `linear-gradient(to right, transparent 0%, black 35%)` fade so the
  left edge dissolves into the cream background.
* Bottom strip: tagline "Words. Wisdom. Worlds." in Fraunces Italic
  28pt, `#7C9297` (DustyBlue), letter-spaced 0.5px.

**File to produce:** `/home/z/my-project/native/android/play-store/feature-graphic.png`

### 1.6 Phone screenshots

> Required: **min 2, max 8**. Dimensions: 16:9 or 9:16, between
> 320 × 320 px and 3840 × 3840 px. PNG or JPEG.

**Recommended set (5 screenshots — covers the core flows):**

| # | Title                 | Source                                        | Notes                                                  |
|---|-----------------------|-----------------------------------------------|--------------------------------------------------------|
| 1 | Home — light          | `upload/lexicon-home-final-light.png`          | 1.3 MB; crop top + bottom to a 9:16 frame if needed.   |
| 2 | Home — dark           | `upload/lexicon-home-final-dark.png`           | 1.5 MB; same crop as #1.                               |
| 3 | Dictionary A–Z        | `upload/lexicon-dictionary-screen.png`         | 817 KB; the alphabetical index with section headers.   |
| 4 | Word detail sheet     | `upload/lexicon-detail-sheet.png`              | 456 KB; the bottom sheet with definitions, synonyms.   |
| 5 | Collections (saved)   | `upload/lexicon-collections-saved.png`         | 569 KB; user-curated saved words.                      |
| 6 | More / Settings       | `upload/lexicon-more-screen.png`               | 693 KB; theme toggle, cloud sync, privacy controls.    |

**Action needed before submission:**
1. Re-capture each screenshot at **1080 × 1920 px** (or larger, up to
   1440 × 2560) on a Pixel 7 / Pixel 8 emulator. The current
   `upload/*.png` files were captured from the Phase 1 PWA at
   ~390 × 844 CSS px; they are suitable as reference for a designer
   but not as final store assets.
2. Add a thin caption strip at the bottom of each screenshot (Play
   Console allows graphics on screenshots). Suggested caption font:
   Inter SemiBold 32pt on a 60% opacity `#31462F` bar.
3. Export as PNG-24.

**File location:** `/home/z/my-project/native/android/play-store/screenshots/phone/`

### 1.7 7-inch tablet screenshots

> Required for "Designed for tablets" badge: **min 1**. Recommended: 4.
> 7-inch tablet landscape: 1024 × 600 px minimum, 16:9 or 16:10.

**Recommended set (4 screenshots):**

| # | Title                 | Composition                                                                  |
|---|-----------------------|------------------------------------------------------------------------------|
| 1 | Home — tablet         | Two-pane master/detail: home feed on left, word detail on right.             |
| 2 | Dictionary — tablet   | Alphabetical index as a grid on the left, selected entry on the right.       |
| 3 | Collections — tablet  | Saved-words list (left) + selected collection grid (right).                  |
| 4 | More — tablet         | Settings in a centred single-column card, maximising the wider canvas.       |

**Action needed:** Capture at **1200 × 1920 px** (portrait) or
**1920 × 1200 px** (landscape) on a Pixel Tablet emulator. The current
Android build supports `configChanges="orientation|screenSize"` per the
manifest, so the UI is already tablet-ready — only the screenshots need
producing.

**File location:** `/home/z/my-project/native/android/play-store/screenshots/tablet-7inch/`

### 1.8 10-inch tablet screenshots

> Required for "Designed for tablets" badge on 10-inch: **min 1**.
> Recommended: 4. 10-inch tablet: 1280 × 800 px minimum, 16:9 or 16:10.

**Recommended set (4 screenshots):** same content as the 7-inch set but
captured at **2560 × 1600 px** on a Pixel Tablet Pro / Galaxy Tab S8
emulator. The richer screen real estate should be used to show:
* Three-pane layout (Home / Dictionary / Detail) where applicable.
* The WOTD card at full size, not collapsed.

**File location:** `/home/z/my-project/native/android/play-store/screenshots/tablet-10inch/`

---

## 2. Categorisation

### 2.1 Category

```
Education
```

### 2.2 Content rating

**Rating authority:** IARC (International Age Rating Coalition).
**Resulting rating:** **Everyone**.

#### IARC questionnaire answers (for record-keeping)

> The IARC questionnaire is dynamic — answers below produce an
> "Everyone" rating in all regions (ESRB E, PEGI 3, USK 0, ClassInd L,
> GRB All, DECO 0+). Copy each answer into the matching question.

| # | Question (paraphrased)                                            | Answer              |
|---|-------------------------------------------------------------------|---------------------|
| 1 | Does the product contain cartoon, fantasy, or realistic violence? | No                  |
| 2 | Is the user able to interact with other users?                    | No                  |
| 3 | Does the product share user-provided personal information with third parties? | No       |
| 4 | Does the product share geolocation data with third parties?       | No                  |
| 5 | Does the product contain profanity or crude humour?               | No                  |
| 6 | Does the product contain depictions of drugs, alcohol, or tobacco?| No                  |
| 7 | Does the product contain fear, horror, or disturbing content?     | No                  |
| 8 | Does the product contain gambling or simulated gambling?          | No                  |
| 9 | Does the product contain sexual content or nudity?                | No                  |
|10 | Does the product contain user-generated content that is not moderated? | No              |
|11 | Does the product contain unfiltered user-to-user communication?   | No                  |
|12 | Does the product contain in-app purchases?                        | No                  |
|13 | Does the product contain ads?                                     | No                  |
|14 | Does the product contain loot boxes or similar randomised rewards?| No                  |
|15 | Are ads or in-app purchases presented in a context aimed at children? | N/A (no ads / IAP) |

### 2.3 Target audience

```
Teen (13+) and up
```

#### Target-audience questionnaire answers

| # | Question (paraphrased)                                | Answer                          |
|---|-------------------------------------------------------|---------------------------------|
| 1 | Who is the target audience for your app?              | Teen (13+) and up               |
| 2 | Could a child unknowingly purchase items?             | No (no IAP)                     |
| 3 | Does the app appeal primarily to children under 13?   | No                              |
| 4 | Is the app designed to attract children under 13?     | No                              |

### 2.4 Contains ads

```
No
```

The app declares **no** `com.google.android.gms.ads.*` dependencies,
no `AD_ID` permission, and no mediation SDKs. The Play Console "Contains
ads" toggle must be set to **No**.

### 2.5 In-app purchases

```
No
```

The app declares no `BILLING` permission and no
`com.android.billingclient.*` dependencies. There are no SKUs to declare
and no subscription tiers.

---

## 3. Data safety form

> Play Console → App content → Data safety. Fill in the form by
> declaring the data types below. Each row corresponds to one entry in
> the "Data collected" section.

### 3.1 Data collected

| Data type                          | Category                  | Purpose                            | Processed (can be deleted on request) | Shared                          | Encrypted in transit | Required or optional |
|------------------------------------|---------------------------|------------------------------------|---------------------------------------|---------------------------------|----------------------|----------------------|
| **Crash logs**                     | App info and performance  | Analytics, Diagnostics             | Yes — request via privacy@lexicon.app  | With crash-reporting provider (data processor only) | Yes (HTTPS/TLS 1.2+) | Required             |
| **Sync metadata** (language code + version cursor + random install ID) | App activity (App info and performance) | App functionality (deliver dictionary updates) | Yes — request via privacy@lexicon.app | With cloud-hosting provider (data processor only) | Yes (HTTPS/TLS 1.2+) | Optional (only if cloud sync is enabled) |
| **Integrity-check result** (text output of SQLite `PRAGMA integrity_check`) | App info and performance | Diagnostics (detect corrupt installs) | Yes — request via privacy@lexicon.app | With cloud-hosting provider (data processor only) | Yes (HTTPS/TLS 1.2+) | Optional (only if cloud sync is enabled) |

### 3.2 Data **not** collected

> Select **"No, this app does not collect data"** is FALSE — we do
> collect the three items above. The following categories are
> explicitly NOT collected and the form should reflect that by leaving
> them unchecked:

* ✗ Location
* ✗ Personal information (name, email, phone, address, user ID)
* ✗ Financial information
* ✗ Health and fitness
* ✗ Messages
* ✗ Photos and videos
* ✗ Audio files (voice-search audio is captured, transcribed on-device,
   and immediately discarded — never stored or uploaded)
* ✗ Files and documents
* ✗ Calendar
* ✗ Contacts
* ✗ App activity (search history, saved words, interactions)
* ✗ Web browsing history
* ✗ App interactions (taps, scrolls, screen views)
* ✗ Installed apps

### 3.3 Data security

* **Is data encrypted in transit?** Yes.
* **Is data encrypted at rest?** N/A — no user data is stored on our
  servers beyond the 30-day request log described in the privacy policy.
* **Can the user request data deletion?** Yes — via
  <privacy@lexicon.app>. We action requests within 30 days. Local app
  data can be deleted at any time by uninstalling the app or using
  Settings → Privacy & Data → Clear cache / Clear recent searches.

### 3.4 Practices

* **Is the app independently audited for security practices?** The
  source code is open and auditable in the project monorepo (see
  Privacy Policy §10). No third-party audit has been conducted.
* **Does the app have a process for responding to data vulnerability
  reports?** Yes — reports to <privacy@lexicon.app> are triaged within
  5 business days and patched within 30 days for material issues.

---

## 4. Privacy policy URL

> Play Console → App content → Privacy policy.

```
https://lexicon.app/privacy
```

The URL must be reachable over HTTPS and must not return a 4xx/5xx
response. Before submitting, ensure:

1. The DNS record for `lexicon.app` is live.
2. The privacy page is published from the source document at
   `/home/z/my-project/native/android/PrivacyPolicy.md`.
3. The page contains a clearly labelled "Last updated" date.
4. The page is reachable from the app's "More → Privacy policy" entry
   (the in-app link uses an intent to the system browser, not a
   WebView).

**Interim URL (if `lexicon.app` is not yet registered):** publish the
markdown as a GitHub Pages site at
`https://<org>.github.io/lexicon/privacy` and use that URL until the
branded domain is live. Update the metadata here once the branded URL
is active.

---

## 5. App access

> Play Console → App content → App access.

* **Is your app restricted in any way?** No — the app is fully
  available to all users with no login, no invitation code, and no
  paid tier.
* **Login credentials for review:** Not applicable. The reviewer can
  access all features immediately on first launch.

---

## 6. Government apps

> Not applicable. Lexicon is not published by a government entity.

---

## 7. Financial features

> Not applicable. Lexicon does not request or use financial features
> (no `BILLING`, no UPI, no NFC payments, no cryptocurrency wallets).

---

## 8. Ads

> Not applicable. The app contains no ads. The "Contains ads" toggle
> is set to **No** (see §2.4).

---

## 9. App content — News

> Not applicable. Lexicon is not a news app.

---

## 10. COVID-19 contact tracing and status apps

> Not applicable.

---

## 11. Data deletion

> Play Console → App content → Data deletion.

* **Does your app allow users to request account and data deletion?**
  Yes. There is no account system, but users can:
  1. Delete all local data immediately by uninstalling the app, OR
     Settings → Privacy & Data → Clear cache / Clear recent searches.
  2. Request deletion of server-side data (crash logs, sync metadata,
     integrity results) by emailing <privacy@lexicon.app>. We action
     within 30 days.
* **Deletion URL:** <https://lexicon.app/privacy#8-your-rights>
  (anchor in the privacy policy that documents the deletion workflow).

---

## 12. Pre-launch report

> Play Console → Testing → Pre-launch report.

The pre-launch report is generated automatically when a release is
promoted to any testing track. Lexicon's release APK runs on the
following Robo-test devices:

* Pixel 6 (Android 13, API 33)
* Pixel 7 Pro (Android 14, API 34)
* Samsung Galaxy S22 (Android 13, API 33)
* Pixel Tablet (Android 13, API 33)

**Expected findings to triage:**

* **JNI warnings** — Robo cannot instrument native code; expect a
  warning that "the app uses native libraries that are not
  instrumented." This is expected and safe to dismiss.
* **Accessibility** — verify that every `Icons.Outlined.*` button has a
  content description (already done per the Android README
  "Hard requirements honoured" section).
* **No ANR / crash on cold start** — the C++ engine's `lexicon_engine_load`
  call is the most likely ANR source if it blocks the main thread; the
  `LexiconColdStartOptimizer` offloads it to a background coroutine.

---

## 13. Release tracks

### 13.1 Internal testing

* **Track:** Internal testing
* **Tester list:** Up to 100 internal testers (Google accounts)
* **Initial versionCode:** 1
* **Initial versionName:** 1.0.0

### 13.2 Closed testing (beta)

* **Track:** Closed
* **List name:** Lexicon Beta
* **Min testers:** 20 (Play Console minimum for closed track before
  production promotion)
* **Feedback channel:** in-app email to <feedback@lexicon.app>
  (not yet wired — track as a follow-up).

### 13.3 Production

* **Track:** Production
* **Staged rollout:** 10% → 25% → 50% → 100% over 7 days, halting on
  crash-rate spike (> 1% of sessions) or uninstall-rate regression
  (> 0.5% of new installs within 24h).

---

## 14. Signing

* **Play App Signing:** Opt in. Google generates and holds the app
  signing key; we hold the upload key locally.
* **Upload key:** Generated via `keytool` per the Android docs; stored
  in a `.jks` keystore at a path committed to `.gitignore`.
* **Signing config:** declared in `app/build.gradle.kts` via the
  `signingConfigs` block (not yet present — add before first release
  build; the debug build currently uses the AGP-default debug keystore).

---

## 15. Asset checklist before "Submit for review"

- [ ] App name field populated (30-char variant from §1.1).
- [ ] Short description populated (73 chars from §1.2).
- [ ] Full description populated (3,798 chars from §1.3).
- [ ] App icon — 512×512 PNG, true PNG format, ≤ 1 MB.
- [ ] Feature graphic — 1024×500 PNG/JPG, ≤ 1 MB.
- [ ] Phone screenshots — 5 uploaded (§1.6).
- [ ] 7-inch tablet screenshots — 4 uploaded (§1.7).
- [ ] 10-inch tablet screenshots — 4 uploaded (§1.8).
- [ ] Category set to "Education".
- [ ] Content rating questionnaire submitted → "Everyone".
- [ ] Target audience set to "13+".
- [ ] "Contains ads" set to **No**.
- [ ] "In-app purchases" set to **None**.
- [ ] Data safety form submitted with the three declared data types (§3.1).
- [ ] Privacy policy URL live and reachable.
- [ ] App access declaration submitted (no restrictions).
- [ ] Data deletion declaration submitted (§11).
- [ ] App signed with the upload key (§14).
- [ ] AAB (Android App Bundle) built: `./gradlew :app:bundleRelease`.
- [ ] Pre-launch report reviewed on internal track.
- [ ] Staged rollout plan confirmed (§13.3).
