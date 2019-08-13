package com.wezom.kiviremote.presentation.home.recentdevices

import android.arch.lifecycle.MutableLiveData
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import com.wezom.kiviremote.Screens
import com.wezom.kiviremote.bus.ConnectEvent
import com.wezom.kiviremote.common.RxBus
import com.wezom.kiviremote.common.extensions.backToMain
import com.wezom.kiviremote.nsd.NsdHelper
import com.wezom.kiviremote.nsd.NsdServiceInfoWrapper
import com.wezom.kiviremote.nsd.NsdServiceModel
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

    private var resolveListener: NsdManager.ResolveListener? = null
    private var resolving = false
    private var serviceInfo: NsdServiceInfo? = null

    fun navigateToHome() = router.backTo(Screens.DEVICE_SEARCH_FRAGMENT)

    fun navigateBack() = router.exit()

    fun navigateToRecentDevice(data: TvDeviceInfo) = router.navigateTo(Screens.RECENT_DEVICE_FRAGMENT, data)

    fun connect(data: NsdServiceInfoWrapper) {
        serviceInfo = data.service
        connect()
    }

    private fun connect() {
        if (!resolving) {
            resolveService()
        }
    }

    private fun resolveService() {
        resolving = true
        nsdHelper.resolve(serviceInfo, resolveListener)
    }

    fun requestLastRecentDevices(count: Int = 5) {
        disposables += database.recentDeviceDao().getLastAdded(count).backToMain()
                .subscribe({ result -> recentDevices.postValue(result) }, { t -> Timber.e(t, t.message) })
    }

    fun requestRecentDevices() {
        disposables += database.recentDeviceDao().all.backToMain()
                .subscribe({ result -> recentDevices.postValue(result) }, { t -> Timber.e(t, t.message) })
    }

    fun discoverDevices() {
        disposables += nsdHelper.nsdRelay
                .subscribe({ nsdServices.postValue(it as Set<NsdServiceInfoWrapper>) }, { e -> Timber.e(e, e.message) })
    }

    fun initResolveListener() {
        resolveListener = object : NsdManager.ResolveListener {
            override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                Timber.e("Resolve failed: $errorCode")
                when (errorCode) {
                    NsdManager.FAILURE_ALREADY_ACTIVE -> {
                        Timber.e("FAILURE_ALREADY_ACTIVE")
                        nsdHelper.resolve(serviceInfo, resolveListener)
                    }
                    NsdManager.FAILURE_INTERNAL_ERROR -> {
                        Timber.e("FAILURE_INTERNAL_ERROR")
                        resolving = false
                    }
                    NsdManager.FAILURE_MAX_LIMIT -> {
                        Timber.e("FAILURE_MAX_LIMIT")
                        resolving = false
                    }
                }
            }

            override fun onServiceResolved(service: NsdServiceInfo) {
                Timber.d("Resolve Succeeded: $serviceInfo")

                if (service.serviceName == NsdHelper.SERVICE_MASK) {
                    return
                }

                if (service.host != null && service.serviceName != null) {
                    serviceInfo = service
                    RxBus.publish(
                            ConnectEvent(
                                    NsdServiceModel(
                                            service.host,
                                            service.port,
                                            service.serviceName
                                    )
                            )
                    )
                    navigateToMainScreen(service)
                    resolving = false
                }
            }
        }
        nsdHelper.initializeResolveListener(resolveListener)
    }

    private fun navigateToMainScreen(service: NsdServiceInfo) {
        if (service.host != null) {
            nsdHelper.stopDiscovery()
            router.navigateTo(
                    Screens.RECOMMENDATIONS_FRAGMENT, NsdServiceModel(
                    service.host,
                    service.port, service.serviceName
            )
            )
        }
    }

}