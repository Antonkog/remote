package com.wezom.kiviremote.presentation.home

import android.arch.lifecycle.MutableLiveData
import android.content.SharedPreferences
import com.wezom.kiviremote.Screens
import com.wezom.kiviremote.Screens.DEVICE_SEARCH_FRAGMENT
import com.wezom.kiviremote.bus.*
import com.wezom.kiviremote.common.Action
import com.wezom.kiviremote.common.Constants.*
import com.wezom.kiviremote.common.RxBus
import com.wezom.kiviremote.common.extensions.boolean
import com.wezom.kiviremote.common.extensions.string
import com.wezom.kiviremote.net.ChatConnection
import com.wezom.kiviremote.net.model.ConnectionMessage
import com.wezom.kiviremote.net.model.SocketConnectionModel
import com.wezom.kiviremote.nsd.NsdServiceModel
import com.wezom.kiviremote.persistence.AppDatabase
import com.wezom.kiviremote.persistence.model.RecentDevice
import com.wezom.kiviremote.persistence.model.ServerApp
import com.wezom.kiviremote.presentation.base.BaseViewModel
import com.wezom.kiviremote.presentation.home.gallery.GalleryFragment
import com.wezom.kiviremote.presentation.home.touchpad.TouchpadButtonClickEvent
import com.wezom.kiviremote.upnp.UPnPManager
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch
import ru.terrakok.cicerone.Navigator
import ru.terrakok.cicerone.NavigatorHolder
import ru.terrakok.cicerone.Router
import timber.log.Timber
import java.util.Observer
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList


class HomeActivityViewModel(
    private val database: AppDatabase,
    private val navigatorHolder: NavigatorHolder,
    private val router: Router,
    private val uPnPManager: UPnPManager, preferences: SharedPreferences
) : BaseViewModel() {

    init {
        disposables += RxBus.listen(ConnectionMessage::class.java)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(onNext = {
                launch(CommonPool) {
                    it.appList?.let {
                        database.serverAppDao().run {
                            removeAll()
                            insertAll(it.mapTo(ArrayList(), {
                                ServerApp().apply {
                                    appName = it.applicationName
                                    packageName = it.packageName
                                    appIcon = it.appIcon
                                }
                            }))
                        }
                    }
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

                if (it.volume != -1) {
                    RxBus.publish(NewVolumeEvent(it.volume))
                    muteStatus = it.volume <= 0
                }
            }, onError = Timber::e)

        disposables += RxBus.listen(TriggerRebirthEvent::class.java)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(onNext = {
                triggerRebirth.postValue(true)
            }, onError = Timber::e)

        disposables += RxBus.listen(SendCursorCoordinatesEvent::class.java)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(onNext = {
                sendTouchpadAction(it.x, it.y, Action.motion)
            }, onError = Timber::e)

        disposables += RxBus.listen(TouchpadButtonClickEvent::class.java)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(onNext = {
                sendTouchpadAction(it.x, it.y, it.action)
            }, onError = Timber::e)

        disposables += RxBus.listen(SendTextEvent::class.java)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(onNext = {
                sendText(it.text)
            }, onError = Timber::e)

        disposables += RxBus.listen(SendKeyEvent::class.java)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(onNext = {
                sendKey(it.keyEvent)
            }, onError = Timber::e)

        disposables += RxBus.listen(SendScrollEvent::class.java)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(onNext = {
                serverConnection?.sendMessage(SocketConnectionModel().apply {
                    setMotion(ArrayList<Double>().apply {
                        add(0.0)
                        add(it.y)
                    })
                    setAction(Action.SCROLL)
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

        disposables += RxBus.listen(LaunchAppEvent::class.java)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(onNext = {
                launchApp(it.packageName)
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

    private val progressObserver = Observer { _, arg ->
        val model = arg as UPnPManager.RendererModel
        progress.postValue(ProgressModel(model, uPnPManager.currentMediaType))
    }

    data class ProgressModel(
        val rendererModel: UPnPManager.RendererModel,
        val currentMediaType: GalleryFragment.MediaType
    )

    private fun initConnection(nsdModel: NsdServiceModel, firstConnection: Boolean) {
        killPing()
        serverConnection = ChatConnection()
        connect(nsdModel)
    }

    private fun killPing() {
        serverConnection?.dispose()
    }

    private fun connect(nsdModel: NsdServiceModel) {
        val currentConnectionName = currentConnection
        if (nsdModel.name != currentConnectionName) {
            launch(CommonPool) {
                database.serverAppDao().removeAll()
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
                    launch(CommonPool) {
                        database.recentDeviceDao().insert(RecentDevice(nsdModel.name, null))
                    }
                }
            },
            onError = Timber::e
        )
    }

    fun reconnect() {
        Timber.d("Reconnecting")
        reconnectTimer?.takeUnless { it.isDisposed }?.dispose()
        reconnectTimer = Single.timer(1, TimeUnit.SECONDS).subscribeBy(
            onSuccess = {
                currentModel?.let { initConnection(it, false) }
            }, onError = { Timber.e(it, "Couldn't establish connection: ${it.message}") }
        )
    }

    private fun sendTouchpadAction(x: Double, y: Double, actionType: Action) =
        serverConnection?.sendMessage(SocketConnectionModel().apply {
            setAction(actionType)
            setMotion(ArrayList<Double>().apply {
                add(x)
                add(y)
            })
        })


    private fun sendText(text: String) {
        serverConnection?.sendMessage(SocketConnectionModel().apply {
            setArgs(ArrayList<String>().apply {
                add(text)
            })
            setAction(Action.text)
        })
    }

    private fun sendKey(key: Int) {
        serverConnection?.sendMessage(SocketConnectionModel().apply {
            setArgs(ArrayList<String>().apply {
                add(key.toString())
            })
            setAction(Action.keyevent)
        })
    }

    private fun sendAction(action: Action) {
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

    fun startUPnPController() = uPnPManager.controller.resume()

    fun stopUPnPController() {
        uPnPManager.controller.pause()
        uPnPManager.controller.serviceListener.serviceConnexion.onServiceDisconnected(null)
    }

    fun backHome() {
        router.backTo(Screens.DEVICE_SEARCH_FRAGMENT)
    }

    fun progressTo(progress: Int, max: Int) = uPnPManager.progressTo(progress, max)

    fun observeProgress() = uPnPManager.addObserver(progressObserver)

    fun removeProgressObserver() = uPnPManager.deleteObserver(progressObserver)

    fun pausePlayback() = uPnPManager.pause()

    fun resumePlayback() = uPnPManager.resume()

    fun renderCurrentItem() = uPnPManager.renderCurrentItem()

    fun getImageContentSize() = uPnPManager.currentImageContent.size

    fun getVideoContentSize() = uPnPManager.currentVideoContent.size

    fun getCurrentContentObservable() = uPnPManager.currentContentState

    fun getSlideshowStateObservable() = uPnPManager.slideshowState

    fun getFlowSubject() = uPnPManager.flowSubject

    fun stopPlayback() = uPnPManager.stop()
}