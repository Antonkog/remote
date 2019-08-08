package com.wezom.kiviremote.presentation.home.devicesearch

import android.arch.lifecycle.MutableLiveData
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import com.wezom.kiviremote.Screens
import com.wezom.kiviremote.bus.ConnectEvent
import com.wezom.kiviremote.bus.NetworkStateEvent
import com.wezom.kiviremote.common.RxBus
import com.wezom.kiviremote.common.extensions.getTvUniqueId
import com.wezom.kiviremote.common.extensions.removeMasks
import com.wezom.kiviremote.nsd.NsdHelper
import com.wezom.kiviremote.nsd.NsdHelper.SERVICE_MASK
import com.wezom.kiviremote.nsd.NsdServiceInfoWrapper
import com.wezom.kiviremote.nsd.NsdServiceModel
import com.wezom.kiviremote.persistence.AppDatabase
import com.wezom.kiviremote.persistence.model.RecentDevice
import com.wezom.kiviremote.presentation.base.BaseViewModel
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch
import ru.terrakok.cicerone.Router
import timber.log.Timber
import java.util.concurrent.CopyOnWriteArrayList


class DeviceSearchViewModel(
        private val nsdHelper: NsdHelper,
        private val router: Router,
        private val database: AppDatabase
) : BaseViewModel() {

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
    val nsdDevices = MutableLiveData<Set<NsdServiceInfoWrapper>>()

    private var resolveListener: NsdManager.ResolveListener? = null
    private var resolving = false
    private var serviceInfo: NsdServiceInfo? = null

    private val recentDevices = CopyOnWriteArrayList<RecentDevice>()

    fun updateRecentDevices() {
        disposables += database.recentDeviceDao()
                .all
                .subscribeOn(Schedulers.io())
                .subscribe({
                    with(recentDevices) {
                        clear()
                        addAll(it)
                    }
                }, { Timber.e(it, it.message) })
    }

    fun connect(data: NsdServiceInfoWrapper) {
        serviceInfo = data.service
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

    private fun checkIfRecent(
            wrapper: NsdServiceInfoWrapper,
            recentDevices: CopyOnWriteArrayList<RecentDevice>
    ): NsdServiceInfoWrapper {
        recentDevices.takeIf { it.isNotEmpty() }?.let { devices ->
            devices.forEach {
                val name = it.actualName
                val userDefinedName = it.userDefinedName
                if (name != null && wrapper.serviceName.contains(name.getTvUniqueId())) {
                    if (name != wrapper.serviceName) {
                        val value = RecentDevice(it.id, wrapper.serviceName, userDefinedName)
                        launch(CommonPool) {
                            database.recentDeviceDao().insertAll(value)
                        }
                    }

                    if (userDefinedName != null) {
                        return NsdServiceInfoWrapper(wrapper.service, userDefinedName)
                    }
                }
            }
        }
        return NsdServiceInfoWrapper(wrapper.service, wrapper.serviceName.removeMasks())
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
                    navigateToMainScreen(service)
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

    private fun handleDevices(devices: Set<NsdServiceInfoWrapper>) {
        when {
            devices.size == 1 -> {
                val wrapper = checkIfRecent(devices.first(), recentDevices)
                serviceInfo = wrapper.service
                nsdDevices.postValue(setOf(wrapper))
                Timber.d("Found single device: ${wrapper.serviceName}")
            }
            devices.size > 1 -> devices.forEach {
                val cleanDevices = devices.mapTo(HashSet(), { checkIfRecent(it, recentDevices) })
                nsdDevices.postValue(cleanDevices)
                Timber.d("Found multiple devices: ")
                cleanDevices.forEach { Timber.d("device: ${it.serviceName}") }
            }
            devices.isEmpty() -> {
                nsdDevices.postValue(setOf())
                Timber.d("KIVI_NSD device is not found.")
            }
        }
    }
    /***
    todo: temp recheck if NsdServiceModel need to be passed
    NsdServiceModel(
    service.host,
    service.port, service.serviceName
    )

     */
    private fun navigateToMainScreen(service: NsdServiceInfo) {
        if (service.host != null) {
            nsdHelper.stopDiscovery()
            router.navigateTo(Screens.RECOMMENDATIONS_FRAGMENT)
        }
    }
}