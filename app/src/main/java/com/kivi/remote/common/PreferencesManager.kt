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
    fun incrementCrashCounter() = setCrashCounter(getCrashCounter() + 1)

    fun incrementLaunchCounter() = setLaunchCounter(getLaunchCounter() + 1)

    private fun setLaunchCounter(value: Int) = preferences.edit().putInt(Constants.LAUNCH_COUNTER, value).apply()

    private fun getLaunchCounter(): Int = preferences.getInt(Constants.LAUNCH_COUNTER, 0)

    private fun setCrashCounter(value: Int) = preferences.edit().putInt(Constants.CRASH_COUNTER, value).apply()

    private fun getCrashCounter(): Int = preferences.getInt(Constants.CRASH_COUNTER, 0)

    fun setShowUpdate(value: Boolean) = preferences.edit().putBoolean(Constants.UPDATE_SHOWING, value).apply()

    fun getShowUpdate(): Boolean = preferences.getBoolean(Constants.UPDATE_SHOWING, true)


    fun setDarkMode(value: Boolean) = preferences.edit().putBoolean(Constants.DARK_MODE, value).apply()

    fun getDarkMode(): Boolean = preferences.getBoolean(Constants.DARK_MODE, true)

}