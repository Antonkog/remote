package com.kivi.remote.presentation.home.recommendations

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import com.kivi.remote.Screens
import com.kivi.remote.bus.*
import com.kivi.remote.common.Constants
import com.kivi.remote.common.KiviCache
import com.kivi.remote.common.RxBus
import com.kivi.remote.common.extensions.boolean
import com.kivi.remote.common.extensions.int
import com.kivi.remote.common.extensions.string
import com.kivi.remote.net.model.*
import com.kivi.remote.persistence.AppDatabase
import com.kivi.remote.presentation.base.BaseViewModel
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import ru.terrakok.cicerone.Router
import timber.log.Timber

class RecommendationsViewModel(private val router: Router,
                               val database: AppDatabase,
                               val cache: KiviCache,
                               preferences: SharedPreferences) : BaseViewModel() {

    var lastNsdHolderName by preferences.string(Constants.UNIDENTIFIED, key = Constants.LAST_NSD_HOLDER_NAME)

    var updateAsked by preferences.boolean(false, key = Constants.UPDATE_SHOWING)
    var ratingAsked by preferences.boolean(false,key = Constants.RATING_ASKED)

    var launchCount by preferences.int(0,key = Constants.LAUNCH_COUNTER)
    var reconnectCount by preferences.int(0,key = Constants.CONNECTION_LOST_COUNTER)

    var aspectTryCounter = Constants.ASPECT_GET_TRY
    var lastPortId = Constants.INPUT_HOME_ID

    val recommendations = MutableLiveData<List<Comparable<Recommendation>>>()
    val apps = MutableLiveData<List<Comparable<ServerAppInfo>>>()
    val inputs = MutableLiveData<List<Comparable<Input>>>()
    val channels = MutableLiveData<List<Comparable<Channel>>>()

    val oldVersionTv = MutableLiveData<Boolean>()
    val showRatingDialog = MutableLiveData<Boolean>()

    init {

        if(launchCount > 10 && launchCount % 10  == 0 && reconnectCount/launchCount < 0.3 && !ratingAsked){
            showRatingDialog.postValue(true)
        }


        disposables += RxBus.listen(GotAspectEvent::class.java).subscribeBy(
                onNext = {
                    if (it.msg?.serverVersionCode ?: Int.MAX_VALUE < Constants.VER_FOR_REMOTE_2) //on mstar always old remote version on realtek on older server versions only.
                    {
                        if (!updateAsked)
                            oldVersionTv.postValue(true)
                        else {
                            Timber.d("user restricted version update")
                        }
                    } else {
                        oldVersionTv.postValue(false)
                    }
                }, onError = Timber::e
        )

//        if(PreferencesManager.getShowUpdate())
    }

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

    fun launchApp(name: String) {
        if (name != Constants.MEDIA_SHARE_TXT_ID) {
            RxBus.publish(LaunchAppEvent(name))
            RxBus.publish(NavigateToRemoteEvent())
        } else {
            //  router.navigateTo(Screens.MEDIA_FRAGMENT) todo next version
        }
    }

    fun navigateTo(screen: String) {
        router.navigateTo(screen)
    }


    fun goSearch() = router.navigateTo(Screens.DEVICE_SEARCH_FRAGMENT)

    fun godo(data: Recommendation) = router.navigateTo(Screens.RECENT_DEVICE_FRAGMENT, data)

    fun sendToRemoteApp(context: Context, toOldRemote : Boolean) {
        val appPackageName =  if(toOldRemote) "com.wezom.kiviremote" else "com.kivi.remote"
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$appPackageName")))
        } catch (anfe: android.content.ActivityNotFoundException) {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")))
        }
    }

}

