package com.wezom.kiviremote.common

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.wezom.kiviremote.common.Constants.*

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

    fun getMuteStatus() = preferences.getBoolean(MUTE_STATUS_KEY, false)

    fun setCursorSpeed(value: Int) = preferences.edit().putInt(CURSOR_SPEED_KEY, value).apply()

    fun getCursorSpeed(): Int = preferences.getInt(CURSOR_SPEED_KEY, 50)

    fun setSelectedTab(value: Int) = preferences.edit().putInt(TAB_SELECTED_KEY, value).apply()

    fun setDarkMode(value: Boolean) = preferences.edit().putBoolean(Constants.DARK_MODE, value).apply()

    fun getDarkMode(): Boolean = preferences.getBoolean(Constants.DARK_MODE, true)

    fun isReconnectNeed(): Boolean = preferences.getBoolean(Constants.NEED_RECONNECT, false)

    fun setReconnectNeed(value: Boolean) = preferences.edit().putBoolean(Constants.NEED_RECONNECT, value).apply()

    fun getSelectedTab() = preferences.getInt(TAB_SELECTED_KEY, 0)
}