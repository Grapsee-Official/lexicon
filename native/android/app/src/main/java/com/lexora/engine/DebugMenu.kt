// DebugMenu.kt — Android hidden Debug Menu for Lexora.
//
// Per spec (P4-09, Week 23-24):
//   * Hidden debug menu (activate by tapping version number 7 times —
//     like Android developer options).
//   * Toggle feature flags
//   * View engine memory usage
//   * Run benchmarks (1M lookups, show timing)
//   * Force sync / force crash / force OOM
//   * View crash logs
//   * Clear cache / reset database
//   * Only available in debug builds (BuildConfig.DEBUG)
//
// Wiring (in MoreScreen.kt):
// ```kotlin
// val tapCounter = rememberDebugTapCounter()
// val context = LocalContext.current
// // ... existing About row:
// SettingsRow(
//     icon = Icons.Outlined.Info,
//     iconLabel = "About",
//     label = "About Lexora",
//     value = "v${BetaConfig.VERSION_NAME}",
//     onClick = { tapCounter.onTap(context) }, // <- 7 taps reveals the menu
// )
// if (tapCounter.revealed.value) {
//     DebugMenuSheet(
//         deps = DebugMenuDeps(
//             engine = engineOrNull,
//             onForceSync = { /* invoke DeltaSyncClient */ },
//             onClearCache = { /* clear filesDir/lexora/cache */ },
//             onResetDatabase = { /* delete filesDir/lexora/*.bin */ },
//             crashLogProvider = { File(cacheDir, "crash.log").readText() },
//         ),
//         onDismiss = { tapCounter.hide() },
//     )
// }
// ```

package com.lexora.engine

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeveloperMode
import androidx.compose.material.icons.filled.Flaky
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lexora.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// ===========================================================================
// 7-tap activation state holder
// ===========================================================================

/**
 * State holder for the 7-tap "reveal debug menu" gesture. Mirrors Android's
 * "tap Build Number 7 times" developer-options activation.
 *
 * Behaviour:
 *   - Each [onTap] bumps an internal counter.
 *   - At counter >= [REVEAL_THRESHOLD] (7), [revealed] flips true.
 *   - A 3-second idle timer resets the counter to 0 (so accidental taps
 *     from a curious user don't accumulate over minutes).
 *   - In release builds ([BuildConfig.DEBUG] == false), [onTap] is a no-op
 *     and [revealed] can never become true — the menu is unreachable even
 *     if a host screen wires up the tap handler.
 *
 * Usage:
 * ```kotlin
 * val tap = rememberDebugTapCounter()
 * Text("v1.0.0", modifier = Modifier.clickable { tap.onTap() })
 * if (tap.revealed.value) DebugMenuSheet(...)
 * ```
 */
class DebugTapCounter {
    /** Current tap count (0..REVEAL_THRESHOLD). Resets to 0 after [IDLE_RESET_MS] of inactivity. */
    val taps: MutableState<Int> = mutableStateOf(0)

    /** True once the threshold has been reached. Stays true until [hide] is called. */
    val revealed: MutableState<Boolean> = mutableStateOf(false)

    /**
     * Call from any "About" / version-number click. No-op in release builds.
     * Returns the number of taps remaining (for snackbars / toasts), or 0
     * once revealed.
     */
    fun onTap(context: Context): Int {
        if (!BuildConfig.DEBUG) return 0
        if (revealed.value) return 0
        val now = taps.value + 1
        taps.value = now
        if (now >= REVEAL_THRESHOLD) {
            revealed.value = true
            Toast.makeText(
                context,
                "Debug menu enabled",
                Toast.LENGTH_SHORT,
            ).show()
            return 0
        }
        val remaining = REVEAL_THRESHOLD - now
        if (remaining in 1..4) {
            Toast.makeText(
                context,
                "$remaining more tap${if (remaining == 1) "" else "s"} to enable debug menu",
                Toast.LENGTH_SHORT,
            ).show()
        }
        return remaining
    }

    /** Programmatically hide the menu (called by the sheet's dismiss button). */
    fun hide() {
        revealed.value = false
        taps.value = 0
    }

