package com.wezom.kiviremote.presentation.home.devicesearch

import android.arch.lifecycle.MutableLiveData
import android.content.SharedPreferences
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import com.wezom.kiviremote.Screens
import com.wezom.kiviremote.bus.ConnectEvent
import com.wezom.kiviremote.bus.NetworkStateEvent
import com.wezom.kiviremote.common.Constants
import com.wezom.kiviremote.common.RxBus
import com.wezom.kiviremote.common.extensions.*
import com.wezom.kiviremote.nsd.NsdHelper
import com.wezom.kiviremote.nsd.NsdHelper.SERVICE_MASK
import com.wezom.kiviremote.nsd.NsdServiceModel
import com.wezom.kiviremote.persistence.AppDatabase
import com.wezom.kiviremote.persistence.model.RecentDevice
import com.wezom.kiviremote.presentation.base.BaseViewModel
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch
import ru.terrakok.cicerone.Router
import timber.log.Timber


class DeviceSearchViewModel(
        private val nsdHelper: NsdHelper,
        private val router: Router,
        private val database: AppDatabase,
        private val preferences: SharedPreferences
) : BaseViewModel() {

    private var lastNsdHolderName by preferences.string(Constants.UNIDENTIFIED, key = Constants.LAST_NSD_HOLDER_NAME)
    private var autoConnect by preferences.boolean(false, Constants.AUTO_CONNECT)

    init {
        disposables += RxBus.listen(NetworkStateEvent::class.java).subscribeBy(
                onNext = {
                    if (it.isAvailable) {
                        networkState.postValue(true)
                    } else {
                        networkState.postValue(false)
                        nsdHelper.nsdRelay.accept(setOf())
                        discoverDevices()
                    }
                }, onError = Timber::e
        )
    }

    val networkState = MutableLiveData<Boolean>()
    val nsdDevices = MutableLiveData<Set<NsdServiceInfo>>()

    private var resolveListener: NsdManager.ResolveListener? = null
    private var resolving = false
    private var serviceInfo: NsdServiceInfo? = null


    fun connect(data: NsdServiceInfo) {
        lastNsdHolderName = data.serviceName
        serviceInfo = data
        connect()
    }

    private fun resolveService() {
        resolving = true
        nsdHelper.resolve(serviceInfo, resolveListener)
    }

    private fun connect() {
        if (!resolving) {
            resolveService()
        }
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

                if (service.serviceName == SERVICE_MASK) {
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
                    navigateToRecommendations(service)
                    resolving = false
                }
            }
        }
        nsdHelper.initializeResolveListener(resolveListener)
    }

    var disposable: Disposable? = null

    fun discoverDevices() {
        disposable?.takeIf { !it.isDisposed }?.dispose()
        disposable =
                nsdHelper.nsdRelay.subscribe(this::handleDevices, { e -> Timber.e(e, e.message) })
        nsdHelper.discoverServices()
    }

    private fun handleDevices(devices: Set<NsdServiceInfo>) {
        nsdDevices.postValue(devices)
        launch(CommonPool) {
            database.recentDeviceDao().removeAll()
            devices.takeIf { it != null && it.isNotEmpty() }?.map { RecentDevice(it.serviceName, it.serviceName.removeMasks(), true) }.let {
                database.recentDeviceDao().insert(it)
            }
        }
        devices.forEach { Timber.d(" found device: ${it}") }
    }

    fun tryAutoConnect(set: Set<NsdServiceInfo>): Boolean {
        if (autoConnect)
            set.forEach {
                if (it.serviceName.remove032Space() == lastNsdHolderName.remove032Space()) {
                    Run.after(Constants.DELAY_AUTO_CONNECT) { connect(it) }
                    true
                }
            }
        return false
    }

    /***
    todo: temp recheck if NsdServiceModel need to be passed
    NsdServiceModel(
    service.host,
    service.port, service.serviceName
    )

     */
    private fun navigateToRecommendations(service: NsdServiceInfo) {
        if (service.host != null) {
            nsdHelper.stopDiscovery()
            router.navigateTo(Screens.RECOMMENDATIONS_FRAGMENT)
        }
    }
}