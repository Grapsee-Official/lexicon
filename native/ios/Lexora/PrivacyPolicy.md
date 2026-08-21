# Lexora — Privacy Policy

**Effective date:** 1 January 2025
**Last updated:** 1 January 2025
**App:** Lexora — Words. Wisdom. Worlds.
**Bundle identifier:** `com.lexora.app`
**Platform:** iOS 16.0 and later
**Developer:** The Lexora Team (`privacy@lexora.app`)

---

## 1. Overview

Lexora is an **offline-first multilingual dictionary**. The core product — looking
up words, browsing the lexicon, saving entries, and reading curated articles —
runs entirely on your device. We designed Lexora so that **you do not need to
trust us with your data**, because we never ask for it.

This policy explains, in plain language:

- what data Lexora **does not** collect,
- the two narrow categories of **anonymous, non-personal** technical data that
  Lexora may transmit to us (anonymous crash reports and anonymous sync
  metadata),
- how long we keep that technical data,
- how you can disable it, and
- your rights under the **GDPR** (European Economic Area, United Kingdom,
  Switzerland) and the **CCPA / CPRA** (California).

This policy applies to the Lexora iOS app only. It does not cover any
third-party website you may reach from a link inside the app (e.g. an article
citation in an entry's bibliography).

---

## 2. The short version

> **Lexora does not collect personal data.**
>
> - No email address, no phone number, no account.
> - No advertising SDKs. No analytics or tracking SDKs.
> - No cross-app or cross-website behavioural tracking.
> - No sale or "sharing" of personal data as defined by CCPA.
> - No data linked to your identity, your device, or your Apple ID.
> - Two narrow categories of **anonymous** technical data may be sent to us
>   (crash reports and sync metadata). Both can be disabled in Settings.
> - Crash logs are deleted from our servers after **30 days**.

If you read nothing else, read that.

---

## 3. Data Lexora does **not** collect

For the avoidance of doubt, Lexora does **not** collect, transmit, or store:

| Category | Status |
| --- | --- |
| Name, email, phone number | Never collected |
| Apple ID, account identifier | Never collected; the app has no account system |
| Device identifier (IDFV, IDFA) | Never read; IDFA is not requested (App Tracking Transparency never triggered) |
| Precise or coarse location | Never collected; no Core Location usage |
| Address book / contacts | Never accessed |
| Photos, camera roll | Never accessed (the optional camera-search feature processes frames on-device only — see §6) |
| Microphone audio recordings | Never stored; voice-search audio is processed ephemerally on-device — see §6 |
| Browsing history | Never collected |
| Health, fitness, sensor data | Never collected |
| Financial data | Never collected |
| Search queries you type | Stored only in your device's `UserDefaults` for the "Recent searches" UI; never transmitted off-device |
| Words you save | Stored only in your device's `UserDefaults`; never transmitted off-device |
| Words you look up | Never logged, never transmitted |

Lexora has **no server-side user profile**. There is no "you" in our database.

---

## 4. Data Lexora **may** collect (anonymous, non-personal, disable-able)

Lexora transmits two narrow categories of purely technical, anonymous data to
its developer. **Neither category contains personal data, personally
identifiable information (PII), or data linked to your identity.** Both are
optional and can be turned off in **Settings → Privacy**.

### 4.1 Anonymous crash reports

If Lexora crashes, the iOS system generates a crash report. With your
permission (the **Crash Reporting** toggle in Settings, on by default), Lexora
uploads a redacted crash report to our crash-reporting backend.

Each uploaded report contains **only**:

- The **stack trace** (function names and addresses from Lexora's own binary
  and the system frameworks it links).
- The **iOS version** (e.g. `17.4.1`).
- The **device model** (e.g. `iPhone16,1`).
- The **Lexora app version** and **build number** (e.g. `1.0.0 (42)`).
- The **timestamp** of the crash (UTC).
- The **crash type** (e.g. `EXC_BAD_ACCESS`, `SIGABRT`).
- The **thread state** (register values — these are memory addresses, not data).

Each uploaded report **does not** contain:

- Your IP address is **not retained**; it is read ephemerally by the transport
  layer to deliver the upload and discarded at the edge.
- Your Apple ID, name, email, or any account identifier.
- The word you were looking up, your saved words, your recent searches, or any
  other on-device content.
- The contents of the clipboard, the camera, the microphone, or the photo
  library.
- Identifier for Advertising (IDFA) — Lexora never requests it.
- Identifier for Vendor (IDFV) — Lexora never reads it.

Crash reports are **aggregated and triaged**, not profiled. We use them to fix
bugs.

### 4.2 Anonymous sync metadata

Lexora supports **optional delta-sync** of language-pack updates — i.e. when a
new version of a language pack is published (new words, corrections, fresh
examples), the app can download just the diff instead of re-downloading the
entire pack.

To make that work, the app sends a single small **anonymous metadata request**
to the Lexora content delivery network. That request contains **only**:

- The **currently-installed version** of each language pack (e.g.
  `en-US:2024.11.03`).
- The list of **language packs you have downloaded** (e.g. `["en-US", "es-ES",
  "fr-FR"]`).
- The **Lexora app version** and **build number**.

That request **does not** contain:

- Any identifier for you or your device. The request is unauthenticated and
  stateless on the server side; the CDN answers based solely on the version
  numbers in the request body.
- Your IP address is not retained beyond the duration of the connection.
- Your search history, saved words, or any other on-device content.

The response from the server is a signed, content-addressed manifest of
available updates; the app downloads only the packs you have installed.

---

## 5. Data retention

| Data category | Retention |
| --- | --- |
| Anonymous crash reports | **30 days** from upload, then permanently deleted from our servers |
| Anonymous sync metadata requests | Not retained at all; the request is processed in-memory to compute the response, then discarded. Server-side access logs (which contain the requester IP) are rotated after **7 days**. |
| On-device saved words, recent searches, preferences | Persist on your device until you delete the app or use **Settings → Reset Lexora**. Never transmitted off-device. |

We do not retain crash reports beyond 30 days for any reason. There is no
"backup" or "long-term archive" copy.

---

## 6. On-device processing features

Lexora offers two optional input features that use device sensors. **Both
process their input entirely on-device.** No audio, no image, and no video
leaves your device.

### 6.1 Voice search (microphone)

When you tap the microphone icon in the search bar, Lexora uses Apple's
on-device **Speech** framework to transcribe a single short utterance into
text. The transcription is used as the search query. The audio is **not
recorded, not stored, and not transmitted**. Speech recognition happens
entirely on-device (iOS routes the request to the system speech recognizer,
which on supported devices does not contact Apple's servers).

This feature requires the `NSMicrophoneUsageDescription` permission in
`Info.plist`. iOS will show the standard microphone permission prompt the first
time you use voice search. You can revoke the permission at any time in
**iOS Settings → Lexora → Microphone**.

### 6.2 Camera search (camera)

When you tap the camera icon in the search bar, Lexora opens the device camera
and uses Apple's on-device **Vision** framework (text recognition) to detect
words in the camera frame. Tapping a recognised word inserts it into the
search bar. The camera feed is **processed in-memory and discarded frame by
frame**; no image is saved to your photo library, no image is transmitted off
the device, and no recording is made.

This feature requires the `NSCameraUsageDescription` permission in
`Info.plist`. iOS will show the standard camera permission prompt the first
time you use camera search. You can revoke the permission at any time in
**iOS Settings → Lexora → Camera**.

Both features are **optional**. If you decline the permission prompt — or
revoke it later — the rest of Lexora works normally; only voice search and
camera search are unavailable.

---

## 7. No advertising. No analytics. No tracking.

Lexora contains **no advertising SDKs** and **no analytics or tracking SDKs**.
Specifically, the app does **not** embed, link, or otherwise include:

- Google Analytics, Firebase Analytics, Mixpanel, Amplitude, Heap, PostHog,
  or any other product-analytics SDK.
- Facebook SDK, Google Ads, AdMob, Unity Ads, or any other advertising SDK.
- AppsFlyer, Adjust, Branch, Kochava, or any other attribution SDK.
- Sentry, Crashlytics, or Bugsnag as a third-party crash SDK — crash reports
  (see §4.1) are delivered to a self-hosted endpoint via our own transport.

Lexora does **not** participate in Apple's SKAdNetwork. The app does **not**
request App Tracking Transparency authorisation, because it has nothing to
track.

Lexora does **not** use the Identifier for Advertisers (IDFA) and does **not**
read the Identifier for Vendor (IDFV).

Lexora does **not** "sell" personal data as defined by CCPA, nor does it
"share" personal data for cross-context behavioural advertising as defined by
CPRA. (There is no personal data to sell or share.)

---

## 8. How to disable crash reporting and sync

Both optional data transmissions can be turned off in the app.

### Disable crash reporting

1. Open Lexora.
2. Tap the **More** tab in the bottom navigation bar.
3. Tap **Settings**.
4. Toggle **Crash Reporting** to **off**.

When crash reporting is off, Lexora will not upload any crash reports, even if
a crash occurs. The toggle takes effect immediately; no app restart is
required.

### Disable automatic language-pack sync

1. Open Lexora → **More** → **Settings**.
2. Toggle **Auto-update language packs** to **off**.

When auto-update is off, Lexora will not send the anonymous sync-metadata
request described in §4.2. You can still manually check for updates from the
same screen — that manual check sends the same anonymous request once per
explicit tap.

### Disable everything at once

Toggling **Settings → Privacy → Send diagnostic & usage data** to **off**
disables both crash reporting and sync metadata in one switch. This is
equivalent to setting both individual toggles to off.

### Nuclear option: airplane mode

Because Lexora is offline-first, simply enabling iOS Airplane Mode (or
revoking Lexora's "Local Network" / cellular / Wi-Fi permission via iOS
Settings) prevents any and all network egress from the app. The dictionary
keeps working.

---

## 9. Legal bases (GDPR)

For the two narrow categories of anonymous technical data described in §4,
the legal basis under Article 6 of the GDPR is:

- **Article 6(1)(f)** — our **legitimate interest** in keeping the app crash-free
  (crash reports) and in shipping language-pack updates to users who have
  explicitly opted into auto-update (sync metadata).

We have balanced this legitimate interest against your interests and rights
and concluded that the processing is minimal, anonymous, and disable-able,
and therefore does not override your rights and freedoms. You have the right
to object to this processing at any time by toggling the switches described
in §8 — there is no need to contact us.

Because Lexora does **not** collect personal data, the GDPR rights of access,
rectification, erasure, portability, and restriction do not strictly apply
(there is no personal data to access, rectify, erase, port, or restrict).
However, if you believe we hold any data about you, you may contact us at
`privacy@lexora.app` and we will respond within 30 days.

If you wish to complain to a data protection authority, you may do so in the
EU/EEA member state of your habitual residence, place of work, or the member
state where the alleged infringement occurred. The lead supervisory authority
for Lexora is the authority of the developer's place of establishment.

---

## 10. Your rights under CCPA / CPRA (California)

Under the California Consumer Privacy Act (as amended by the California Privacy
Rights Act), California residents have specific rights regarding their personal
information. Because Lexora **does not collect personal information** as
defined by CCPA, most of these rights do not apply. For completeness:

| CCPA right | Applicability to Lexora |
| --- | --- |
| Right to know what personal information is collected | Lexora collects no personal information. Anonymous crash reports and sync metadata (§4) are not personal information as defined by CCPA §1798.140(v). |
| Right to delete personal information | Lexora holds no personal information about you. On-device data (saved words, recent searches) is yours to delete via **Settings → Reset Lexora** or by deleting the app. |
| Right to correct personal information | Lexora holds no personal information about you. |
| Right to opt out of sale or sharing | Lexora does not sell or share personal information. No opt-out is necessary. |
| Right to limit use of sensitive personal information | Lexora does not collect sensitive personal information. |
| Right to non-discrimination | Lexora does not discriminate against users who exercise privacy rights (e.g. by disabling crash reporting). All features work identically with crash reporting on or off. |

If you have any questions about your CCPA rights and Lexora, contact us at
`privacy@lexora.app`. We will not ask you to create an account to exercise
these rights, and we will respond within 45 days.

---

## 11. Children's privacy

Lexora is suitable for all ages and is rated **4+** on the App Store. The app
does not knowingly collect any personal information from anyone, including
children under 13 (COPPA) or under 16 (CCPA). No feature of the app is gated
behind an account, an email, or any other identifier.

If you believe a child has provided us with personal information (for example,
by emailing us), please contact `privacy@lexora.app` and we will delete it.

---

## 12. International transfers

Lexora's crash-reporting and sync-metadata endpoints are hosted in the
European Union (Frankfurt) with backups in the United States (us-east-1).
Because the data transmitted is **anonymous and non-personal** (§4), the
GDPR's cross-border transfer rules (Chapter V) do not strictly apply. If you
prefer that your anonymous diagnostic data not leave the EU, disable crash
reporting and auto-update per §8 — the dictionary will continue to function
normally.

---

## 13. Security

- All network traffic from Lexora to its endpoints uses **TLS 1.3** with
  certificate pinning on the sync endpoint. Plain HTTP is never used.
- Crash reports are stored at rest on the crash-reporting backend **encrypted
  with AES-256** and accessible only to two named engineers on the Lexora team,
  both of whom use hardware security keys for two-factor authentication.
- The crash-reporting backend is audited quarterly for access logs; no
  unauthorised access has ever been detected.

Because the data is anonymous, a breach of the crash-reporting backend would
not expose personal information about any user.

---

## 14. Changes to this policy

We may update this policy from time to time. When we do, we will:

- bump the **Last updated** date at the top of this document,
- ship the updated policy inside the next Lexora app update (so the version of
  the policy you see in-app always matches the version of the app you are
  running), and
- if the change is material (e.g. a new category of data is added), show an
  in-app banner on first launch of the updated version.

The latest version of this policy is always available inside the app at
**More → Settings → Privacy Policy**, and on the web at
`https://lexora.app/privacy`.

---

## 15. Open-source acknowledgements

Lexora ships with the following third-party open-source components, none of
which collect data:

- **SQLite** (public domain) — on-device dictionary store.
- **zstd** (BSD-3-Clause) — delta-compressed language-pack downloads.
- **Apple SwiftUI, Foundation, Speech, Vision** — first-party Apple frameworks.

A full list, including exact versions and licences, is available in the app at
**More → Settings → Acknowledgements**.

---

## 16. Contact

If you have any questions, concerns, or requests regarding this privacy policy
or Lexora's data practices, please contact:

- **Email:** `privacy@lexora.app`
- **Postal address:**
  The Lexora Team — Privacy
  [Developer mailing address — fill in before submission]
- **In-app:** More → Settings → Privacy Policy → Send feedback

We aim to respond to all privacy-related enquiries within **30 days**. For
GDPR complaints and CCPA requests, please include the phrase "Privacy Request"
in the subject line.

---

*This document is part of the Lexora iOS submission bundle and is included
verbatim in the app at `Lexora/PrivacyPolicy.md`. The App Store Connect
listing references the canonical URL `https://lexora.app/privacy`, which
serves an HTML rendering of this same document.*
