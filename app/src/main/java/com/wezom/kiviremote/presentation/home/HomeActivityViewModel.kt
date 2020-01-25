package com.wezom.kiviremote.presentation.home

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import androidx.lifecycle.MutableLiveData
import com.wezom.kiviremote.App
import com.wezom.kiviremote.Screens.DEVICE_SEARCH_FRAGMENT
import com.wezom.kiviremote.bus.*
import com.wezom.kiviremote.common.*
import com.wezom.kiviremote.common.Constants.*
import com.wezom.kiviremote.common.extensions.Run
import com.wezom.kiviremote.common.extensions.boolean
import com.wezom.kiviremote.common.extensions.string
import com.wezom.kiviremote.net.ChatConnection
import com.wezom.kiviremote.net.model.*
import com.wezom.kiviremote.nsd.NsdServiceModel
import com.wezom.kiviremote.persistence.AppDatabase
import com.wezom.kiviremote.persistence.dao.ServerAppsDao
import com.wezom.kiviremote.persistence.model.ServerApp
import com.wezom.kiviremote.persistence.model.ServerChannel
import com.wezom.kiviremote.persistence.model.ServerRecommendation
import com.wezom.kiviremote.presentation.base.BaseViewModel
import com.wezom.kiviremote.presentation.home.gallery.GalleryFragment
import com.wezom.kiviremote.presentation.home.touchpad.TouchpadButtonClickEvent
import com.wezom.kiviremote.presentation.home.tvsettings.AspectHolder
import com.wezom.kiviremote.presentation.home.tvsettings.LastVolume
import com.wezom.kiviremote.upnp.UPnPManager
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import ru.terrakok.cicerone.Navigator
import ru.terrakok.cicerone.NavigatorHolder
import ru.terrakok.cicerone.Router
import timber.log.Timber
import java.util.concurrent.TimeUnit


