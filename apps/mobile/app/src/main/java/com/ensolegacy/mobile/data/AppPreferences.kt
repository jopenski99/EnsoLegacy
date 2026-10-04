package com.ensolegacy.mobile.data

import android.content.Context

/**
 * Lightweight key/value store for small app-wide flags (e.g. whether the
 * onboarding flow has been seen). Backed by [android.content.SharedPreferences]
 * — synchronous reads, no extra dependency — which is enough for a handful of
 * booleans. Reach for DataStore only if this grows into real preference state.
 */
class AppPreferences(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /** True once the user has finished (or skipped) the onboarding flow. */
    var hasCompletedOnboarding: Boolean
        get() = prefs.getBoolean(KEY_ONBOARDING_COMPLETE, false)
        set(value) = prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETE, value).apply()

    /**
     * True once the user has declined the "restore from backup?" prompt shown
     * on a fresh install when a device backup is found. Restore stays
     * available in Settings; this just stops the prompt from nagging.
     */
    var backupRestorePromptDismissed: Boolean
        get() = prefs.getBoolean(KEY_BACKUP_PROMPT_DISMISSED, false)
        set(value) = prefs.edit().putBoolean(KEY_BACKUP_PROMPT_DISMISSED, value).apply()

    private companion object {
        const val PREFS_NAME = "enso_prefs"
        const val KEY_ONBOARDING_COMPLETE = "onboarding_complete"
        const val KEY_BACKUP_PROMPT_DISMISSED = "backup_restore_prompt_dismissed"
    }
}
