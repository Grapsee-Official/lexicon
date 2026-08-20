# PHASE-2-ANDROID-NAV — android-nav-subagent

## Task
Wire up navigation between all Lexora Android screens and update the main activity to compose everything together.

## Files Touched

### Created
- `native/android/app/src/main/java/com/lexora/ui/screens/RootScreen.kt`
  - Top-level orchestrator (direct translation of `src/app/page.tsx`).
  - `collectAsStateWithLifecycle()` for lifecycle-aware state collection.
  - `AnimatedContent` between tabs with `fadeIn(220ms) + slideInVertically(full/12)` ↔ `fadeOut(180ms) + slideOutVertically(-full/12)`.
  - `NonHomeTab` private composable renders LexoraHeader + sub-header (title + subtitle) + content Box(weight=1f, bottom padding=96dp).
  - BottomNav overlay rendered conditionally (`activeTab != TabKey.HOME`) to avoid duplicate nav bars (HomeScreen renders its own on the HOME tab).
  - WordDetailSheet + SearchModal called unconditionally as overlays (parallel agent's components observe ViewModel state internally).
- `native/android/app/src/main/java/com/lexora/LexoraApplication.kt`
  - Thin `Application` subclass — single startup log line.
  - Reserved as hook for future SDK init (Phase 3).
  - Documents that dark/light mode is driven by `LexoraViewModel`, not the system configuration.

### Updated
- `native/android/app/src/main/java/com/lexora/MainActivity.kt`
  - Replaced `HomeScreen(viewModel)` with `RootScreen(viewModel = viewModel)`.
  - Switched `collectAsState()` → `collectAsStateWithLifecycle()`.
  - Kept `enableEdgeToEdge()` + `attachPrefs(applicationContext)` before setContent.
  - Kept `LexoraTheme(darkTheme = state.isDark)` wrapper + Surface host.
- `native/android/app/src/main/AndroidManifest.xml`
  - Added `android:name=".LexoraApplication"` to `<application>`.
  - Added `android:configChanges="orientation|screenSize|screenLayout|keyboardHidden|uiMode|density|fontScale"` to MainActivity (prevents Activity recreation on config changes — Compose handles via recomposition).
  - Verified MainActivity is the launcher and theme is `@style/Theme.Lexora`.
- `native/android/gradle/libs.versions.toml`
  - Added `androidx-lifecycle-runtime-compose` entry (version 2.8.7, matches other lifecycle libs).
- `native/android/app/build.gradle.kts`
  - Added `implementation(libs.androidx.lifecycle.runtime.compose)` — provides `collectAsStateWithLifecycle`.

### Verified (no changes needed)
- `native/android/gradle/wrapper/gradle-wrapper.properties` — already uses Gradle 8.9 (satisfies "8.7+" requirement, compatible with AGP 8.6.0).

## Coordination With Parallel Agents

RootScreen imports/calls these components built by parallel agents:

| Component | Package | Call Site |
|-----------|---------|-----------|
| `DictionaryScreen` | `com.lexora.ui.screens` (same package as RootScreen — no import) | `DictionaryScreen(vm = viewModel)` |
| `CollectionsScreen` | `com.lexora.ui.screens` | `CollectionsScreen(vm = viewModel)` |
| `MoreScreen` | `com.lexora.ui.screens` | `MoreScreen(vm = viewModel)` |
| `WordDetailSheet` | `com.lexora.ui.components` | `WordDetailSheet(vm = viewModel)` |
| `SearchModal` | `com.lexora.ui.components` | `SearchModal(vm = viewModel)` |

All five are called with the named parameter `vm = viewModel` matching the agreed `@Composable fun XxxScreen(vm: LexoraViewModel)` / `@Composable fun XxxSheet(vm: LexoraViewModel)` signatures.

## Design Decisions

1. **Conditional BottomNav rendering** — `if (activeTab != TabKey.HOME)`. HomeScreen.kt already renders its own BottomNav when activeTab == HOME (cannot modify HomeScreen per task rule), so an unconditional render would produce duplicate nav bars.
2. **`pb-24` equivalent** — 96dp bottom padding on `NonHomeTab`'s content Box, matching HomeScreen's `PaddingValues(bottom = 96.dp)`.
3. **AnimatedContent keying** — `contentKey = { it }` so transitions are keyed on the tab enum (prevents unnecessary recomposition). Enter (220ms) slightly slower than exit (180ms) so the incoming tab reads first.
4. **Overlays called unconditionally** — RootScreen does NOT wrap WordDetailSheet / SearchModal in `AnimatedVisibility` to avoid fighting the components' own internal animations.
5. **SEARCH tab fallback** — `when` branch falls back to `HomeScreen` defensively. The BottomNav centre FAB calls `viewModel.openSearch()` (opens the SearchModal overlay) and never sets `activeTab = SEARCH`.
6. **`android:configChanges="uiMode"`** — prevents Activity recreation when the system dark-mode flips, so the in-app theme toggle (driven by `LexoraViewModel.isDark`) remains the single source of truth.

## Dependencies Audited

All task-required deps verified present in `gradle/libs.versions.toml` + `app/build.gradle.kts`:
- ✓ Compose BOM (2024.10.01)
- ✓ material3, ui, ui-graphics, ui-tooling-preview, ui-tooling (debug)
- ✓ activity-compose (1.9.3)
- ✓ lifecycle-runtime-ktx, lifecycle-viewmodel-compose, lifecycle-runtime-compose (all 2.8.7)
- ✓ navigation-compose (2.8.4)
- ✓ material-icons-extended, datastore-preferences, com.google.android.material:material (1.12.0)

## Status
- All 6 task items completed.
- No tests written (per task rule).
- No build executed (per Phase 2 convention — source only, user compiles locally).
- Did NOT modify any existing component files (only RootScreen created, MainActivity/Application/manifest updated).