class HomeActivityViewModel(
        private val database: AppDatabase,
        private val navigatorHolder: NavigatorHolder,
        private val cache: KiviCache,
        private val router: Router,
        preferences: SharedPreferences
) : BaseViewModel() {
    var autoConnect by preferences.boolean(false, Constants.AUTO_CONNECT)

    init {
        disposables += RxBus.listen(ConnectionMessage::class.java)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(onNext = {

                    if (it.aspectMessage != null && it.available != null) {
                        AspectHolder.setAspectValues(it.aspectMessage, it.available, it.initialMessage)

                        // no massage recieved in this session, check AspectHolder/set/clean.
                        if (AspectHolder.initialMsg == null)
                            if (AspectHolder.message?.serverVersionCode ?: 0 >= Constants.VER_ASPECT_XIX) {
                                Run.after(1000) {
                                    RxBus.publish(RequestInitialEvent())
                                }
                            }
                        RxBus.publish(GotAspectEvent(it.aspectMessage, it.available, it.initialMessage
                                ?: AspectHolder.initialMsg))
                    }

                    if (it.isShowKeyboard) {
                        RxBus.publish(ShowKeyboardEvent())
                    }

                    if (it.isHideKeyboard) {
                        RxBus.publish(HideKeyboardEvent())
                    }

                    if (!it.isSetKeyboard) {
                        showSettingsDialog.postValue(true)
                    }

                    if (it.isDisconnect) {
                        disconnect()
                    }

                    if (it.volume != NO_VALUE) {
                        RxBus.publish(NewVolumeEvent(it.volume))
                        muteStatus = it.volume <= 0
                    }
                }, onError = Timber::e)

        disposables += RxBus.listen(GotPreviewsInitialEvent::class.java)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(onNext = { initialEvent ->
                    if (initialEvent.previewCommonStructures != null) {
                        GlobalScope.launch(Dispatchers.Default) {
                            async {
                                val apps = getApps(initialEvent)

                                database.serverAppDao().run {
                                    removeAll()
//                                    addMediaSharing()
                                    insertAll(apps)
                                }
                                val ids = arrayListOf<String>()
                                apps.forEach { app ->
                                    ids.add(app.packageName)
                                }
                                if (ids.isNotEmpty()) RxBus.publish(RequestImgByIds(ids))
                            }.await()

                            async {
                                val inputs = getAppInputs(initialEvent)
                                database.serverInputsDao().run {
                                    removeAll()
                                    insertAll(inputs)
                                }
                            }.await()



                            database.chennelsDao().run {
                                removeAll()
                                insertAll(
                                        initialEvent.previewCommonStructures.filter { it.type == LauncherBasedData.TYPE.CHANNEL.name }.mapTo(ArrayList()) {
                                            ServerChannel().apply {
                                                serverId = it.id
                                                name = it.name
                                                is_active = it.is_active
                                                imageUrl = it.imageUrl
                                                sort = it.additionalData?.entries?.firstOrNull { it1 -> it1.key == "sort" }?.value
                                                edited_at = it.additionalData?.entries?.firstOrNull { it2 -> it2.key == "edited_at" }?.value
                                                has_timeshift = it.additionalData?.entries?.firstOrNull { it3 -> it3.key == "has_timeshift" }?.value
                                            }
                                        })
                            }

                            database.recommendationsDao().run {
                                removeAll()
                                insertAll(
                                        initialEvent.previewCommonStructures.filter { it.type == LauncherBasedData.TYPE.RECOMMENDATION.name }.mapTo(ArrayList()) {
                                            ServerRecommendation().apply {
                                                contentID = it.id
                                                favourite = false
                                                title = it.name
                                                imageUrl = it.imageUrl
                                                kind = it.additionalData?.entries?.firstOrNull { it1 -> it1.key == "kind" }?.value
                                                monetizationType = it.additionalData?.entries?.firstOrNull { it2 -> it2.key == "monetizationType" }?.value
                                                imdb = it.additionalData?.entries?.firstOrNull { it3 -> it3.key == "imdb" }?.value
                                            }
                                        })
                            }
                        }
                    }
                })


        disposables += RxBus.listen(GotPreviewsContentEvent::class.java)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(onNext = { previewsContents ->
                    previewsContents.previewContents.forEach {
                        Timber.e(" got preview for ${it.id} ")
                        updateAppIcon(it)
                    }
                })

        disposables += RxBus.listen(NewAppListEvent::class.java)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(onNext = {
                    if (it.appInfo != null) {
                        GlobalScope.launch(Dispatchers.Default) {
                            it.appInfo?.let {
                                database.serverAppDao().run {
                                    removeAll()
                                    insertAll(it.mapTo(ArrayList(), {
                                        ServerApp().apply {
                                            appName = it.applicationName
                                            packageName = it.packageName
                                            appIcon = it.appIcon
                                            baseIcon = it.baseIcon
                                        }
                                    }))
                                }
                            }
                        }
//                        RxBus.publish(GotAppsEvent(it.appList))
                        Timber.e("12345 got appList")
                    }
                }, onError = Timber::e)

        disposables += RxBus.listen(TriggerRebirthEvent::class.java)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(onNext = {
                    triggerRebirth.postValue(true)
                }, onError = Timber::e)

        disposables += RxBus.listen(SendCursorCoordinatesEvent::class.java)
                .observeOn(AndroidSchedulers.mainThread()).debounce(TOUCH_EVENT_FREQUENCY, TimeUnit.MILLISECONDS)
                .subscribeBy(onNext = {
                    sendTouchpadAction(it.x, it.y, Action.MOTION)
                }, onError = Timber::e)

        disposables += RxBus.listen(TouchpadButtonClickEvent::class.java)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(onNext = {
                    sendTouchpadAction(it.x, it.y, it.action)
                }, onError = Timber::e)

        disposables += RxBus.listen(SendVoiceEvent::class.java)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(onNext = {
                    sendArgAction(Action.VOICE_SEARCH, it.text)
                }, onError = Timber::e)


        disposables += RxBus.listen(SetVolumeEvent::class.java)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(onNext = {
                    LastVolume.volumeInt = it.newVolumeToSend
                    sendArgAction(Action.SET_VOLUME, it.newVolumeToSend.toString())
                }, onError = Timber::e)

        disposables += RxBus.listen(SendKeyEvent::class.java)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(onNext = {
                    sendArgAction(Action.KEY_EVENT, it.keyEvent.toString())
                }, onError = Timber::e)

        disposables += RxBus.listen(RequestAspectEvent::class.java)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(onNext = {
                    sendAction(Action.REQUEST_ASPECT)
                }, onError = Timber::e)

        disposables += RxBus.listen(ShowHideAspectEvent::class.java)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(onNext = {
                    sendAction(Action.SHOW_OR_HIDE_ASPECT)
                }, onError = Timber::e)



        disposables += RxBus.listen(RequestInitialEvent::class.java)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(onNext = {
                    sendAction(Action.REQUEST_INITIAL)
                }, onError = Timber::e)


        disposables += RxBus.listen(RequestInitialPreviewEvent::class.java)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(onNext = {
                    sendAction(Action.REQUEST_INITIAL_II)
                }, onError = Timber::e)



        disposables += RxBus.listen(RequestImgByIds::class.java)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(onNext = {
                    serverConnection?.sendMessage(SocketConnectionModel().apply {
                        setAction(Action.REQUEST_IMG_BY_IDS)
                        setArgs(it.ids)
                    })
                }, onError = Timber::e)


        disposables += RxBus.listen(RequestAppsEvent::class.java)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(onNext = {
                    sendAction(Action.REQUEST_APPS)
                }, onError = Timber::e)

        disposables += RxBus.listen(SendScrollEvent::class.java)
                .observeOn(AndroidSchedulers.mainThread()).debounce(SCROLL_EVENT_FREQUENCY, TimeUnit.MILLISECONDS)
                .subscribeBy(onNext = {
                    serverConnection?.sendMessage(SocketConnectionModel().apply {
                        setMotion(ArrayList<Double>().apply {
                            add(0.0)
                            add(it.y)
                        })
                        setAction(it.action)
                    })
                }, onError = Timber::e)

        disposables += RxBus.listen(ConnectEvent::class.java)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(onNext = {
                    initConnection(it.model, true)
                }, onError = Timber::e)

        disposables += RxBus.listen(ReconnectEvent::class.java)
                .subscribeBy(onNext = { currentModel?.let { reconnect() } }, onError = Timber::e)

        disposables += RxBus.listen(KillPingEvent::class.java)
                .subscribeBy(onNext = {
                    killPing()
                }, onError = Timber::e)

        disposables += RxBus.listen(NewNameEvent::class.java)
                .subscribeBy(onNext = {
                    sendArgAction(Action.NAME_CHANGE, it.name)
                }, onError = Timber::e)

        disposables += RxBus.listen(NewAspectEvent::class.java)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(onNext = {
                    sendAspectChanged(it.message)
                }, onError = Timber::e)

        disposables += RxBus.listen(LaunchAppEvent::class.java)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(onNext = {
                    launchApp(it.packageName)
                }, onError = Timber::e)

        disposables += RxBus.listen(LaunchChannelEvent::class.java)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(onNext = {
                    launchChannel(it.channel)
                }, onError = Timber::e)

        disposables += RxBus.listen(LaunchRecommendationEvent::class.java)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(onNext = {
                    launchRecommendation(it.recommendation)
                }, onError = Timber::e)


        disposables += RxBus.listen(RemotePlayerEvent::class.java)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(onNext = {
                    sendPlayerEvent(it)
                }, onError = Timber::e)

        disposables += RxBus.listen(SendActionEvent::class.java)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(onNext = {
                    sendAction(it.action)
                }, onError = Timber::e)

        disposables += RxBus.listen(DisconnectEvent::class.java)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(onNext = {
                }, onError = Timber::e)

    }

    private fun ServerAppsDao.addMediaSharing() {
        insertMediaShareStaticApp(ServerApp().apply {
            appName = MEDIA_SHARE_TXT_ID
            packageName = MEDIA_SHARE_TXT_ID
        })
    }

    fun updateAppIcon(appData: PreviewContent) { //to update saving old name
        Timber.e(" 12345 updateAppIcon:")

        if (appData.id != null && appData.img != null)
            decodeFromBase64(appData.img, 120, 90).let { bitmap ->
                cache.put(appData.id, bitmap)
                Timber.e("12345 caching app:")
            }

//        database.serverAppDao().update(ServerApp().apply {
//            packageName = appData.id
//            baseIcon = appData.img
//        })
    }


    val showSettingsDialog = MutableLiveData<Boolean>()
    val progress = MutableLiveData<ProgressModel>()
    val slidingPanelContent = MutableLiveData<UPnPManager.SlidingContentModel>()
    val triggerRebirth = MutableLiveData<Boolean>()

    private var currentConnection: String by preferences.string(
            UNIDENTIFIED,
            CURRENT_CONNECTION_KEY
    )
    private var currentConnectionIp: String by preferences.string("", CURRENT_CONNECTION_IP_KEY)
    private var muteStatus: Boolean by preferences.boolean(false, MUTE_STATUS_KEY)

    private var serverConnection: ChatConnection? = null
    private var currentModel: NsdServiceModel? = null

    private var reconnectTimer: Disposable? = null

    data class ProgressModel(
            val rendererModel: UPnPManager.RendererModel,
            val currentMediaType: GalleryFragment.MediaType
    )

    fun clearData() {
        GlobalScope.launch(Dispatchers.Default) {
            database.recommendationsDao().removeAll()
        }
    }

    private fun initConnection(nsdModel: NsdServiceModel, firstConnection: Boolean) {
        killPing()
        serverConnection = ChatConnection()
        connect(nsdModel, firstConnection)
    }

    private fun killPing() {
        serverConnection?.dispose()
    }

    private fun connect(nsdModel: NsdServiceModel, firstConnection: Boolean) { //research
        val currentConnectionName = currentConnection
        if (nsdModel.name != currentConnectionName) {
            GlobalScope.launch(Dispatchers.Default) {
                AspectHolder.clean()
            }
        }

        currentConnection = nsdModel.name
        Timber.d("Current connection ip: ${nsdModel.host.hostAddress}, port: ${nsdModel.port}")
        currentConnectionIp = nsdModel.host.hostAddress

        Completable.timer(500, TimeUnit.MILLISECONDS).subscribeBy(
                onComplete = {
                    currentModel = nsdModel
                    serverConnection?.run {
                        connectToServer(nsdModel.host, nsdModel.port)
                        GlobalScope.launch(Dispatchers.Default) {
                            if (firstConnection) {
                                database.recommendationsDao().removeAll()
                                database.serverInputsDao().removeAll()
                                database.chennelsDao().removeAll()
                                database.serverAppDao().removeAll()
                                RxBus.publish(RequestInitialPreviewEvent())
                                RxBus.publish(RemotePlayerEvent(RemotePlayerEvent.PlayerAction.REQUEST_CONTENT, null))
                            }
                        }
                    }
                },
                onError = Timber::e
        )
    }

    fun reconnect() {
        Timber.d("Reconnecting")
        reconnectTimer?.takeUnless { it.isDisposed }?.dispose()
        reconnectTimer = Single.timer(Constants.DELAY_RECONNECT, TimeUnit.SECONDS).subscribeBy(
                onSuccess = {
                    currentModel?.let { initConnection(it, false) }
                }, onError = { Timber.e(it, "Couldn't establish connection: ${it.message}") }
        )
    }

    fun sendTextToTv(msg: String) {
        sendArgAction(Action.TEXT, msg)
    }

    private fun sendTouchpadAction(x: Double, y: Double, actionType: Action) =
            serverConnection?.sendMessage(SocketConnectionModel().apply {
                setAction(actionType)
                setMotion(ArrayList<Double>().apply {
                    add(x)
                    add(y)
                })
            })


    private fun sendArgAction(action: Action, argText: String) {
        serverConnection?.sendMessage(SocketConnectionModel().apply {
            setArgs(ArrayList<String>().apply {
                add(argText)
            })
            setAction(action)
        })
    }


    private fun sendAspectChanged(msg: AspectMessage) {
        serverConnection?.sendMessage(SocketConnectionModel().apply {
            setAspectMessage(msg)
        })
    }

    private fun launchChannel(msg: Channel) {
        serverConnection?.launchChannel(msg)
    }

    private fun launchRecommendation(msg: Recommendation) {
        serverConnection?.launchRecommendation(msg)
    }

    private fun sendPlayerEvent(msg: RemotePlayerEvent) {
        serverConnection?.synchronizePlayerToTV(msg)
    }


    private fun sendAction(action: Action) {
        Timber.e("12345  send Action " + action.name)
        serverConnection?.sendMessage(SocketConnectionModel().apply {
            setAction(action)
        })
    }

    private fun launchApp(packageName: String) {
        serverConnection?.sendMessage(SocketConnectionModel().apply {
            setAction(Action.LAUNCH_APP)
            setPackageName(packageName)
        })
    }

    private fun disconnect() {
        router.backTo(DEVICE_SEARCH_FRAGMENT)
    }

    fun tearConnectionDown() {
        serverConnection?.tearDown()
    }

    fun openSettings() {
        sendAction(Action.OPEN_SETTINGS)
    }

    fun setNavigator(navigator: Navigator) = navigatorHolder.setNavigator(navigator)

    fun removeNavigator() = navigatorHolder.removeNavigator()

    fun newRootScreen(screenKey: String) = router.newRootScreen(screenKey)

    fun restartColorScheme(ctx: Activity?) {
        if (ctx != null) {
            PreferencesManager.setDarkMode(!App.isDarkMode())
            val i = ctx.intent
            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            i.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
            i.putExtra(Constants.BUNDLE_REALUNCH_KEY, true)
            ctx.finish()
            ctx.startActivity(i)
        }
    }

    fun goTo(screenKey: String) {
        router.navigateTo(screenKey);
    }
}