    companion object {
        /** Number of consecutive taps required to reveal the menu. */
        const val REVEAL_THRESHOLD: Int = 7

        /** Idle window (ms) after which the tap counter resets to 0. */
        const val IDLE_RESET_MS: Long = 3_000L
    }
}

/**
 * Create a [DebugTapCounter] remembered across recompositions. Also
 * installs a [LaunchedEffect] that resets the counter after
 * [DebugTapCounter.IDLE_RESET_MS] of inactivity.
 */
@Composable
fun rememberDebugTapCounter(): DebugTapCounter {
    val counter = remember { DebugTapCounter() }
    // Reset the tap counter after a short idle window. We watch `taps` via
    // snapshotFlow so the timer only starts when a tap actually lands.
    LaunchedEffect(Unit) {
        snapshotFlow { counter.taps.value }
            .distinctUntilChanged()
            .collect { t ->
                if (t in 1 until DebugTapCounter.REVEAL_THRESHOLD) {
                    delay(DebugTapCounter.IDLE_RESET_MS)
                    // Only reset if no further tap bumped the counter.
                    if (counter.taps.value == t && !counter.revealed.value) {
                        counter.taps.value = 0
                    }
                }
            }
    }
    return counter
}

// ===========================================================================
// Dependency injection
// ===========================================================================

/**
 * Dependencies the [DebugMenuSheet] needs from the host app. Keeping these
 * as lambdas / interfaces (rather than concrete types) keeps the debug
 * menu decoupled from sync client, crash reporter, and storage internals.
 *
 * Every callback is invoked on the main thread; long work should hop to
 * a background dispatcher internally.
 *
 * @param engine nullable — the engine may not be ready yet at debug-menu
 *   open time. When null, the memory + benchmark sections render a
 *   "not ready" placeholder.
 * @param onForceSync kick off a delta-sync cycle. Implementations should
 *   not block — return immediately and report progress out-of-band.
 * @param onClearCache clear the engine's runtime caches (in-memory hash
 *   cache, image cache, etc.) but leave the on-disk DB intact.
 * @param onResetDatabase delete the on-disk dictionary files. After this
 *   the engine must be re-loaded before lookups work again.
 * @param crashLogProvider returns the current crash-log text (empty string
 *   if no crashes have been recorded).
 */
data class DebugMenuDeps(
    val engine: LexoraEngine?,
    val onForceSync: () -> Unit = {},
    val onClearCache: () -> Unit = {},
    val onResetDatabase: () -> Unit = {},
    val crashLogProvider: () -> String = { "" },
)

// ===========================================================================
// Result data classes
// ===========================================================================

/** Result of a benchmark run. */
data class BenchmarkResult(
    val iterations: Int,
    val elapsedMillis: Long,
    val perLookupNanos: Double,
    val hits: Int,
) {
    override fun toString(): String =
        "$iterations lookups in ${elapsedMillis}ms " +
            "(${"%.0f".format(perLookupNanos)} ns/lookup, $hits hits)"
}

// ===========================================================================
// Sheet
// ===========================================================================

