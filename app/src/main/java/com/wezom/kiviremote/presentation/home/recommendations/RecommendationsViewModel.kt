package com.wezom.kiviremote.presentation.home.recommendations

import android.arch.lifecycle.MutableLiveData
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.wezom.kiviremote.Screens
import com.wezom.kiviremote.bus.*
import com.wezom.kiviremote.common.Constants
import com.wezom.kiviremote.common.KiviCache
import com.wezom.kiviremote.common.LowCostLRUCache
import com.wezom.kiviremote.common.RxBus
import com.wezom.kiviremote.common.extensions.Run
import com.wezom.kiviremote.net.model.AspectMessage
import com.wezom.kiviremote.net.model.RecommendItem
import com.wezom.kiviremote.persistence.AppDatabase
import com.wezom.kiviremote.presentation.base.BaseViewModel
import com.wezom.kiviremote.presentation.home.apps.AppModel
import com.wezom.kiviremote.upnp.UPnPManager
import com.wezom.kiviremote.upnp.org.droidupnp.view.Port
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import ru.terrakok.cicerone.Router
import timber.log.Timber
import java.util.*
import kotlin.collections.ArrayList

class RecommendationsViewModel(private val router: Router,
                               val database: AppDatabase,
                               private val cache: KiviCache,
                               preferences: SharedPreferences,
                               private val uPnPManager: UPnPManager) : BaseViewModel() {

    var aspectTryCounter = Constants.ASPECT_GET_TRY
    var lastPortId = Constants.INPUT_HOME_ID


    val recommendations = MutableLiveData<List<Comparable<RecommendItem>>>()
    val apps = MutableLiveData<List<Comparable<AppModel>>>()
    val ports = MutableLiveData<List<Comparable<Port>>>()


    fun populateApps() {
        Timber.d("Populate app list")
        disposables += database.serverAppDao()
                .all
                .subscribeOn(Schedulers.computation())
                .subscribeBy(
                        onNext = { dbApps ->
                            val recommendations = ArrayList<AppModel>()
                            dbApps.forEach {
                                val key = it.appName
                                if (cache.get(key) == null) {
                                    BitmapFactory.decodeByteArray(
                                            it.appIcon,
                                            0,
                                            it.appIcon.size
                                    ).let {
                                        LowCostLRUCache<String, Bitmap>().put(key, it)
                                    }
                                } else {
                                    LowCostLRUCache<String, Bitmap>().put(key, cache.get(key))
                                }

                                if (LowCostLRUCache<String, Bitmap>().get(key) != null)
                                    recommendations.add(AppModel(it.appName, it.packageName))
                            }
                            this.apps.postValue(recommendations)
                        },
                        onError = Timber::e
                )
    }

    fun requestApps() = RxBus.publish(RequestAppsEvent())


    fun setRecommendData(): List<RecommendItem> {
        val list = LinkedList<RecommendItem>()
        Timber.d("Populate RecommendItem list")
        list.addLast(
                RecommendItem(
                        RecommendationsAdapter.TYPE_RECOMMENDATIONS,
                        title = "The Godfather",
                        serverId = 1,
                        url = "https://m.media-amazon.com/images/M/MV5BM2MyNjYxNmUtYTAwNi00MTYxLWJmNWYtYzZlODY3ZTk3OTFlXkEyXkFqcGdeQXVyNzkwMjQ5NzM@._V1_SX300.jpg"
                )
        )

        list.addLast(
                RecommendItem(
                        RecommendationsAdapter.TYPE_RECOMMENDATIONS,
                        title = "Disco Godfather",
                        serverId = 2,
                        url = "https://m.media-amazon.com/images/M/MV5BMTU5MzAyMTY1Ml5BMl5BanBnXkFtZTgwNzA2MjI4MzE@._V1._CR46,89.5,1255,1862_SX89_AL_.jpg_V1_SX300.jpg"
                )
        )



        list.addLast(
                RecommendItem(
                        RecommendationsAdapter.TYPE_RECOMMENDATIONS,
                        title = "The Godfather Family: A Look Inside",
                        serverId = 3,
                        url = "https://m.media-amazon.com/images/M/MV5BMTUzOTc0NDAyNF5BMl5BanBnXkFtZTcwNjAwMDEzMQ@@._V1_SX300.jpg"
                )
        )

        list.addLast(
                RecommendItem(
                        RecommendationsAdapter.TYPE_RECOMMENDATIONS,
                        title = "The Godfather Trilogy: 1901-1980",
                        serverId = 4,
                        url = "https://m.media-amazon.com/images/M/MV5BMTY1NzYxNDk0NV5BMl5BanBnXkFtZTYwMjk5MTM5._V1_SX300.jpg"
                )
        )
        return list

//            this.recommendations.postValue(list)
    }

    fun observePorts() {
        disposables += RxBus.listen(GotAspectEvent::class.java).observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                        onNext = {
                            val ports = it?.getPortsList() ?: LinkedList()
                            if (!ports.isEmpty()) {
                                var containsActive = false
                                val recommendations = ArrayList<Port>()

                                for (port in ports) {
                                    if (port.active && port.portNum == lastPortId || aspectTryCounter == 0)
                                        containsActive = true
                                    recommendations.add(port)
                                }

                                if (containsActive) this.ports.postValue(recommendations)
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

    fun launchApp(name: String) {
        RxBus.publish(LaunchAppEvent(name))
        RxBus.publish(NavigateToRemoteEvent())
    }

    fun goSearch() = router.navigateTo(Screens.DEVICE_SEARCH_FRAGMENT)

    fun godo(data: RecommendItem) = router.navigateTo(Screens.RECENT_DEVICE_FRAGMENT, data)


    fun startUPnPController() = uPnPManager.controller.resume()


}

