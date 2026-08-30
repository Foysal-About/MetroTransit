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

    /**
     * True while a "remember me" session is standing. Signing in without it keeps the session
     * in memory only, so the next cold start asks again.
     */
    var hasRememberedSession: Boolean
        get() = prefs.getBoolean(KEY_REMEMBERED_SESSION, false)
        set(value) {
            prefs.edit().putBoolean(KEY_REMEMBERED_SESSION, value).apply()
        }

    /**
     * The mobile number or email of the last rider to sign in, kept so the sign-in page can
     * greet them and prefill the field. Null once they sign out.
     */
    var rememberedIdentifier: String?
        get() = prefs.getString(KEY_REMEMBERED_IDENTIFIER, null)
        set(value) {
            prefs.edit().putString(KEY_REMEMBERED_IDENTIFIER, value).apply()
        }

    private companion object {
        const val KEY_SEEN_ONBOARDING = "has_seen_onboarding"
        const val KEY_REMEMBERED_SESSION = "has_remembered_session"
        const val KEY_REMEMBERED_IDENTIFIER = "remembered_identifier"
    }
}