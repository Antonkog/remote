package com.wezom.kiviremote.presentation.home.recommendations.deep

import android.arch.lifecycle.MutableLiveData
import com.wezom.kiviremote.Screens
import com.wezom.kiviremote.bus.LaunchAppEvent
import com.wezom.kiviremote.bus.NavigateToRemoteEvent
import com.wezom.kiviremote.common.Constants
import com.wezom.kiviremote.common.KiviCache
import com.wezom.kiviremote.common.RxBus
import com.wezom.kiviremote.net.model.ServerAppInfo
import com.wezom.kiviremote.persistence.AppDatabase
import com.wezom.kiviremote.presentation.base.BaseViewModel
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import ru.terrakok.cicerone.Router
import timber.log.Timber

class AppsDeepViewModel (val router: Router, val cache: KiviCache, val database : AppDatabase): BaseViewModel() {

    val apps = MutableLiveData<List<ServerAppInfo>>()

    fun populateApps() {
        disposables += database.serverAppDao()
                .all
                .subscribeOn(Schedulers.computation())
                .subscribeBy(
                        onNext = { dbApps ->
                            val recommendations = ArrayList<ServerAppInfo>()
                            dbApps.forEach {
                                recommendations.add(ServerAppInfo(it.appName, it.packageName, it.baseIcon))
                            }
                            this.apps.postValue(recommendations)
                        },
                        onError = Timber::e
                )
    }


    fun launchApp(name: String) {
        if (name != Constants.MEDIA_SHARE_TXT_ID) {
            RxBus.publish(LaunchAppEvent(name))
            RxBus.publish(NavigateToRemoteEvent())
        } else {
            router.navigateTo(Screens.MEDIA_FRAGMENT)
        }
    }

}