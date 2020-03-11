package com.kivi.remote.presentation.home.recommendations.deep

import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import com.kivi.remote.R
import com.kivi.remote.bus.LaunchAppEvent
import com.kivi.remote.bus.NavigateToRemoteEvent
import com.kivi.remote.common.Constants
import com.kivi.remote.common.KiviCache
import com.kivi.remote.common.RxBus
import com.kivi.remote.net.model.ServerAppInfo
import com.kivi.remote.persistence.AppDatabase
import com.kivi.remote.presentation.base.BaseViewModel
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class AppsDeepViewModel(val navController: NavController, val cache: KiviCache, val database: AppDatabase): BaseViewModel() {

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
            navController.navigate(R.id.action_recsAppsDeepFragment_to_mediaFragment)
        }
    }

}