package com.wezom.kiviremote.presentation.home.recommendations

import android.arch.lifecycle.MutableLiveData
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import com.wezom.kiviremote.Screens
import com.wezom.kiviremote.bus.*
import com.wezom.kiviremote.common.Constants
import com.wezom.kiviremote.common.KiviCache
import com.wezom.kiviremote.common.RxBus
import com.wezom.kiviremote.common.extensions.Run
import com.wezom.kiviremote.net.model.AspectMessage
import com.wezom.kiviremote.net.model.RecommendItem
import com.wezom.kiviremote.persistence.AppDatabase
import com.wezom.kiviremote.presentation.base.BaseViewModel
import com.wezom.kiviremote.upnp.UPnPManager
import com.wezom.kiviremote.views.HorizontalCardsView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import ru.terrakok.cicerone.Router
import timber.log.Timber
import java.util.*

class RecommendationsViewModel(private val router: Router,
                               val database: AppDatabase,
                               private val cache: KiviCache,
                               preferences: SharedPreferences,
                               private val uPnPManager: UPnPManager) : BaseViewModel() {

    var aspectTryCounter = Constants.ASPECT_GET_TRY
    var lastPortId = Constants.INPUT_HOME_ID


    val recommendations = MutableLiveData<List<RecommendItem>>()
//    val apps = MutableLiveData<List<AppModel>>()
//    val aspectEvent = MutableLiveData<GotAspectEvent>()


    fun populateApps() {
        Timber.d("Populate app list")
        disposables += database.serverAppDao()
                .all
                .subscribeOn(Schedulers.computation())
                .subscribeBy(
                        onNext = { dbApps ->
                            val recommendations = ArrayList<RecommendItem>()
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
                                recommendations.add(RecommendItem(HorizontalCardsView.ContentType.TYPE_APPS.ordinal, serverId = it.id, title = it.appName, packageName = it.packageName, imageId = -1, url = ""))
                            }
                            updateRecommendations(recommendations)
                        },
                        onError = Timber::e
                )
    }

    fun requestApps() = RxBus.publish(RequestAppsEvent())


    fun observePorts() {
        disposables += RxBus.listen(GotAspectEvent::class.java).observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                        onNext = {
                            var ports = it?.getPortsList() ?: LinkedList()
                            if (!ports.isEmpty()) {
                                var containsActive = false
                                val recommendations = ArrayList<RecommendItem>()

                                for (port in ports) {
                                    if (port.active && port.portNum == lastPortId || aspectTryCounter == 0)
                                        containsActive = true
                                    recommendations.add(RecommendItem(HorizontalCardsView.ContentType.TYPE_INPUTS.ordinal, serverId = port.portNum, title = port.portName, packageName = "", imageId = port.portImageId, url = ""))
                                }

                                if (containsActive) updateRecommendations(recommendations)
                                else {
                                    Run.after(1000) {
                                        requestAspect()
                                    }
                                    aspectTryCounter--
                                }
                            } else {
                                requestAspect()
                            }
                        }, onError = Timber::e
                )
    }

    fun sendAspectSingleChangeEvent(valueType: AspectMessage.ASPECT_VALUE, value: Int) {
        val builder = AspectMessage.AspectMsgBuilder()
        builder.addValue(valueType, value)
        RxBus.publish(NewAspectEvent(builder.buildAspect()))
    }

    fun requestAspect() = RxBus.publish(RequestAspectEvent())


    fun updateRecommendations(recs: List<RecommendItem>) {
        this.recommendations.postValue(recs)
    }

    fun launchApp(name: String) {
        RxBus.publish(LaunchAppEvent(name))
        RxBus.publish(NavigateToRemoteEvent())
    }

    fun goSearch() = router.navigateTo(Screens.DEVICE_SEARCH_FRAGMENT)

    fun godo(data: RecommendItem) = router.navigateTo(Screens.RECENT_DEVICE_FRAGMENT, data)


    fun startUPnPController() = uPnPManager.controller.resume()


}

