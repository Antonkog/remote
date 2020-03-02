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
import com.kivi.remote.common.PreferencesManager
import com.kivi.remote.common.RxBus
import com.kivi.remote.common.extensions.Run
import com.kivi.remote.common.extensions.string
import com.kivi.remote.net.model.*
import com.kivi.remote.persistence.AppDatabase
import com.kivi.remote.presentation.base.BaseViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import ru.terrakok.cicerone.Router
import timber.log.Timber

class RecommendationsViewModel(private val router: Router,
                               val database: AppDatabase,
                               val cache: KiviCache,
                               preferences: SharedPreferences) : BaseViewModel() {

    var lastNsdHolderName by preferences.string(Constants.UNIDENTIFIED, key = Constants.LAST_NSD_HOLDER_NAME)

    var aspectTryCounter = Constants.ASPECT_GET_TRY
    var lastPortId = Constants.INPUT_HOME_ID

    val recommendations = MutableLiveData<List<Comparable<Recommendation>>>()
    val apps = MutableLiveData<List<Comparable<ServerAppInfo>>>()
    val inputs = MutableLiveData<List<Comparable<Input>>>()
    val channels = MutableLiveData<List<Comparable<Channel>>>()

    val oldVersionTv = MutableLiveData<Boolean>()

    init {
        disposables += RxBus.listen(GotAspectEvent::class.java).subscribeBy(
                onNext = {
                    if (it?.msg?.serverVersionCode ?: Constants.VER_FOR_REMOTE_2 <  Constants.VER_FOR_REMOTE_2
                            || it?.getManufacture() == Constants.SERV_MSTAR) //on mstar always old remote version on realtek on older server versions only.
                    {
                        if(PreferencesManager.getShowUpdate())
                        oldVersionTv.postValue(true)
                        else{Timber.e("user restricted version update")}
                    } else{
                        oldVersionTv.postValue(false)
                    }
                }, onError = Timber::e
        )
    }

    fun populateChannels() {
        disposables += RxBus.listen(GotPreviewsContentEvent::class.java)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(onNext = { _ ->
                    Run.after(1000) {
                        populateApps() //for review - now using Cache
                    }
                })


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
    fun setndToOldRemote( context: Context) {
        val appPackageName = "com.wezom.kiviremote" //old remote
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$appPackageName")))
        } catch (anfe: android.content.ActivityNotFoundException) {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")))
        }
    }


}

