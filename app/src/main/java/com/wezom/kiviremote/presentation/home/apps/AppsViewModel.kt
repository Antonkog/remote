package com.wezom.kiviremote.presentation.home.apps

import android.arch.lifecycle.MutableLiveData
import android.content.SharedPreferences
import android.databinding.ObservableField
import android.graphics.BitmapFactory
import com.wezom.kiviremote.R
import com.wezom.kiviremote.bus.LaunchAppEvent
import com.wezom.kiviremote.bus.NavigateToRemoteEvent
import com.wezom.kiviremote.bus.RequestAppsEvent
import com.wezom.kiviremote.common.Constants
import com.wezom.kiviremote.common.Constants.CURRENT_CONNECTION_KEY
import com.wezom.kiviremote.common.KiviCache
import com.wezom.kiviremote.common.ResourceProvider
import com.wezom.kiviremote.common.RxBus
import com.wezom.kiviremote.common.extensions.string
import com.wezom.kiviremote.persistence.AppDatabase
import com.wezom.kiviremote.presentation.base.BaseViewModel
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.util.*


class AppsViewModel(
        val database: AppDatabase,
        private val cache: KiviCache, preferences: SharedPreferences,
        private val resourceProvider: ResourceProvider
) : BaseViewModel() {

    private var currentConnection by preferences.string(
            Constants.UNIDENTIFIED,
            key = CURRENT_CONNECTION_KEY
    )

    val apps = MutableLiveData<List<AppModel>>()
//    val deviceName = MutableLiveData<String>()

    val deviceName = ObservableField<String>()

    fun populateApps() {
        Timber.d("Populate app list")
        disposables += database.serverAppDao()
                .all
                .subscribeOn(Schedulers.computation())
                .subscribeBy(
                        onNext = { dbApps ->
                            val apps = ArrayList<AppModel>()
                            dbApps.forEach {
                                if (cache.get(it.appName) == null) {
                                    val bmp = BitmapFactory.decodeByteArray(
                                            it.appIcon,
                                            0,
                                            it.appIcon.size
                                    )
                                    val appName = it.appName;
                                    bmp?.let {
                                        cache.put(appName, bmp)
                                    }

                                }

                                if (cache.get(it.appName) != null) {
                                    apps.add(AppModel(it.appName, it.packageName))
                                }
                            }
                            this.apps.postValue(apps)
                        },
                        onError = Timber::e
                )
    }

    fun requestApps() = RxBus.publish(RequestAppsEvent())

    private fun setCurrentDeviceName(value: String) {
        deviceName.set(resourceProvider.getString(R.string.current_device, value))
        deviceName.notifyChange()
    }

    fun launchApp(name: String) {
        RxBus.publish(LaunchAppEvent(name))
        RxBus.publish(NavigateToRemoteEvent())
    }
}