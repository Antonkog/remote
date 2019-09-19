package com.wezom.kiviremote.presentation.home.recentdevice

import android.content.SharedPreferences
import com.wezom.kiviremote.common.Constants
import com.wezom.kiviremote.common.extensions.string
import com.wezom.kiviremote.persistence.AppDatabase
import com.wezom.kiviremote.persistence.model.RecentDevice
import com.wezom.kiviremote.presentation.base.BaseViewModel
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.run
import ru.terrakok.cicerone.Router


class RecentDeviceViewModel(val database: AppDatabase, val router: Router, preferences: SharedPreferences) : BaseViewModel() {

    var lastNsdHolderName by preferences.string(Constants.UNIDENTIFIED, key = Constants.LAST_NSD_HOLDER_NAME)

    suspend fun saveChanges(model: RecentDevice) {
        run(CommonPool) {
            database.recentDeviceDao().update(model)
        }
    }

    fun goBack(recentDevice: RecentDevice) {
        database.recentDeviceDao().removeByName(recentDevice.actualName)
        router.exit()
    }

}