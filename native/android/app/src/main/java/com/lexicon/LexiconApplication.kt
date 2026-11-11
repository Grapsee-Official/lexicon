package com.lexicon

import android.app.Application
import android.util.Log
import com.lexicon.engine.CrashReporter
import com.lexicon.engine.OOMMonitor

class LexiconApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        runCatching {
            CrashReporter.get(this).apply {
                install()
                uploadPendingCrashLogs()
            }
            OOMMonitor.get(this).register()
        }

        @Suppress("DEPRECATION")
        val versionName = try {
            packageManager.getPackageInfo(packageName, 0).versionName
        } catch (_: Exception) {
            "1.0.0"
        }
        Log.d(TAG, "LexiconApplication initialised — v$versionName")
    }

    companion object {
        private const val TAG = "Lexicon"
    }
}
