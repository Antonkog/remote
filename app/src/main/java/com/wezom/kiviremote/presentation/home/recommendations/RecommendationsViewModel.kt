package com.wezom.kiviremote.presentation.home.recommendations

import android.arch.lifecycle.MutableLiveData
import android.content.SharedPreferences
import com.wezom.kiviremote.Screens
import com.wezom.kiviremote.bus.*
import com.wezom.kiviremote.common.Action
import com.wezom.kiviremote.common.Constants
import com.wezom.kiviremote.common.KiviCache
import com.wezom.kiviremote.common.RxBus
import com.wezom.kiviremote.common.extensions.string
import com.wezom.kiviremote.net.model.*
import com.wezom.kiviremote.persistence.AppDatabase
import com.wezom.kiviremote.presentation.base.BaseViewModel
import com.wezom.kiviremote.upnp.UPnPManager
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import ru.terrakok.cicerone.Router
import timber.log.Timber

class RecommendationsViewModel(private val router: Router,
                               val database: AppDatabase,
                               val cache: KiviCache,
                               preferences: SharedPreferences,
                               private val uPnPManager: UPnPManager) : BaseViewModel() {

    var lastNsdHolderName by preferences.string(Constants.UNIDENTIFIED, key = Constants.LAST_NSD_HOLDER_NAME)

    var aspectTryCounter = Constants.ASPECT_GET_TRY
    var lastPortId = Constants.INPUT_HOME_ID

    val recommendations = MutableLiveData<List<Comparable<Recommendation>>>()
    val apps = MutableLiveData<List<Comparable<ServerAppInfo>>>()
    val inputs = MutableLiveData<List<Comparable<Input>>>()
    val channels = MutableLiveData<List<Comparable<Channel>>>()

    fun requestApps() = RxBus.publish(SendActionEvent(Action.REQUEST_APPS))
    fun requestInputs() = RxBus.publish(SendActionEvent(Action.REQUEST_INPUTS))
    fun requestRecommendations() = RxBus.publish(SendActionEvent(Action.REQUEST_RECOMMENDATIONS))
    fun requestChannels() = RxBus.publish(SendActionEvent(Action.REQUEST_CHANNELS))

    fun populateChannels() {
        disposables += database.chennelsDao()
                .all
                .subscribeOn(Schedulers.computation())
                .subscribeBy(
                        onNext = { dbChannels ->
                            val channels = ArrayList<Channel>()
                            dbChannels.forEach {
                                //                                Timber.d("12345 Populate channel  " + it.name)
                                channels.add(Channel()
                                        .addId(it.serverId)
                                        .addActive(it.is_active)
                                        .addIconUrl(it.imageUrl)
                                        .addSort(it.sort)
                                        .addEdited(it.edited_at)
                                        .addName(it.name)
                                )
                            }
//                            this.inputs.postValue(newInputs.distinct()) //could n't be same values
                            this.channels.postValue(channels)
                        }, onError = {
                    Timber.e(it.message)
                }
                )
    }

    fun populateRecommendations() {
        disposables += database.recommendationsDao()
                .all
                .subscribeOn(Schedulers.computation())
                .subscribeBy(
                        onNext = { dbRecs ->
                            val recommendations = ArrayList<Recommendation>()
                            dbRecs.forEach {
                                recommendations.add(Recommendation()
                                        .addContentId(it.contentID)
                                        .addImageUrl(it.imageUrl)
                                        .setImdb(it.imdb)
                                        .addKind(it.kind)
                                        .addDiscription(it.description)
                                        .addTitle(it.title)
                                        .addSubtitle(it.subTitle)
                                )
                            }
                            this.recommendations.postValue(recommendations)
                        }, onError = {
                    Timber.e(it.message)
                }
                )
    }


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


    fun populatePorts() {
        Timber.d("Populate ports list")
        disposables += database.serverInputsDao()
                .all
                .subscribeOn(Schedulers.computation())
                .subscribeBy(
                        onNext = { inputs ->
                            val newInputs = ArrayList<Input>()
                            inputs.forEach {
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

    fun requestImgByIds(ids : List<String>) = RxBus.publish(RequestImgByIds(ids))


    fun launchApp(name: String) {
        if (name != Constants.MEDIA_SHARE_TXT_ID) {
            RxBus.publish(LaunchAppEvent(name))
            RxBus.publish(NavigateToRemoteEvent())
        } else {
            //  router.navigateTo(Screens.MEDIA_FRAGMENT) todo next version
        }
    }

    fun goDeep(screen: String) {
        router.navigateTo(screen)
    }

    fun goSearch() = router.navigateTo(Screens.DEVICE_SEARCH_FRAGMENT)

    fun godo(data: Recommendation) = router.navigateTo(Screens.RECENT_DEVICE_FRAGMENT, data)

    fun startUPnPController() = uPnPManager.controller.resume()


}

