package com.kivi.remote.presentation.home.devicesearch

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import com.kivi.remote.R
import com.kivi.remote.bus.ConnectEvent
import com.kivi.remote.bus.NetworkStateEvent
import com.kivi.remote.common.Constants
import com.kivi.remote.common.RxBus
import com.kivi.remote.common.extensions.Run
import com.kivi.remote.common.extensions.boolean
import com.kivi.remote.common.extensions.remove032Space
import com.kivi.remote.common.extensions.string
import com.kivi.remote.nsd.NsdHelper
import com.kivi.remote.nsd.NsdHelper.SERVICE_MASK
import com.kivi.remote.nsd.NsdServiceModel
import com.kivi.remote.persistence.AppDatabase
import com.kivi.remote.persistence.model.RecentDevice
import com.kivi.remote.presentation.base.BaseViewModel
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber


class DeviceSearchViewModel(
        private val nsdHelper: NsdHelper,
        private val navController: NavController,
        private val database: AppDatabase,
        private val preferences: SharedPreferences
) : BaseViewModel() {

    private var lastNsdHolderName by preferences.string(Constants.UNIDENTIFIED, key = Constants.LAST_NSD_HOLDER_NAME)
    private var autoConnect by preferences.boolean(false, Constants.AUTO_CONNECT)
    private var tuturialDone by preferences.boolean(false, Constants.TUTORIAL_DONE)

    init {
        GlobalScope.launch(Dispatchers.Default) {
            // moke all offline
            database.recentDeviceDao().all?.forEach { device ->
              val update =   database.recentDeviceDao().update(device.apply { isOnline = false })
                if(update > 0) Timber.e(" updated as offline " + device.actualName)
                else Timber.e("was NOT updated as offline " + device.actualName)
            }
        }
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

        if(!tuturialDone){
            navController.navigate(R.id.action_deviceSearchFragment_to_tutorialFragment)
        }
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
                if (errorCode != NsdManager.FAILURE_ALREADY_ACTIVE) GlobalScope.launch(Dispatchers.Default) {
                    val updated = database.recentDeviceDao().update(RecentDevice(serviceInfo.serviceName).apply {
                        isOnline = false
                    })
                    Timber.e("Resolve failed updated as offline in db: $updated")
                }
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
                    GlobalScope.launch(Dispatchers.Default) {

                        val device = RecentDevice(service.serviceName).apply {
                            isOnline = true
                            wasConnected = System.currentTimeMillis()}

                        Timber.e("trying to update: " + device.actualName)

                        val updated = database.recentDeviceDao().update(device)

                        if (updated > 0) Timber.e("Resolved  updated as online in db: $updated")
                        else  Timber.e("Resolved NOT updated as online in db: $updated")
                    }
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
                nsdHelper.nsdRelay.subscribe(this::handleDevices) { e -> Timber.e(e, e.message) }
        nsdHelper.discoverServices()
    }

    private fun handleDevices(devices: Set<NsdServiceInfo>) {
        nsdDevices.postValue(devices)
        devices.forEach { Timber.d(" found device: ${it}") }
        GlobalScope.launch(Dispatchers.Default) {
            devices.takeIf { it != null && it.isNotEmpty() }?.forEach {
                upsert(it.serviceName, database)
            }
        }
    }

    @SuppressLint("CheckResult")
    fun upsert(serviceName: String, database: AppDatabase) { //to update saving old name
        val id = database.recentDeviceDao().insert(RecentDevice(serviceName))
        if (id == -1L) {
            database.recentDeviceDao().getDevice(serviceName).subscribe {
                if (!it.isOnline) {
                    val result = database.recentDeviceDao().update(it.apply { isOnline = true })
                    Timber.e(" insert in db Ignore : $serviceName updating $result")
                }
            }
        } else {
            Timber.e(" insert in db success : $serviceName")
        }
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
            GlobalScope.launch(Dispatchers.Main){ //todo: recheck if navController is ready.
                navController.navigate(R.id.action_deviceSearchFragment_to_recommendationsFragment)
            }
        }
    }
}