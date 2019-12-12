package com.wezom.kiviremote.presentation.home.recentdevice

import android.content.SharedPreferences
import com.wezom.kiviremote.common.Constants
import com.wezom.kiviremote.common.extensions.string
import com.wezom.kiviremote.persistence.AppDatabase
import com.wezom.kiviremote.persistence.model.RecentDevice
import com.wezom.kiviremote.presentation.base.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import ru.terrakok.cicerone.Router
import timber.log.Timber


class RecentDeviceViewModel(val database: AppDatabase, val router: Router, preferences: SharedPreferences) : BaseViewModel() {

    var lastNsdHolderName by preferences.string(Constants.UNIDENTIFIED, key = Constants.LAST_NSD_HOLDER_NAME)

    fun saveChanges(model: RecentDevice) {
        GlobalScope.launch(Dispatchers.Default) {
            val update = database.recentDeviceDao().update(model)
            if (update > 0) Timber.e("setting new name in db: $update")
        }
    }

    fun goBack(recentDevice: RecentDevice) {
        database.recentDeviceDao().removeByName(recentDevice.actualName)
        router.exit()
    }

}