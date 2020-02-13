package com.kivi.remote.presentation.home.recentdevices

import android.content.Context
import android.content.SharedPreferences
import android.net.nsd.NsdServiceInfo
import androidx.lifecycle.MutableLiveData
import com.kivi.remote.Screens
import com.kivi.remote.common.Constants
import com.kivi.remote.common.extensions.backToMain
import com.kivi.remote.common.extensions.string
import com.kivi.remote.common.restartApp
import com.kivi.remote.persistence.AppDatabase
import com.kivi.remote.persistence.model.RecentDevice
import com.kivi.remote.presentation.base.BaseViewModel
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