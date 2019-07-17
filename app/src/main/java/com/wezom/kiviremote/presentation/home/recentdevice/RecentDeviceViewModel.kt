package com.wezom.kiviremote.presentation.home.recentdevice

import com.wezom.kiviremote.persistence.AppDatabase
import com.wezom.kiviremote.persistence.model.RecentDevice
import com.wezom.kiviremote.presentation.base.BaseViewModel
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.run
import ru.terrakok.cicerone.Router


class RecentDeviceViewModel(val database: AppDatabase, val router: Router) : BaseViewModel() {
    suspend fun saveChanges(model: RecentDevice) {
        run(CommonPool) {
            database.recentDeviceDao().insertAll(model)
        }
    }

    fun goBack() {
        router.exit()
    }

}