/**
 * The full Debug Menu as a Material3 [ModalBottomSheet]. Only render this
 * when [DebugTapCounter.revealed] is true AND [BetaConfig.isDebugMenuVisible]
 * returns true for the current channel/build combination.
 *
 * Sections (top to bottom):
 *   1. Header — title, channel badge, close button.
 *   2. Build info — channel / build / version / feature-flag version.
 *   3. Feature flags — one [Switch] row per [FeatureFlag].
 *   4. Engine memory — hash-table / DAWG / total bytes.
 *   5. Benchmark — "Run 1M lookups" button + last result.
 *   6. Sync — "Force sync now" button.
 *   7. Crash testing — "Force crash" + "Force OOM" buttons (confirm dialog).
 *   8. Crash logs — expandable text view of the crash log.
 *   9. Data — "Clear cache" + "Reset database" buttons (confirm dialog).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebugMenuSheet(
    deps: DebugMenuDeps,
    onDismiss: () -> Unit,
) {
    // Final safety net: never render the sheet in a release build, even if
    // the host wired up the tap counter incorrectly.
    if (!BuildConfig.DEBUG) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Reactive state for the various sections.
    val flagSnapshot: State<Map<String, Boolean>> =
        FeatureFlags.state.collectAsState()
    var memoryInfo by remember { mutableStateOf<MemoryInfo?>(null) }
    var benchmarkResult by remember { mutableStateOf<BenchmarkResult?>(null) }
    var benchmarkRunning by remember { mutableStateOf(false) }
    var crashLogExpanded by remember { mutableStateOf(false) }
    var crashLogText by remember { mutableStateOf("") }
    var confirmDialog by remember { mutableStateOf<ConfirmDialog?>(null) }
    var toastMessage by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // ── Header ────────────────────────────────────────────────────
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(
                        Icons.Filled.DeveloperMode,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Lexora Debug Menu",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            "Channel: ${BetaConfig.activeChannel().name}  ·  Build #${BetaConfig.BUILD_NUMBER}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "Close debug menu")
                    }
                }
            }

            // ── Build info ───────────────────────────────────────────────
            item {
                DebugSection(title = "Build", icon = Icons.Filled.BugReport) {
                    InfoRow("Channel", BetaConfig.activeChannel().name)
                    InfoRow("Version name", BetaConfig.VERSION_NAME)
                    InfoRow("Build number", BetaConfig.BUILD_NUMBER.toString())
                    InfoRow("Feature-flag schema", BetaConfig.FEATURE_FLAG_VERSION.toString())
                    InfoRow("Remote config v", FeatureFlags.remoteConfigVersion.toString())
                    InfoRow("Debug overrides", FeatureFlags.debugOverrideCount.toString())
                }
            }

            // ── Feature flags ────────────────────────────────────────────
            item {
                DebugSection(title = "Feature Flags", icon = Icons.Filled.Flaky) {
                    FeatureFlag.values().forEach { flag ->
                        FlagToggleRow(
                            flag = flag,
                            value = flagSnapshot.value[flag.key] ?: flag.default,
                            onToggle = { newVal ->
                                FeatureFlags.setDebugOverride(
                                    flag,
                                    if (newVal == flag.default) null else newVal,
                                )
                            },
                        )
                    }
                    if (FeatureFlags.debugOverrideCount > 0) {
                        OutlinedButton(
                            onClick = { FeatureFlags.clearDebugOverrides() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                        ) {
                            Text("Clear ${FeatureFlags.debugOverrideCount} debug override(s)")
                        }
                    }
                }
            }

            // ── Engine memory ────────────────────────────────────────────
            item {
                DebugSection(title = "Engine Memory", icon = Icons.Filled.Memory) {
                    val engine = deps.engine
                    if (engine == null) {
                        Text(
                            "Engine not ready.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        // Sample once on first composition. Subsequent
                        // refreshes go through the explicit Refresh button.
                        LaunchedEffect(engine) {
                            if (memoryInfo == null) memoryInfo = engine.getMemoryUsage()
                        }
                        val info = memoryInfo
                        if (info != null) {
                            InfoRow("Hash table", formatBytes(info.hashTableBytes))
                            InfoRow("DAWG", formatBytes(info.dawgBytes))
                            InfoRow("Total engine", formatBytes(info.totalBytes))
                            InfoRow("DAWG enabled", engine.isDawgEnabled().toString())
                        }
                        OutlinedButton(
                            onClick = { memoryInfo = engine.getMemoryUsage() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                        ) {
                            Icon(Icons.Filled.Refresh, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Refresh")
                        }
                    }
                }
            }

            // ── Benchmark ────────────────────────────────────────────────
            item {
                DebugSection(title = "Benchmark", icon = Icons.Filled.Speed) {
                    val engine = deps.engine
                    if (engine == null) {
                        Text(
                            "Engine not ready — cannot benchmark.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        Button(
                            onClick = {
                                if (benchmarkRunning) return@Button
                                benchmarkRunning = true
                                scope.launch {
                                    val result = withContext(Dispatchers.Default) {
                                        runBenchmark(engine, iterations = 1_000_000)
                                    }
                                    benchmarkResult = result
                                    benchmarkRunning = false
                                }
                            },
                            enabled = !benchmarkRunning,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Icon(Icons.Filled.PlayArrow, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(if (benchmarkRunning) "Running 1M lookups…" else "Run 1M lookups")
                        }
                        benchmarkResult?.let { r ->
                            Spacer(Modifier.height(8.dp))
                            Text(
                                r.toString(),
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

            // ── Sync ─────────────────────────────────────────────────────
            item {
                DebugSection(title = "Cloud Sync", icon = Icons.Filled.Sync) {
                    Button(
                        onClick = {
                            deps.onForceSync()
                            toastMessage = "Force sync triggered"
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(Icons.Filled.Sync, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Force sync now")
                    }
                }
            }

            // ── Crash testing ────────────────────────────────────────────
            item {
                DebugSection(title = "Crash Testing", icon = Icons.Filled.Warning) {
                    Text(
                        "These actions crash the app on purpose. The crash " +
                            "reporter will capture the stack trace.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Button(
                            onClick = {
                                confirmDialog = ConfirmDialog.ForceCrash
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer,
                            ),
                        ) {
                            Text("Force crash")
                        }
                        Button(
                            onClick = {
                                confirmDialog = ConfirmDialog.ForceOom
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer,
                            ),
                        ) {
                            Text("Force OOM")
                        }
                    }
                }
            }

            // ── Crash logs ───────────────────────────────────────────────
            item {
                DebugSection(title = "Crash Logs", icon = Icons.Filled.BugReport) {
                    OutlinedButton(
                        onClick = {
                            crashLogText = try {
                                deps.crashLogProvider()
                            } catch (e: Exception) {
                                "(failed to read crash log: ${e.message})"
                            }
                            crashLogExpanded = !crashLogExpanded
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(if (crashLogExpanded) "Hide crash logs" else "View crash logs")
                    }
                    AnimatedVisibility(
                        visible = crashLogExpanded,
                        enter = expandVertically(),
                        exit = shrinkVertically(),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                                .height(200.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .border(
                                    1.dp,
                                    MaterialTheme.colorScheme.outline,
                                    RoundedCornerShape(8.dp),
                                ),
                        ) {
                            val scroll = rememberScrollState()
                            Text(
                                text = crashLogText.ifBlank { "(no crashes recorded)" },
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier
                                    .verticalScroll(scroll)
                                    .padding(12.dp),
                            )
                        }
                    }
                }
            }

            // ── Data ─────────────────────────────────────────────────────
            item {
                DebugSection(title = "Data", icon = Icons.Filled.Delete) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        OutlinedButton(
                            onClick = { confirmDialog = ConfirmDialog.ClearCache },
                            modifier = Modifier.weight(1f),
                        ) {
                            Text("Clear cache")
                        }
                        Button(
                            onClick = { confirmDialog = ConfirmDialog.ResetDb },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer,
                            ),
                        ) {
                            Text("Reset DB")
                        }
                    }
                }
            }

            // Trailing spacer so the last section isn't flush against the
            // gesture-navigation bar.
            item { Spacer(Modifier.height(24.dp)) }
        }
    }

    // ── Confirm dialog ──────────────────────────────────────────────────
    confirmDialog?.let { dialog ->
        AlertDialog(
            onDismissRequest = { confirmDialog = null },
            title = { Text(dialog.title) },
            text = { Text(dialog.message) },
            confirmButton = {
                Button(
                    onClick = {
                        when (dialog) {
                            ConfirmDialog.ForceCrash -> {
                                confirmDialog = null
                                // Defer one frame so the dialog can dismiss
                                // before the crash lands (cleaner stack trace).
                                scope.launch {
                                    delay(100)
                                    throw RuntimeException(
                                        "Debug menu: forced crash for crash-reporter testing"
                                    )
                                }
                            }
                            ConfirmDialog.ForceOom -> {
                                confirmDialog = null
                                scope.launch(Dispatchers.Default) {
                                    forceOom()
                                }
                            }
                            ConfirmDialog.ClearCache -> {
                                deps.onClearCache()
                                toastMessage = "Cache cleared"
                                confirmDialog = null
                            }
                            ConfirmDialog.ResetDb -> {
                                deps.onResetDatabase()
                                toastMessage = "Database reset — reload engine"
                                confirmDialog = null
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError,
                    ),
                ) {
                    Text(dialog.confirmLabel)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { confirmDialog = null }) {
                    Text("Cancel")
                }
            },
        )
    }

    // ── Toast sink ──────────────────────────────────────────────────────
    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            toastMessage = null
        }
    }
}

/** Dialog specs for destructive actions. */
private enum class ConfirmDialog(
    val title: String,
    val message: String,
    val confirmLabel: String,
) {
    ForceCrash(
        title = "Force crash?",
        message = "This will throw a RuntimeException. The crash reporter " +
            "should capture the stack trace. The app will close.",
        confirmLabel = "Crash",
    ),
    ForceOom(
        title = "Force OOM?",
        message = "This will allocate ~100MB chunks until OutOfMemoryError. " +
            "The crash reporter should capture the OOM trace. The app will close.",
        confirmLabel = "OOM",
    ),
    ClearCache(
        title = "Clear cache?",
        message = "Clears the in-memory hash cache and image cache. The " +
            "on-disk dictionary is preserved. Lookups will be slower for " +
            "a few seconds afterwards.",
        confirmLabel = "Clear",
    ),
    ResetDb(
        title = "Reset database?",
        message = "Deletes the on-disk hash_table.bin and dawg.bin. The " +
            "engine must be re-loaded (or the app restarted) before " +
            "lookups work again.",
        confirmLabel = "Reset",
    ),
}

