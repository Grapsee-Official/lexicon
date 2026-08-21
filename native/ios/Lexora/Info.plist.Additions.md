# Lexora — Info.plist Additions (Submission-Ready Checklist)

This document describes the keys that **must** be present in
`/native/ios/Lexora/Info.plist` before Lexora is submitted to App Store
Connect. It is the canonical reference for the four additions called out in
task **P4-06**.

> **Current state of `Info.plist`:** the file ships with a complete baseline
> (bundle identifiers, display name, orientations, launch screen, status bar
> config, `ITSAppUsesNonExemptEncryption = false`). The two permission-string
> keys (`NSCameraUsageDescription`, `NSMicrophoneUsageDescription`) are
> already present in the file **but commented out** — see §2 and §3 for the
> uncomment instructions.

---

## Table of contents

1. [Overview — at-a-glance status table](#1-overview--at-a-glance-status-table)
2. [`NSCameraUsageDescription` — camera search](#2-nscamerausagedescription--camera-search)
3. [`NSMicrophoneUsageDescription` — voice search](#3-nsmicrophoneusagedescription--voice-search)
4. [`ITSAppUsesNonExemptEncryption = false` — encryption export compliance](#4-itsappusesnonexemptencryption--false--encryption-export-compliance)
5. [`CFBundleVersion` auto-increment — build-phase script](#5-cfbundleversion-auto-increment--build-phase-script)
6. [Applying the changes — step-by-step](#6-applying-the-changes--step-by-step)
7. [Verification checklist](#7-verification-checklist)

---

## 1. Overview — at-a-glance status table

| # | Key | Required for | Current state in `Info.plist` | Action |
| --- | --- | --- | --- | --- |
| 1 | `NSCameraUsageDescription` | Camera search (Phase 3) | Present, **commented out** (lines 110–111) | Uncomment before submission |
| 2 | `NSMicrophoneUsageDescription` | Voice search (Phase 3) | Present, **commented out** (lines 112–113) | Uncomment before submission |
| 3 | `ITSAppUsesNonExemptEncryption = false` | Encryption export compliance (App Store Connect) | **Already set** (lines 100–102) | None — verify only |
| 4 | `CFBundleVersion` auto-increment | Build version management | Static value `"1"` (lines 30–31) — no auto-increment | Add Run-Script build phase (see §5) |

### Submission-readiness verdict

- For **v1.0 submission with Phase 3 features enabled** (camera + voice
  search): **4 changes needed** — uncomment items 1 & 2, verify item 3, add
  the build-phase script for item 4.
- For **v1.0 submission with Phase 3 features disabled** (no camera / no
  voice search in v1.0): **2 changes needed** — verify item 3, add the
  build-phase script for item 4. Items 1 & 2 can stay commented out, but
  the `PrivacyPolicy.md` §6 (On-device processing features) and the
  `AppStoreMetadata.md` §11.3 (Notes for App Review) text would need to be
  trimmed accordingly.

---

## 2. `NSCameraUsageDescription` — camera search

### Why it is needed

Lexora's **camera search** feature (Phase 3) lets the user point the device
camera at a printed page, tap a recognised word, and look it up. The feature
uses Apple's on-device **Vision** framework (`VNRecognizeTextRequest`) to
detect text in the camera feed.

iOS requires any app that opens the camera to declare a
`NSCameraUsageDescription` string in `Info.plist`. Without this key, the
first call to `AVCaptureSession.startRunning()` crashes the app with:

```
This app has crashed because it attempted to access privacy-sensitive data
without a usage description. The app's Info.plist must contain an
NSCameraUsageDescription key with a string value explaining to the user how
the app uses this data.
```

App Review will **reject** the build outright if the camera is ever invoked
and this key is missing.

### The required key/value

```xml
<key>NSCameraUsageDescription</key>
<string>Lexora uses the camera to scan and look up words on the page in front of you. The camera feed is processed on-device only — no images are saved, and no images leave your device.</string>
```

### How to apply

The key is **already present in `Info.plist`** but commented out inside a
multi-line `<!-- ... -->` block (lines 109–116). To enable it:

1. Open `/native/ios/Lexora/Info.plist` in Xcode (right-click → Open As →
   Source Code) or any text editor.
2. Find the block starting at line 109:
   ```xml
   <!--
   <key>NSCameraUsageDescription</key>
   <string>Lexora uses the camera to scan and look up words.</string>
   ...
   -->
   ```
3. **Replace** that entire commented block (lines 109–116) with the
   uncommented, expanded versions below (this single edit enables both
   `NSCameraUsageDescription` and `NSMicrophoneUsageDescription` in one go —
   see §3 for the microphone half):

   ```xml
   <!-- Camera search — required by Phase 3 camera-search feature (Vision framework). -->
   <key>NSCameraUsageDescription</key>
   <string>Lexora uses the camera to scan and look up words on the page in front of you. The camera feed is processed on-device only — no images are saved, and no images leave your device.</string>

   <!-- Voice search — required by Phase 3 voice-search feature (Speech framework). -->
   <key>NSMicrophoneUsageDescription</key>
   <string>Lexora uses the microphone for voice search. A single short utterance is transcribed on-device and used as your search query — no audio is recorded, stored, or transmitted.</string>
   ```

### Important: replace the placeholder string

The current commented-out value is a short placeholder:
> "Lexora uses the camera to scan and look up words."

For submission, replace it with the longer, user-facing explanation shown
above. Apple's review guidelines (section 5.1.1) require the string to
**clearly explain what the camera is used for**. The expanded copy:

- States the user-visible purpose ("scan and look up words on the page in
  front of you").
- States the data-handling guarantee ("processed on-device only — no images
  are saved, and no images leave your device"), which is also a selling
  point in the Privacy Policy (`PrivacyPolicy.md` §6.2).

### Verification

After applying the change:

1. Build the app (`⌘B`).
2. Open the built `Info.plist` in the `.app` bundle:
   - In Xcode: **Window → Organizer → Products** → right-click the built
     `.app` → **Show in Finder** → right-click the `.app` → **Show Package
     Contents** → open `Info.plist` in a text editor.
3. Confirm `NSCameraUsageDescription` is present as a top-level key and its
   value matches the string above.
4. Confirm there is **no** `<!--` comment wrapper around the key.
5. Run the app on a device, tap the camera icon in the search bar, and
   confirm iOS shows the standard camera permission prompt with the new
   string.

---

## 3. `NSMicrophoneUsageDescription` — voice search

### Why it is needed

Lexora's **voice search** feature (Phase 3) lets the user tap a microphone
icon in the search bar, speak a word, and have it transcribed and inserted
into the search field. The feature uses Apple's on-device **Speech**
framework (`SFSpeechRecognizer` with `requestAuthorization(_:)` and
`SFSpeechAudioBufferRecognitionRequest`).

iOS requires any app that accesses the microphone to declare a
`NSMicrophoneUsageDescription` string in `Info.plist`. Without this key, the
first call to `AVAudioEngine.startRecording()` crashes the app with the same
privacy-sensitive-data error as for the camera.

App Review will **reject** the build if the microphone is ever invoked and
this key is missing.

### The required key/value

```xml
<key>NSMicrophoneUsageDescription</key>
<string>Lexora uses the microphone for voice search. A single short utterance is transcribed on-device and used as your search query — no audio is recorded, stored, or transmitted.</string>
```

### How to apply

Same as §2 — the key is already present in `Info.plist` but commented out.
The single edit in §2 enables both keys at once. Refer to §2 step 3 for the
exact replacement block.

### Additional: Speech Recognition permission

Voice search also uses the **Speech** framework, which requires a **separate**
permission from the microphone:

```xml
<key>NSSpeechRecognitionUsageDescription</key>
<string>Lexora uses speech recognition to transcribe your voice-search queries on-device.</string>
```

This key is **NOT** currently in `Info.plist`. If voice search is part of
v1.0, add `NSSpeechRecognitionUsageDescription` alongside
`NSMicrophoneUsageDescription`. (The user will see two consecutive iOS
permission prompts the first time they use voice search — one for the
microphone, one for speech recognition. This is standard iOS behaviour and
cannot be combined into a single prompt.)

### Verification

1. Build the app.
2. Inspect the built `Info.plist` per §2 step 2.
3. Confirm `NSMicrophoneUsageDescription` (and, if added,
   `NSSpeechRecognitionUsageDescription`) are present with the correct
   strings.
4. Run the app on a device, tap the microphone icon, and confirm iOS shows
   the microphone permission prompt followed by the speech-recognition
   permission prompt, both with the new strings.

---

## 4. `ITSAppUsesNonExemptEncryption = false` — encryption export compliance

### Why it is needed

United States export law requires apps that use encryption to file an
encryption registration with the U.S. Bureau of Industry and Security (BIS)
— **unless** the app's encryption qualifies for an exemption under Export
Administration Regulations (EAR) section 740.13(e).

The most common exemption — and the one Lexora qualifies for — is the
"mass-market exemption" for apps that **only** use standard system-provided
encryption (HTTPS/TLS via `URLSession`, `CryptoKit` for hashing, etc.) and
do **not** implement their own custom encryption, do **not** encrypt files
at rest with anything beyond iOS Data Protection, and do **not** add
encryption to non-Apple products.

When `ITSAppUsesNonExemptEncryption` is set to `false` in `Info.plist`,
App Store Connect treats the build as exempt and **skips** the encryption
export-compliance questionnaire at submission time. The app can be submitted
without uploading a self-classification report or registering with BIS.

If the key is **missing** or set to `true`, App Store Connect will refuse
to process the build until the encryption questionnaire is filled in — and
that questionnaire asks about every encryption algorithm the app uses,
including system-provided ones.

### Current state

✅ **Already present and correctly set.** Lines 100–102 of `Info.plist`:

```xml
<!-- App does not use exempt encryption — keeps App Store review simple. -->
<key>ITSAppUsesNonExemptEncryption</key>
<false/>
```

> The comment on line 100 is slightly misleading — it should read "App does
> not use **non-exempt** encryption" (i.e. the app uses **only** exempt
> encryption). The XML value is correct; only the comment wording is off.
> A minor cosmetic fix is recommended (not required):

```xml
<!-- App uses only exempt (system-provided) encryption — keeps App Store review simple. -->
```

### Verification

1. Build the app.
2. Inspect the built `Info.plist` per §2 step 2.
3. Confirm `ITSAppUsesNonExemptEncryption` is present and set to `<false/>`.
4. (Optional) Run `plutil -p Info.plist | grep -i encrypt` from the
   terminal against the built `.app`'s `Info.plist` to confirm the key is
   present in the compiled (binary) plist, not just the source XML.

### When this key would need to change

If a future version of Lexora adds:

- **End-to-end encryption** of user data with a key not derived from the
  device passcode, OR
- **Custom encryption** (e.g. AES with a Lexora-controlled key for some
  feature), OR
- **File-level encryption at rest** with anything beyond iOS Data Protection
  classes (`NSFileProtectionComplete`, etc.),

then `ITSAppUsesNonExemptEncryption` must be set to `true` and the
encryption export-compliance questionnaire must be filled in at submission
time. None of these apply to Lexora v1.0.

> **Note on Lexora's actual crypto usage:** Lexora uses TLS 1.3 (system-
> provided by `URLSession`) for sync-metadata and crash-report transport,
> and certificate pinning (system-provided via `URLSessionDelegate`). Both
> are exempt. The app does **not** use CryptoKit, does **not** ship a
> custom crypto library, and does **not** encrypt files at rest beyond iOS
> Data Protection defaults (the on-device SQLite database uses the default
> `NSFileProtectionCompleteUntilFirstUserAuthentication` class, which is
> exempt). Conclusion: exemption holds.

---

## 5. `CFBundleVersion` auto-increment — build-phase script

### Why it is needed

App Store Connect requires every uploaded build to have a **unique**
`CFBundleVersion` (the "build number"). If two builds have the same
`CFBundleVersion`, the second upload is rejected with:

```
ERROR ITMS-4238: "Redundant Binary Upload. There already exists a binary
upload with build version '1' for train '1.0.0'"
```

The human-readable version (`CFBundleShortVersionString`, e.g. `"1.0.0"`)
is what users see in the App Store. The build number (`CFBundleVersion`)
is an internal counter that must increment on every upload.

Lexora's `Info.plist` currently has:

```xml
<key>CFBundleShortVersionString</key>
<string>1.0.0</string>

<key>CFBundleVersion</key>
<string>1</string>
```

If a developer does TestFlight builds during development and then tries to
upload the final 1.0.0 build with `CFBundleVersion=1`, the upload will fail
if any earlier 1.0.0 build with `CFBundleVersion=1` was ever uploaded.

### The solution: a Run-Script build phase that auto-increments `CFBundleVersion`

The standard pattern is to add a **Run-Script Phase** to the Xcode target
that bumps `CFBundleVersion` by 1 on every build. The script edits the
source `Info.plist` in place before the build copies it into the `.app`
bundle.

> **Why not use `agvtool`?** `agvtool new-version -all N` is the older Apple
> tool for bumping build numbers, but it requires the project to have
> "Versioning System: apple-generic" set in Build Settings, and it modifies
> `project.pbxproj` rather than `Info.plist` directly. The Run-Script
> approach below is more transparent and works without that setting.

### The script

Save this as `/native/ios/Lexora/Scripts/bump-build-number.sh` (create the
`Scripts/` directory if it does not exist):

```bash
#!/bin/bash
# bump-build-number.sh
# Run-Script build phase: increments CFBundleVersion in Info.plist by 1.
# Pasted into Xcode → Target → Build Phases → Run Script Phase.

# Resolve the source Info.plist path relative to the project.
# ${SRCROOT} is the directory containing the .xcodeproj.
INFO_PLIST="${SRCROOT}/Lexora/Info.plist"

if [ ! -f "${INFO_PLIST}" ]; then
    echo "warning: Info.plist not found at ${INFO_PLIST} — skipping build-number bump."
    exit 0
fi

# Read the current build number.
BUILD_NUM=$(/usr/libexec/PlistBuddy -c "Print :CFBundleVersion" "${INFO_PLIST}" 2>/dev/null)

# Skip if missing or non-numeric.
if ! [[ "${BUILD_NUM}" =~ ^[0-9]+$ ]]; then
    echo "warning: CFBundleVersion is '${BUILD_NUM}' (not a positive integer) — resetting to 1."
    /usr/libexec/PlistBuddy -c "Set :CFBundleVersion 1" "${INFO_PLIST}"
    exit 0
fi

# Increment.
NEW_BUILD_NUM=$((BUILD_NUM + 1))
/usr/libexec/PlistBuddy -c "Set :CFBundleVersion ${NEW_BUILD_NUM}" "${INFO_PLIST}"

echo "Bumped CFBundleVersion: ${BUILD_NUM} → ${NEW_BUILD_NUM}"
```

Make it executable:

```bash
chmod +x /native/ios/Lexora/Scripts/bump-build-number.sh
```

### How to wire it into the Xcode target

1. Open `Lexora.xcodeproj` in Xcode.
2. Select the **Lexora** target in the project navigator.
3. Go to **Build Phases** tab.
4. Click **+** → **New Run Script Phase**.
5. Drag the new phase to be the **first** item in the list (above "Compile
   Sources"). The script must run before the `Info.plist` is processed into
   the built `.app` bundle.
6. Configure the phase:
   - **Name:** `Bump Build Number`
   - **Shell:** `/bin/sh`
   - **Script:**
     ```bash
     "${SRCROOT}/Lexora/Scripts/bump-build-number.sh"
     ```
   - **Input files:** leave empty.
   - **Output files:** leave empty.
   - Uncheck **"Run script only when installing"** — the script should run
     on every build (so TestFlight builds also increment). If you only want
     to increment on Archive, leave it checked and use `agvtool` from the
     command line for ad-hoc increments.
7. **⌘B** to build — the build log should show:
   ```
   Bumped CFBundleVersion: 1 → 2
   ```

### Verifying the auto-increment

After building:

1. **In Xcode build log:** search for "Bumped CFBundleVersion" — the line
   should show the old → new value.
2. **In the source `Info.plist`:** open `/native/ios/Lexora/Info.plist` and
   confirm `CFBundleVersion` has been bumped (e.g. from `1` to `2`).
3. **In the built `.app`'s `Info.plist`:**
   ```bash
   plutil -p ~/Library/Developer/Xcode/DerivedData/Lexora-*/Build/Products/Debug-iphonesimulator/Lexora.app/Info.plist | grep CFBundleVersion
   ```
   Should show the new value.
4. **In an uploaded TestFlight build:** App Store Connect → Lexora →
   TestFlight → Builds should show the new build number alongside the
   upload timestamp.

### Alternative: increment only on Archive

If you don't want every debug build to bump the number (which can lead to
build numbers in the thousands after a few weeks of dev work), gate the
script on the `CONFIGURATION`:

```bash
# Only bump on Release (Archive) builds.
if [ "${CONFIGURATION}" != "Release" ]; then
    echo "Skipping build-number bump for ${CONFIGURATION} build."
    exit 0
fi
```

Add this block at the top of `bump-build-number.sh`, immediately after the
`INFO_PLIST=...` line.

### Alternative: use a Git-based build number

For a deterministic, reproducible build number that doesn't drift between
developer machines, derive the build number from the Git commit count:

```bash
BUILD_NUM=$(git rev-list --count HEAD)
/usr/libexec/PlistBuddy -c "Set :CFBundleVersion ${BUILD_NUM}" "${INFO_PLIST}"
```

This requires every developer to fetch the full Git history before building.
It also means the build number is meaningful (it maps 1:1 to a Git commit),
which is useful for crash-report triage.

### Important: do NOT check the bumped `Info.plist` into Git

Every developer's local `Info.plist` will diverge after the first build (one
dev's `CFBundleVersion=42`, another's `=15`). To avoid merge noise, either:

- **Add `Info.plist` to `.gitignore`** and ship a `Info.plist.template` that
  is copied to `Info.plist` on first build, OR
- **Use the Git-based build-number approach** (deterministic across
  machines — no `Info.plist` divergence), OR
- **Accept the merge noise** and resolve conflicts by always keeping the
  higher number.

The recommended approach for Lexora v1.0 is the **Git-based** one, because
it gives a meaningful crash-to-commit mapping (see `PrivacyPolicy.md` §4.1 —
crash reports include `CFBundleVersion`, which now maps directly to a Git
commit count, making crash triage trivial).

### Why this can't be done purely in `Info.plist`

`CFBundleVersion` is a static value in `Info.plist`. There is no
`Info.plist` mechanism (no variable, no placeholder, no preprocessor macro)
that auto-increments a value at build time. The increment must happen
either:

- **At build time**, via a Run-Script phase that edits `Info.plist` before
  the `ProcessInfoPlistFile` build phase copies it into the `.app` bundle
  (the approach above), OR
- **At archive time**, via a custom `.xcconfig` that derives the value
  from `agvtool` or Git (a more advanced pattern).

Both approaches are external to `Info.plist` itself. This is why
`Info.plist.Additions.md` documents the script but does not modify
`Info.plist` for this item.

---

## 6. Applying the changes — step-by-step

For a v1.0 submission that ships with camera and voice search enabled:

### Step 1 — Edit `Info.plist`

Open `/native/ios/Lexora/Info.plist` in a text editor (or Xcode → Open As →
Source Code).

**Find** (currently lines 109–116):

```xml
<!--
<key>NSCameraUsageDescription</key>
<string>Lexora uses the camera to scan and look up words.</string>
<key>NSMicrophoneUsageDescription</key>
<string>Lexora uses the microphone for voice search.</string>
<key>NSPhotoLibraryUsageDescription</key>
<string>Lexora can save word illustrations to your photo library.</string>
-->
```

**Replace with:**

```xml
<!-- Camera search — required by Phase 3 camera-search feature (Vision framework). -->
<key>NSCameraUsageDescription</key>
<string>Lexora uses the camera to scan and look up words on the page in front of you. The camera feed is processed on-device only — no images are saved, and no images leave your device.</string>

<!-- Voice search — required by Phase 3 voice-search feature (Speech framework). -->
<key>NSMicrophoneUsageDescription</key>
<string>Lexora uses the microphone for voice search. A single short utterance is transcribed on-device and used as your search query — no audio is recorded, stored, or transmitted.</string>

<!-- Speech recognition — required alongside the microphone for voice search. -->
<key>NSSpeechRecognitionUsageDescription</key>
<string>Lexora uses speech recognition to transcribe your voice-search queries on-device.</string>

<!-- Photo library — reserved for Phase 3 "save illustration to Photos" feature. Leave commented out unless that feature ships in v1.0. -->
<!--
<key>NSPhotoLibraryUsageDescription</key>
<string>Lexora can save word illustrations to your photo library.</string>
<key>NSPhotoLibraryAddUsageDescription</key>
<string>Lexora can save word illustrations to your photo library.</string>
-->
```

> **Note on `NSPhotoLibraryUsageDescription`:** the original commented block
> included this key. It is for a hypothetical "save word illustration to
> Photos" feature. If that feature is NOT in v1.0, keep the key commented
> out (Apple will reject the build if the key is declared but the feature
> is not present, or vice versa). The replacement block above keeps the
> photo-library keys commented out and adds the
> `NSPhotoLibraryAddUsageDescription` (the "add-only" variant — preferred
> over full `NSPhotoLibraryUsageDescription` because it triggers a less
> alarming permission prompt).

### Step 2 — Verify `ITSAppUsesNonExemptEncryption`

No edit needed — already set to `<false/>` at lines 100–102.

(Optional cosmetic fix: update the comment on line 100 from "App does not
use exempt encryption" to "App uses only exempt (system-provided)
encryption" — the original wording is ambiguous.)

### Step 3 — Create the build-number script

```bash
mkdir -p /native/ios/Lexora/Scripts
# Save the bump-build-number.sh script from §5 to this directory.
chmod +x /native/ios/Lexora/Scripts/bump-build-number.sh
```

### Step 4 — Wire the script into the Xcode target

Follow §5 "How to wire it into the Xcode target".

### Step 5 — Build and verify

Follow §7 below.

---

## 7. Verification checklist

After applying all four additions, verify each before submitting to App
Store Connect.

### 7.1 `NSCameraUsageDescription`

- [ ] Open the built `.app`'s `Info.plist` (per §2 step 2).
- [ ] Confirm `NSCameraUsageDescription` is a top-level key.
- [ ] Confirm the value is the long user-facing string from §2.
- [ ] Confirm there is no `<!--` comment wrapper around it.
- [ ] Launch the app on a device, tap the camera icon in the search bar,
      and confirm iOS shows the standard camera permission prompt with the
      new string.

### 7.2 `NSMicrophoneUsageDescription` (and `NSSpeechRecognitionUsageDescription`)

- [ ] Open the built `.app`'s `Info.plist`.
- [ ] Confirm `NSMicrophoneUsageDescription` is present with the long
      user-facing string from §3.
- [ ] Confirm `NSSpeechRecognitionUsageDescription` is present (only if
      voice search ships in v1.0).
- [ ] Launch the app on a device, tap the microphone icon in the search
      bar, and confirm iOS shows two consecutive permission prompts
      (microphone, then speech recognition) with the new strings.

### 7.3 `ITSAppUsesNonExemptEncryption`

- [ ] Open the built `.app`'s `Info.plist`.
- [ ] Confirm `ITSAppUsesNonExemptEncryption` is present and set to
      `<false/>`.
- [ ] Run `plutil -p Info.plist | grep -i encrypt` and confirm output.
- [ ] (Optional) When uploading the build to App Store Connect via Xcode
      Organizer, confirm the upload does NOT trigger the encryption
      questionnaire prompt.

### 7.4 `CFBundleVersion` auto-increment

- [ ] `/native/ios/Lexora/Scripts/bump-build-number.sh` exists and is
      executable.
- [ ] Xcode target → Build Phases → first phase is "Bump Build Number"
      Run-Script phase.
- [ ] Build the app once; the build log shows
      `Bumped CFBundleVersion: N → N+1`.
- [ ] Open `/native/ios/Lexora/Info.plist` and confirm `CFBundleVersion`
      is the new value.
- [ ] Open the built `.app`'s `Info.plist` and confirm the same value.
- [ ] (Optional) Upload two consecutive TestFlight builds and confirm
      App Store Connect accepts both with unique build numbers.

### 7.5 Final smoke test

- [ ] Archive a **Release** build of the app (`Product → Archive`).
- [ ] In Xcode Organizer, validate the archive (`Validate...` button) —
      this catches any missing-usage-description or
      missing-encryption-key errors before upload.
- [ ] If validation passes, upload to App Store Connect.
- [ ] In App Store Connect → Lexora → Activity → Build, confirm the build
      appears with the correct `CFBundleShortVersionString` and
      `CFBundleVersion`.
- [ ] Confirm App Store Connect does NOT show a "Missing Encryption
      Export Compliance" warning on the build.

---

## 8. References

- Apple Developer docs — [Information Property List Key Reference:
  `NSCameraUsageDescription`](https://developer.apple.com/documentation/bundleresources/information_property_list/nscamerausagedescription)
- Apple Developer docs — [Information Property List Key Reference:
  `NSMicrophoneUsageDescription`](https://developer.apple.com/documentation/bundleresources/information_property_list/nsmicrophoneusagedescription)
- Apple Developer docs — [Information Property List Key Reference:
  `NSSpeechRecognitionUsageDescription`](https://developer.apple.com/documentation/bundleresources/information_property_list/nsspeechrecognitionusagedescription)
- Apple Developer docs — [Information Property List Key Reference:
  `ITSAppUsesNonExemptEncryption`](https://developer.apple.com/documentation/bundleresources/information_property_list/itsappusesnonexemptencryption)
- Apple Developer docs — [`CFBundleVersion`](https://developer.apple.com/documentation/bundleresources/information_property_list/cfbundleversion)
- Apple Developer docs — [Exporting Your App for Distribution:
  Encryption documentation](https://developer.apple.com/documentation/security/complying_with_encryption_export_regulations)
- App Store Connect Help — [Add an app version](https://help.apple.com/app-store-connect/#/dev82a1c2f9b)

---

*This document is part of the Lexora iOS submission bundle. The canonical
`Info.plist` source lives at `/native/ios/Lexora/Info.plist`; this document
describes the post-submission-ready state of that file.*
