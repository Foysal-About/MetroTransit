package com.example.metrotransit.data

import android.content.Context

/**
 * The handful of settings that must outlive the process. Kept deliberately small —
 * everything else in the app is in-memory session state.
 */
class AppPreferences(context: Context) {
    private val prefs =
        context.getSharedPreferences("metro_transit_prefs", Context.MODE_PRIVATE)

    /** False until the rider has been through the welcome page once. */
    var hasSeenOnboarding: Boolean
        get() = prefs.getBoolean(KEY_SEEN_ONBOARDING, false)
        set(value) {
            prefs.edit().putBoolean(KEY_SEEN_ONBOARDING, value).apply()
        }

    private companion object {
        const val KEY_SEEN_ONBOARDING = "has_seen_onboarding"
    }
}