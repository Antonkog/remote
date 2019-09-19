package com.wezom.kiviremote.presentation.home.recentdevices

import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.content.SharedPreferences
import android.net.nsd.NsdServiceInfo
import com.wezom.kiviremote.Screens
import com.wezom.kiviremote.common.Constants
import com.wezom.kiviremote.common.extensions.backToMain
import com.wezom.kiviremote.common.extensions.string
import com.wezom.kiviremote.common.restartApp
import com.wezom.kiviremote.persistence.AppDatabase
import com.wezom.kiviremote.persistence.model.RecentDevice
import com.wezom.kiviremote.presentation.base.BaseViewModel
import ru.terrakok.cicerone.Router
import timber.log.Timber

class RecentDevicesViewModel(private val router: Router,
                             private val database: AppDatabase,
                             preferences: SharedPreferences) : BaseViewModel() {

    var lastNsdHolderName by preferences.string(Constants.UNIDENTIFIED, key = Constants.LAST_NSD_HOLDER_NAME)

    val recentDevices = MutableLiveData<List<RecentDevice>>()

    fun navigateToRecentDevice(data: RecentDevice) = router.navigateTo(Screens.RECENT_DEVICE_FRAGMENT, data)

    fun connect(data: NsdServiceInfo, contetx: Context) {
        lastNsdHolderName = data.serviceName
        restartApp(contetx)
    }

    fun requestRecentDevices() {
        disposables += database.recentDeviceDao().fiveByConnection.backToMain()
                .subscribe({ result -> recentDevices.postValue(result) }, { t -> Timber.e(t, t.message) })
    }
}