package com.lexicon.textclassifier

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.service.textclassifier.TextClassifierService
import android.view.textclassifier.TextClassification
import android.view.textclassifier.TextClassificationContext
import android.view.textclassifier.TextClassifier
import android.view.textclassifier.TextLinks
import androidx.annotation.RequiresApi
import com.lexicon.engine.LexiconEngine

/**
 * LexiconTextClassifierService — extends Android's TextClassifierService.
 *
 * Per the Lexicon spec:
 *   "Do use the TextClassifier API. Do not use SYSTEM_ALERT_WINDOW or floating overlays.
 *    Do extend TextClassifier and override classifyText().
 *    Wrap the system DefaultTextClassifier. Return a TextClassification object
 *    containing a RemoteAction pointing to your Lexicon translation service."
 *
 * This service injects a "Lexicon" action into the text selection menu when
 * the user selects a word. Tapping the action launches the Lexicon lookup.
 *
 * Registration: add to AndroidManifest.xml:
 *   <service
 *       android:name=".textclassifier.LexiconTextClassifierService"
 *       android:permission="android.permission.BIND_TEXTCLASSIFIER_SERVICE"
 *       android:exported="true">
 *       <intent-filter>
 *           <action android:name="android.service.textclassifier.TextClassifierService" />
 *       </intent-filter>
 *   </service>
 */
@RequiresApi(Build.VERSION_CODES.Q)
class LexiconTextClassifierService : TextClassifierService() {

    private var lexiconEngine: LexiconEngine? = null
    private var defaultClassifier: TextClassifier? = null

    override fun onConnected() {
        super.onConnected()
        // Initialize the C++ engine
        lexiconEngine = LexiconEngine()
        // Load database from app's internal storage
        val dbDir = getDatabaseDir()
        lexiconEngine?.loadDatabase(dbDir)
        // Wrap the system default classifier
        defaultClassifier = getSystemService(TextClassifierService::class.java)
            ?.defaultTextClassifier
    }

    override fun onDisconnected() {
        lexiconEngine?.close()
        lexiconEngine = null
        super.onDisconnected()
    }

    /**
     * Override classifyText to inject the "Lexicon" action.
     * The system calls this when the user selects text.
     */
    override fun onClassifyText(
        request: TextClassification.Request,
        callback: TextClassifierService.Response<TextClassification>
    ) {
        // First, get the system's default classification (Copy, Paste, etc.)
        val defaultClassification = defaultClassifier?.classifyText(request)
            ?: TextClassification.Builder().build()

        // Extract the selected text
        val selectedText = request.text
            .subSequence(request.getStartIndex(), request.getEndIndex())
            .toString()
            .trim()

        if (selectedText.isEmpty() || selectedText.contains(" ")) {
            // Single words only — don't add Lexicon for multi-word selections
            callback.onSuccess(defaultClassification)
            return
        }

        // Quick lookup — must be <10ms to avoid TextClassifier timeout
        val entry = lexiconEngine?.lookup(selectedText)
        if (entry == null) {
            callback.onSuccess(defaultClassification)
            return
        }

        // Build the Lexicon action
        val lexiconAction = buildLexiconAction(selectedText, entry.word)

        // Add the Lexicon action to the existing classification
        val enhancedClassification = TextClassification.Builder()
            .setText(defaultClassification.text)
            .addAction(0, lexiconAction) // Insert at position 0 (first action)
            .addActions(defaultClassification.actions)
            .setEntityConfidence(defaultClassification.entityConfidence)
            .build()

        callback.onSuccess(enhancedClassification)
    }

    /**
     * Build a RemoteAction that launches Lexicon when tapped.
     */
    private fun buildLexiconAction(word: String, displayName: String): android.app.RemoteAction {
        val intent = Intent().apply {
            action = "com.lexicon.LOOKUP_WORD"
            putExtra("word", word)
            putExtra("display_name", displayName)
            // Target the Lexicon app's main activity
            setClassName(packageName, "com.lexicon.MainActivity")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        val pendingIntent = android.app.PendingIntent.getActivity(
            this,
            word.hashCode(),
            intent,
            android.app.PendingIntent.FLAG_IMMUTABLE or android.app.PendingIntent.FLAG_UPDATE_CURRENT
        )

        val icon = Icon.createWithResource(this, android.R.drawable.ic_menu_search)
        val action = android.app.RemoteAction(
            icon,
            "Lexicon: $displayName",
            "Look up \"$displayName\" in Lexicon",
            pendingIntent
        )
        return action
    }

    /**
     * Override onSuggestSelection to optionally adjust selection boundaries.
     */
    override fun onSuggestSelection(
        request: TextSelection.Request,
        callback: TextClassifierService.Response<TextSelection>
    ) {
        // Delegate to default classifier
        val default = defaultClassifier?.suggestSelection(request)
        if (default != null) {
            callback.onSuccess(default)
        } else {
            callback.onSuccess(
                TextSelection.Builder(request.getStartIndex(), request.getEndIndex()).build()
            )
        }
    }

    /**
     * Override onGenerateLinks for linkification.
     */
    override fun onGenerateLinks(
        request: TextLinks.Request,
        callback: TextClassifierService.Response<TextLinks>
    ) {
        val default = defaultClassifier?.generateLinks(request)
        if (default != null) {
            callback.onSuccess(default)
        } else {
            callback.onSuccess(TextLinks.Builder(request.text).build())
        }
    }

    private fun getDatabaseDir(): String {
        return getDir("lexicon_db", Context.MODE_PRIVATE).absolutePath
    }
}
