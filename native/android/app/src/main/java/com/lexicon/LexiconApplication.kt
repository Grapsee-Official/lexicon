package com.lexicon

import android.app.Application
import android.util.Log

/**
 * LexiconApplication — app-level entry point.
 *
 * Registered in `AndroidManifest.xml` via `android:name=".LexiconApplication"`.
 *
 * Responsibilities:
 *  - Single startup log line for debugging / logcat grep.
 *  - Reserved for future app-scope initialisation (analytics, crash
 *    reporters, strict-mode configuration in debug builds, …).
 *
 * The UI / state / persistence layers are owned by Compose and the
 * [com.lexicon.data.LexiconViewModel]; this class is intentionally thin so
 * nothing here can block the first frame.
 *
 * Dark / light mode is driven entirely by [com.lexicon.data.LexiconViewModel]
 * (the in-app theme toggle), NOT by the system configuration. The
 * Activity is registered with `android:configChanges="uiMode"` in the
 * manifest so a system dark-mode flip does not recreate the Activity and
 * override the user's in-app preference.
 */
class LexiconApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        @Suppress("DEPRECATION")
        val versionName = try {
            packageManager.getPackageInfo(packageName, 0).versionName
        } catch (e: Exception) {
            "unknown"
        }
        Log.d(TAG, "LexiconApplication initialised — v$versionName")
    }

    companion object {
        private const val TAG = "Lexicon"
    }
}
