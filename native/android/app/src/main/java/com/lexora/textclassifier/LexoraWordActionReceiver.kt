package com.lexora.textclassifier

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.lexora.MainActivity

/**
 * LexoraWordActionReceiver — forwards the "Lexora" text-selection action
 * to [MainActivity] so the WordDetailSheet can open for the tapped word.
 *
 * Flow:
 *   1. User selects a word in any text field.
 *   2. [LexoraTextClassifierService] injects a "Lexora" RemoteAction whose
 *      PendingIntent carries an Intent with action = "com.lexora.LOOKUP_WORD"
 *      and a "word" String extra.
 *   3. Tapping the action delivers the Intent here.
 *   4. This receiver re-launches [MainActivity] (NEW_TASK) and forwards the
 *      "word" extra so MainActivity can hydrate the WordDetailSheet.
 *
 * Registration (AndroidManifest.xml):
 *   <receiver
 *       android:name=".textclassifier.LexoraWordActionReceiver"
 *       android:exported="false">
 *       <intent-filter>
 *           <action android:name="com.lexora.LOOKUP_WORD" />
 *       </intent-filter>
 *   </receiver>
 *
 * MainActivity can then read the extra in onCreate / onNewIntent via:
 *   intent.getStringExtra(LexoraWordActionReceiver.EXTRA_WORD)
 * and call `viewModel.openWord(...)` to surface the WordDetailSheet.
 */
class LexoraWordActionReceiver : BroadcastReceiver() {

    companion object {
        /** Intent action fired by [LexoraTextClassifierService]. */
        const val ACTION_LOOKUP_WORD = "com.lexora.LOOKUP_WORD"

        /** String extra carrying the word to look up. */
        const val EXTRA_WORD = "word"
    }

    override fun onReceive(context: Context, intent: Intent) {
        // Only respond to our action — ignore any stray broadcasts.
        if (intent.action != ACTION_LOOKUP_WORD) return

        val word = intent.getStringExtra(EXTRA_WORD)?.trim()
        // No word to look up — nothing to do.
        if (word.isNullOrEmpty()) return

        // Forward the word to MainActivity. FLAG_ACTIVITY_NEW_TASK is
        // mandatory because we're starting an Activity from a non-Activity
        // context (a BroadcastReceiver has no Activity task to anchor to).
        val launch = Intent(context, MainActivity::class.java).apply {
            putExtra(EXTRA_WORD, word)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(launch)
    }
}
