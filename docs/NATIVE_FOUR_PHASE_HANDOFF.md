# Lexicon — Four-phase native implementation handoff

The ten approved countryside screens remain the sole visual scope. **No new pages or branches.** The primary source of visual truth is the owner's daylight watercolor and moonlit English-countryside references; the owner alone performs the final physical-device visual audit.

## Phase 1 — Home and Dictionary
- Updated both native Home heroes for narrow screens and large text. Decorative scenery is no longer an invisible search button.
- Rebuilt Android Compose and iOS SwiftUI Dictionary Home around the reference's village header, search bar, popular chips, illustrated books quotation, recent words, quick-access tiles, and landscape strip.
- Dictionary Quick Access Favorites and Bookmarks now go to their corresponding filtered Collections state.
- The Home Word of the Day card and the dedicated detail use the same UTC featured-word selection on Android and iOS.
- Corrected night starfield/hero layering and raised Search button in previous native commits.

## Phase 2 — Collections, Notes, Lists, Templates
- Collections filters, saved/favorite real counts and personal notes still use native stored data.
- Notes support creation from quick note templates, editing, bookmarking, search and deletion.
- Custom study lists can be created from valid offline dictionary words or from the user's bookmarks. A list has a real word count; unknown terms are not silently counted.
- Custom lists and review progress persist on both platforms (Android SharedPreferences JSON; iOS UserDefaults Codable).
- New list, reset progress, delete custom list and practice flows have native controls. Today’s Study chooses the first unfinished list rather than hard-coding an ID.
- Templates can be selected, starred and used to write a note; users can create and persist custom templates.

## Phase 3 — Discovery and word exploration
- Search suggestions open the intended word instead of re-running filters that could remove the selection.
- Search Results show an honest empty state for unmatched words rather than pretending an unrelated word matched.
- Recent searches, search filters, idiom discovery, and available translation panels are functional.
- Translation grids respond with one column on narrow Android displays and adaptive columns on iOS.
- Fixed expandable iOS translation state so the panel can collapse again.
- Added native spoken English word pronunciation on Word Detail and Search Results (Android TextToSpeech / iOS AVSpeechSynthesizer).

## Phase 4 — Responsive, assets, CI and distribution
- Shared countryside headers grow for accessibility text; navigation retains Home / Dictionary / raised Search / Collections / More.
- Both native apps use matching scenic asset names; `scripts/check_countryside_art.py` verifies day/night twins, assets and ten screen route families.
- Original full-scene watercolor paintings remain raster; the native editable night-sky ornament uses proper Canvas/SwiftUI drawing and SVG source.
- GitHub Actions builds Android **Release** APK and iOS **Release simulator** binary (plus C++). APK publication occurs only after both build jobs pass and the Android APK is signed with the stable GitHub Actions keystore secrets.
- The release pipeline updates the single `lexicon-latest` GitHub Release asset without a description. Superseded main-branch builds are cancelled instead of publishing intermediate APK versions.

## Honest remaining boundaries
- **This handoff does not claim pixel-perfect device accuracy.** User performs the final visual review on physical devices.
- **The watercolor paintings are not fully editable vectors**, and an SVG containing an embedded PNG is not vector art. The editable ornament does not magically vectorize paintings.
- Dictionary/translation coverage currently includes offline embedded/curated data; **36 complete language translation packs are not delivered by these ten UI phases**.
- Native text-to-speech output quality depends on the installed Android TTS engine or Apple's available voices.
- An iOS simulator build is **not** an installable, signed iOS IPA. Real iOS distribution needs Apple signing credentials and device provisioning separately.

## Owner's concise on-device checklist
1. Light and dark Home: paper/ink contrast, midnight cottage warmth, glowing accents, raised floating Search.
2. Dictionary: cottage position, readable serif titles, spacing, horizontal popular chips and Quick Access destinations.
3. Word of the Day and Word Detail: identical daily selection, pronunciations, definitions, etymology and decorative watercolor behind text.
4. Collections/Notes: appropriate filtering, new/edit/delete note, bookmarks, graceful empty states, botanical card backgrounds.
5. Study Lists: create known-word list, review an answer, persist progress, reset or delete custom list, test after app relaunch.
6. Quick Templates: start a draft, star items, create a template, reopen after relaunch.
7. Search/Results/Expanded: matched/unmatched words, idioms, translation expansion/collapse, compact-width behavior.
8. Responsive states: small device, large accessibility font, tablet/large phone, portrait/landscape as available, safe area and keyboard.
9. iOS vs Android: compare daylight/nighttime visual character and navigation behaviours—not just matching files.
