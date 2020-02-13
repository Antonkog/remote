package com.kivi.remote.presentation.home.apps

import android.content.SharedPreferences
import android.graphics.BitmapFactory
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import com.kivi.remote.R
import com.kivi.remote.bus.LaunchAppEvent
import com.kivi.remote.bus.NavigateToRemoteEvent
import com.kivi.remote.bus.RequestAppsEvent
import com.kivi.remote.common.Constants
import com.kivi.remote.common.Constants.CURRENT_CONNECTION_KEY
import com.kivi.remote.common.KiviCache
import com.kivi.remote.common.ResourceProvider
import com.kivi.remote.common.RxBus
import com.kivi.remote.common.extensions.string
import com.kivi.remote.persistence.AppDatabase
import com.kivi.remote.presentation.base.BaseViewModel
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