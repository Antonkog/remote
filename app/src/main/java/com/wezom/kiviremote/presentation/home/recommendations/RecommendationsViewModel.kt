package com.wezom.kiviremote.presentation.home.recommendations

import android.arch.lifecycle.MutableLiveData
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.wezom.kiviremote.Screens
import com.wezom.kiviremote.bus.*
import com.wezom.kiviremote.common.*
import com.wezom.kiviremote.common.extensions.Run
import com.wezom.kiviremote.net.model.*
import com.wezom.kiviremote.persistence.AppDatabase
import com.wezom.kiviremote.presentation.base.BaseViewModel
import com.wezom.kiviremote.presentation.home.apps.AppModel
import com.wezom.kiviremote.upnp.UPnPManager
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

    val recommendations = MutableLiveData<List<Comparable<Recommendation>>>()
    val apps = MutableLiveData<List<Comparable<ServerAppInfo>>>()
    val inputs = MutableLiveData<List<Comparable<Input>>>()
    val channels = MutableLiveData<List<Comparable<Channel>>>()
    //
    fun requestApps() = RxBus.publish(SendActionEvent(Action.REQUEST_APPS))

    fun requestInputs() = RxBus.publish(SendActionEvent(Action.REQUEST_INPUTS))
    fun requestRecommendations() = RxBus.publish(SendActionEvent(Action.REQUEST_RECOMMENDATIONS))
    fun requestChannels() = RxBus.publish(SendActionEvent(Action.REQUEST_CHANNELS))
    fun requestPreviews() = RxBus.publish(RequestInitialPreviewEvent())

    fun observePreviews() {
        disposables += RxBus.listen(GotPreviewsInitialEvent::class.java).observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                        onNext = {
                            Timber.e("12345 got preview " + it.previewCommonStructures?.firstOrNull()?.toString())

                            recommendations.postValue(
                                    it.previewCommonStructures?.filter { it.type == LauncherBasedData.TYPE.RECOMMENDATION.name }?.mapTo(ArrayList(),
                                            {
                                                Recommendation()
                                                        .addContent(Integer.parseInt(it.id))
                                                        .addImageUrl(it.imageUrl)
                                                        .addTitle(it.name)
                                            }
                                    )
                            )

                            channels.postValue(
                                    it.previewCommonStructures?.filter { it.type == LauncherBasedData.TYPE.CHANNEL.name }?.mapTo(ArrayList(),
                                            {
                                                Channel()
                                                        .addId(it.id)
                                                        .addIconUrl(it.imageUrl)
                                                        .addName(it.name)
                                                        .addActive(it.is_active)
                                            }
                                    )
                            )


                        }, onError = Timber::e
                )
    }


    fun populateApps() {
        Timber.d("Populate app list")
        disposables += database.serverAppDao()
                .all
                .subscribeOn(Schedulers.computation())
                .subscribeBy(
                        onNext = { dbApps ->
                            val recommendations = ArrayList<ServerAppInfo>()
                            dbApps.forEach {
                                Timber.e("12345 got app from db " + it.appName + " package " + it.packageName)

                                val key = it.packageName
                                if (cache.get(key) == null) {
                                    Timber.e("12345 no app cache" + it.packageName)

                                    if (it.baseIcon != null && it.baseIcon.isNotEmpty()) {
                                        decodeFromBase64(it.baseIcon).let { bitmap ->
                                            cache.put(key, bitmap)
                                        }

                                    } else {
                                        val bitmap = BitmapFactory.decodeByteArray(
                                                it.appIcon,
                                                0,
                                                it.appIcon.size
                                        )

                                        if (bitmap != null)
                                            cache.put(key, bitmap)
                                    }
                                }

                                if (cache.get(key) != null)
                                    recommendations.add(ServerAppInfo(it.appName, it.packageName))
                                else {
                                    Timber.e("12345 no app cache2" + it.packageName)
                                }
                            }
                            this.apps.postValue(recommendations)
                        },
                        onError = Timber::e
                )
    }


    fun populatePorts() {
        Timber.d("Populate ports list")
        disposables += database.serverInputsDao()
                .all
                .subscribeOn(Schedulers.computation())
                .subscribeBy(
                        onNext = { inputs ->
                            val newInputs = ArrayList<Input>()
                            inputs.forEach {
                                Timber.e("got input from db " + it.portName + " id = " + it.portNum)
                                val key = it.portName
                                if (cache.get(key) == null) {
                                    decodeFromBase64(it.inputIcon).let { bitmap ->
                                        cache.put(key, bitmap)
                                    }
                                }

                                if (cache.get(key) != null)
                                    newInputs.add(Input(it))
                            }
                            this.inputs.postValue(newInputs.distinct()) //could n't be same values
                        },
                        onError = Timber::e
                )
    }


    fun sendAspectSingleChangeEvent(valueType: AspectMessage.ASPECT_VALUE, value: Int) {
        val builder = AspectMessage.AspectMsgBuilder()
        builder.addValue(valueType, value)
        RxBus.publish(NewAspectEvent(builder.buildAspect()))
    }


    fun launchChannel(channel: Channel) {
        RxBus.publish(LaunchChannelEvent(channel))
    }


    fun launchRecommendation(recommendation: Recommendation) {
        RxBus.publish(LaunchRecommendationEvent(recommendation))
    }


    fun requestAspect() = RxBus.publish(RequestAspectEvent())


    fun requestAllPreviews() = RxBus.publish(RequestInitialPreviewEvent())


    fun launchApp(name: String) {
        RxBus.publish(LaunchAppEvent(name))
        RxBus.publish(NavigateToRemoteEvent())
    }

    fun goSearch() = router.navigateTo(Screens.DEVICE_SEARCH_FRAGMENT)

    fun godo(data: Recommendation) = router.navigateTo(Screens.RECENT_DEVICE_FRAGMENT, data)

    fun startUPnPController() = uPnPManager.controller.resume()


}

