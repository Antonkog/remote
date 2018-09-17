package com.wezom.kiviremote.presentation.home.recentdevices

import android.arch.lifecycle.MutableLiveData
import com.wezom.kiviremote.Screens
import com.wezom.kiviremote.common.extensions.backToMain
import com.wezom.kiviremote.nsd.NsdHelper
import com.wezom.kiviremote.nsd.NsdServiceInfoWrapper
import com.wezom.kiviremote.persistence.AppDatabase
import com.wezom.kiviremote.persistence.model.RecentDevice
import com.wezom.kiviremote.presentation.base.BaseViewModel
import ru.terrakok.cicerone.Router
import timber.log.Timber


class RecentDevicesViewModel(private val router: Router,
                             private val database: AppDatabase,
                             private val nsdHelper: NsdHelper) : BaseViewModel() {

    val nsdServices = MutableLiveData<Set<NsdServiceInfoWrapper>>()

    val recentDevices = MutableLiveData<List<RecentDevice>>()

    fun navigateToHome() = router.backTo(Screens.DEVICE_SEARCH_FRAGMENT)

    fun navigateBack() = router.exit()

    fun navigateToRecentDevice(data: RecentDevice) = router.navigateTo(Screens.RECENT_DEVICE_FRAGMENT, data)

    fun requestRecentDevices() {
        disposables += database.recentDeviceDao().all.backToMain()
                .subscribe({ result -> recentDevices.postValue(result) }, { t -> Timber.e(t, t.message) })
    }

    fun discoverDevices() {
        disposables += nsdHelper.nsdRelay
                .subscribe({ nsdServices.postValue(it as Set<NsdServiceInfoWrapper>) }, { e -> Timber.e(e, e.message) })
    }
}