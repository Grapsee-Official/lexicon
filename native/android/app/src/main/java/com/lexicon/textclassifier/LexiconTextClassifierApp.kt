package com.lexicon.textclassifier

import android.app.Application
import android.content.ComponentCallbacks2
import android.content.res.Configuration
import com.lexicon.engine.LexiconEngine
import com.lexicon.engine.MemoryPressureLevel

/**
 * LexiconTextClassifierApp — registers memory pressure callbacks.
 *
 * Per the spec risk matrix:
 *   "Do use WebP with strict dimension capping. Do lazy-load illustrations.
 *    Do unload DAWG when RAM pressure is detected via ComponentCallbacks2."
 *
 * This Application class registers the C++ engine to receive memory pressure
 * events from the Android system. When the system reports MODERATE or CRITICAL
 * memory pressure, the engine unloads the DAWG to free memory.
 */
class LexiconTextClassifierApp : Application() {

    var engine: LexiconEngine? = null
        private set

    override fun onCreate() {
        super.onCreate()

        // Register global memory pressure callback
        registerComponentCallbacks(object : ComponentCallbacks2 {
            override fun onConfigurationChanged(config: Configuration) {}

            override fun onLowMemory() {
                engine?.enableDawg(false)
            }

            override fun onTrimMemory(level: Int) {
                val mpLevel = when {
                    level >= ComponentCallbacks2.TRIM_MEMORY_COMPLETE ->
                        MemoryPressureLevel.CRITICAL
                    level >= ComponentCallbacks2.TRIM_MEMORY_MODERATE ->
                        MemoryPressureLevel.MODERATE
                    else -> MemoryPressureLevel.LOW
                }
                when (mpLevel) {
                    MemoryPressureLevel.LOW -> { /* keep DAWG loaded */ }
                    MemoryPressureLevel.MODERATE -> engine?.enableDawg(false)
                    MemoryPressureLevel.CRITICAL -> {
                        engine?.enableDawg(false)
                    }
                }
            }
        })
    }

    fun initEngine(dbDir: String): LexiconEngine {
        val eng = LexiconEngine()
        eng.loadDatabase(dbDir)
        engine = eng
        return eng
    }
}
