package com.lexicon.textclassifier

import android.app.PendingIntent
import android.app.RemoteAction
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.os.IBinder
import android.view.textclassifier.TextClassification
import android.view.textclassifier.TextClassificationManager
import android.view.textclassifier.TextClassifier
import androidx.annotation.RequiresApi
import com.lexicon.engine.LexiconEngine

/**
 * LexiconTextClassifierService — Public Android SDK text-selection classifier bridge.
 *
 * Uses `android.view.textclassifier.TextClassifier` and `TextClassificationManager`
 * (part of the public Android SDK since API 26/28) rather than the `@SystemApi`
 * `android.service.textclassifier.TextClassifierService`, so the APK compiles cleanly
 * against standard `android.jar` (compileSdk 34) while providing fast `<10ms` C++
 * word lookups and `RemoteAction` creation for selected words.
 */
class LexiconTextClassifierService : Service() {

    private var lexiconEngine: LexiconEngine? = null
    private var defaultClassifier: TextClassifier? = null

    override fun onCreate() {
        super.onCreate()
        val eng = LexiconEngine()
        val dbDir = getDir("lexicon_db", Context.MODE_PRIVATE).absolutePath
        if (!eng.loadDatabase(dbDir)) {
            eng.loadDatabase(":embedded:")
        }
        lexiconEngine = eng

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val tcm = getSystemService(TextClassificationManager::class.java)
            defaultClassifier = tcm?.textClassifier
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        lexiconEngine?.close()
        lexiconEngine = null
        super.onDestroy()
    }

    @RequiresApi(Build.VERSION_CODES.P)
    fun classifySelection(text: CharSequence, startIndex: Int, endIndex: Int): TextClassification {
        val request = TextClassification.Request.Builder(text, startIndex, endIndex).build()
        val base = defaultClassifier?.classifyText(request)
            ?: TextClassification.Builder().setText(text.subSequence(startIndex, endIndex).toString()).build()

        val selectedText = text.subSequence(startIndex, endIndex).toString().trim()
        if (selectedText.isEmpty() || selectedText.contains(" ")) {
            return base
        }

        val resolvedWord = lexiconEngine?.lookup(selectedText)?.word
            ?: com.lexicon.data.LexiconData.dictionary.firstOrNull {
                it.word.equals(selectedText, ignoreCase = true)
            }?.word
            ?: return base

        val builder = TextClassification.Builder()
            .setText(selectedText)
            .addAction(buildLexiconAction(selectedText, resolvedWord))

        for (action in base.actions) {
            builder.addAction(action)
        }
        return builder.build()
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun buildLexiconAction(word: String, displayName: String): RemoteAction {
        val intent = Intent(this, com.lexicon.MainActivity::class.java).apply {
            action = LexiconWordActionReceiver.ACTION_LOOKUP_WORD
            putExtra(LexiconWordActionReceiver.EXTRA_WORD, word)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            word.hashCode(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        val icon = Icon.createWithResource(this, android.R.drawable.ic_menu_search)
        return RemoteAction(
            icon,
            "Lexora: $displayName",
            "Look up \"$displayName\" in Lexora",
            pendingIntent,
        )
    }
}
