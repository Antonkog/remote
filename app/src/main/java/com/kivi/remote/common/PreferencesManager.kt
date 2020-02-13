package com.kivi.remote.common

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager

// todo remove this
@Deprecated(
    message = "Migrate to property delegation later, " +
            "see: https://blog.stylingandroid.com/kotlin-contexts-sharedpreferences/, " +
            "https://hackernoon.com/kotlin-delegates-in-android-development-part-1-50346cf4aed7",
    level = DeprecationLevel.WARNING
)
object PreferencesManager {
    private lateinit var preferences: SharedPreferences

    fun init(context: Context) {
        synchronized(this) {
            preferences = PreferenceManager.getDefaultSharedPreferences(context)
        }
    }

    fun setDarkMode(value: Boolean) = preferences.edit().putBoolean(Constants.DARK_MODE, value).apply()

    fun getDarkMode(): Boolean = preferences.getBoolean(Constants.DARK_MODE, true)

}