// ===========================================================================
// Section + row composables
// ===========================================================================

@Composable
private fun DebugSection(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant,
                RoundedCornerShape(12.dp),
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp),
            )
            Text(
                title.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp,
            )
        }
        content()
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun FlagToggleRow(
    flag: FeatureFlag,
    value: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle(!value) }
            .padding(vertical = 4.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    flag.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    flag.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    flag.key,
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.outline,
                )
            }
            Switch(
                checked = value,
                onCheckedChange = onToggle,
            )
        }
    }
}

// ===========================================================================
// Benchmark + OOM helpers
// ===========================================================================

/**
 * Run [iterations] `lookup("test")` calls against [engine] and return the
 * timing. Must be called on a background dispatcher — this is a blocking
 * CPU-bound loop.
 *
 * Uses a fixed lookup key ("lexora") so the comparison across runs is
 * apples-to-apples. Hits vs misses are reported separately in [BenchmarkResult.hits].
 */
private fun runBenchmark(engine: LexoraEngine, iterations: Int): BenchmarkResult {
    // Warm-up: ensure the first lookup doesn't pay for cold cache + JNI
    // resolution, which would skew the per-lookup average.
    repeat(1_000) { engine.lookup("lexora") }

    val tStart = System.nanoTime()
    var hits = 0
    val key = "lexora"
    repeat(iterations) {
        if (engine.lookup(key) != null) hits++
    }
    val elapsedNanos = System.nanoTime() - tStart
    return BenchmarkResult(
        iterations = iterations,
        elapsedMillis = elapsedNanos / 1_000_000L,
        perLookupNanos = elapsedNanos.toDouble() / iterations,
        hits = hits,
    )
}

/**
 * Allocate ~100MB chunks in a list until the JVM throws OutOfMemoryError.
 * Catches the OOM to log it (so the crash reporter sees it) then rethrows
 * so the process actually dies — otherwise we'd be left in a half-broken
 * state with no free memory.
 */
private fun forceOom(): Nothing {
    val sink = ArrayList<ByteArray>()
    try {
        while (true) {
            // 100MB per chunk — large enough to OOM in a few iterations.
            sink.add(ByteArray(100 * 1024 * 1024))
        }
    } catch (oom: OutOfMemoryError) {
        // Log so the crash reporter captures a stack trace before rethrowing.
        android.util.Log.e("LexoraDebug", "Forced OOM after ${sink.size} chunks", oom)
        throw oom
    }
}

// ===========================================================================
// Formatting helpers
// ===========================================================================

private fun formatBytes(bytes: Long): String {
    if (bytes < 1024) return "$bytes B"
    val kb = bytes / 1024.0
    if (kb < 1024) return "%.1f KB".format(kb)
    val mb = kb / 1024.0
    if (mb < 1024) return "%.1f MB".format(mb)
    return "%.2f GB".format(mb / 1024.0)
}
