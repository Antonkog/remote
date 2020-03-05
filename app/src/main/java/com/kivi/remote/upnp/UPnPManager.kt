package com.kivi.remote.upnp

import android.content.SharedPreferences
import androidx.lifecycle.MutableLiveData
import com.kivi.remote.bus.TriggerRebirthEvent
import com.kivi.remote.common.Constants.CURRENT_CONNECTION_IP_KEY
import com.kivi.remote.common.ImageInfo
import com.kivi.remote.common.RxBus
import com.kivi.remote.common.VideoInfo
import com.kivi.remote.common.extensions.string
import com.kivi.remote.presentation.home.gallery.GalleryFragment
import com.kivi.remote.services.NotificationService
import com.kivi.remote.upnp.org.droidupnp.controller.upnp.IUPnPServiceController
import com.kivi.remote.upnp.org.droidupnp.model.upnp.*
import com.kivi.remote.upnp.org.droidupnp.model.upnp.didl.IDIDLItem
import com.kivi.remote.upnp.org.droidupnp.view.DIDLObjectDisplay
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import org.fourthline.cling.model.meta.RemoteDeviceIdentity
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

private typealias MediaType = GalleryFragment.MediaType

class UPnPManager @Inject constructor(
    val controller: IUPnPServiceController,
    val factory: IFactory,
    preferences: SharedPreferences
) : Observable(),
    IDeviceDiscoveryObserver, Observer {

    val currentImageContent: ArrayList<DIDLObjectDisplay> = arrayListOf()
    val currentVideoContent: ArrayList<DIDLObjectDisplay> = arrayListOf()

    lateinit var potentialImageContent: ArrayList<DIDLObjectDisplay>
    lateinit var potentialVideoContent: ArrayList<DIDLObjectDisplay>

    lateinit var currentImageContentDirectories: ArrayList<DIDLObjectDisplay>
    lateinit var currentVideoContentDirectories: ArrayList<DIDLObjectDisplay>

    lateinit var contentCallback: ContentCallback

    var currentDir: String = ""
    var currentDirType: String? = null
    var currentMediaType = GalleryFragment.MediaType.IMAGE
    var nextItem: SlidingContentModel? = null
    var previousItem: SlidingContentModel? = null

    val currentContentState = MutableLiveData<SlidingContentModel>()
    val slideshowState = MutableLiveData<SlideshowProgress>()
    val notificationActionObservable = MutableLiveData<NotificationService.NotificationAction>()
    val flowSubject: Subject<Boolean> = PublishSubject.create<Boolean>()

    private var currentConnectionIp: String by preferences.string("", CURRENT_CONNECTION_IP_KEY)
    private var currentProgress = 0
    private var currentPosition: Int = 0
    private var rendererCommand: IRendererCommand? = null
    private var rendererState: ARendererState? = null
    private var currentItem: IDIDLItem? = null
    private var timerDisposable: Disposable? = null
    private var previousMediaType: MediaType? = null
    private var imageThumbnails: Set<ImageInfo>? = null
    private var videoThumbnails: Set<VideoInfo>? = null

    private val browseCommand: IContentDirectoryCommand? by lazy { factory.createContentDirectoryCommand() }

//    typealias ContentCallback = () -> {}

    private fun String.cleanIP() = this.substringBeforeLast(":").substringAfterLast("/")

    data class SlideshowProgress(val pauseState: Boolean, val progress: Int, val terminate: Boolean)
    data class SlidingContentModel(
        val item: IDIDLItem,
        val _title: String,
        val uri: String,
        val position: Int,
        val type: MediaType
    ) {
        val title
        get() = _title
    }

    data class RendererModel(
        val durationRemaining: String?,
        val durationElapse: String?,
        val progress: Int,
        val title: String?,
        val artist: String?,
        val state: IRendererState.State?
    )

    fun launchItem(
        item: IDIDLItem, position: Int, type: MediaType,
        imageThumbnails: Set<ImageInfo>? = this.imageThumbnails,
        videoThumbnails: Set<VideoInfo>? = this.videoThumbnails, isUserAction: Boolean = false
    ) {
        this.imageThumbnails = imageThumbnails ?: this.imageThumbnails
        this.videoThumbnails = videoThumbnails ?: this.videoThumbnails

        if (isUserAction)
            when (type) {
                GalleryFragment.MediaType.IMAGE -> {
                    currentImageContent.run {
                        clear()
                        addAll(potentialImageContent)
                    }
                }

                GalleryFragment.MediaType.VIDEO -> {
                    currentVideoContent.run {
                        clear()
                        addAll(potentialVideoContent)
                    }
                }
            }

        this.currentPosition = position
        previousMediaType = currentMediaType

        if (previousMediaType != type) {
            killTimer()
        }

        currentProgress = 0
        currentItem = item
        currentMediaType = type
        rendererState = factory.createRendererState()
        rendererState?.addObserver { _, _ ->
            val durationRemaining = rendererState?.remainingDuration
            val durationElapse = rendererState?.position
            val progress = rendererState?.elapsedPercent
            val title = rendererState?.title
            val artist = rendererState?.artist
            val state = rendererState?.state

            setChanged()
            notifyObservers(
                RendererModel(
                    durationRemaining,
                    durationElapse,
                    progress ?: 0,
                    title,
                    artist,
                    state
                )
            )
        }
        rendererCommand?.run {
            commandStop()
            pause()
        }
        rendererCommand = factory.createRendererCommand(rendererState)
        rendererCommand?.run {
            resume()
            updateFull()
            launchItem(item)
        }

        currentContentState.postValue(prepareItem(position, type))
        if (type == GalleryFragment.MediaType.IMAGE)
            slideshowState.postValue(SlideshowProgress(true, currentProgress, false))

        nextItem = prepareItem(position + 1, type)
        previousItem = prepareItem(position - 1, type)
        flowSubject.onNext(false)
    }

    private fun prepareItem(position: Int, mediaType: MediaType): SlidingContentModel? =
        when (mediaType) {
            GalleryFragment.MediaType.IMAGE -> {
                getImageItem(position)
            }
            GalleryFragment.MediaType.VIDEO -> {
                getVideoItem(position)
            }
        }

    private fun getImageItem(position: Int): SlidingContentModel? {
        return if (position > -1 && currentImageContent.size > position) {
            val display = currentImageContent[position]
            val item = display.didlObject as IDIDLItem

            var slidingItem: SlidingContentModel? = null
            imageThumbnails?.let {
                it.forEach {
                    if (item.title == it.title) {
                        slidingItem = SlidingContentModel(
                            item,
                            item.title,
                            it.data,
                            position,
                            MediaType.IMAGE
                        )
                    }
                }
            }
            slidingItem
        } else null
    }

    private fun getVideoItem(position: Int): SlidingContentModel? {
        return if (position > -1 && currentVideoContent.size > position) {
            val display = currentVideoContent[position]
            val item = display.didlObject as IDIDLItem
            var slidingItem: SlidingContentModel? = null

            videoThumbnails?.let {
                it.forEach({
                    if (item.title == it.title) {
                        slidingItem = SlidingContentModel(
                            item,
                            item.title,
                            it.data,
                            position,
                            MediaType.VIDEO
                        )
                    }
                })
            }
            slidingItem
        } else
            null
    }

    fun renderCurrentItem() = currentItem?.let { launchItem(it, currentPosition, currentMediaType) }

    fun progressTo(progress: Int, max: Int) {
        fun formatTime(h: Long, m: Long, s: Long): String {
            return ((if (h >= 10) "" + h else "0" + h) + ":" + (if (m >= 10) "" + m else "0" + m) + ":"
                    + if (s >= 10) "" + s else "0" + s)
        }

        rendererState?.run {
            val t = ((1.0 - (max.toDouble() - progress) / max) * durationSeconds).toLong()
            val h = t / 3600
            val m = (t - h * 3600) / 60
            val s = t - h * 3600 - m * 60
            val seek = formatTime(h, m, s)
            rendererCommand?.run {
                Timber.d("Seek to " + seek)
                commandSeek(seek)
            }
        }
    }

    fun pause() = rendererCommand?.commandPause()

    fun stop() {
        rendererCommand?.commandStop()
        killTimer()
        flowSubject.onNext(true)
    }

    fun resume() {
        rendererCommand?.commandPlay()
        flowSubject.onNext(false)
    }

    override fun addedDevice(device: IUpnpDevice) {
        Timber.d("Found UPnP device \"${device.displayString}\" with IP: ${(device.device.identity as? RemoteDeviceIdentity)?.descriptorURL}")

        if (device.displayString == KIVI_RENDERER_DISPLAY_NAME && isKiviRendererIP(device)) {
            Timber.d("Found KIVI renderer")
            controller.selectedRenderer = device
        }

        if (device.displayString == KODI_RENDERER_DISPLAY_NAME && isKiviRendererIP(device)) {
            Timber.d("Found KODI renderer")
            controller.selectedRenderer = device
        }

        if (device.displayString != null && device.serialNumber != null) {
            if ((device.displayString == KIVI_CONTENT_DIRECTORY_DISPLAY_NAME
                        || device.displayString == KIVI_DEBUG_CONTENT_DIRECTORY_DISPLAY_NAME)
                && device.serialNumber == controller.serviceListener.generatedSerial) {
                Timber.d("Found KIVI Remote content directory")
                if (controller.selectedContentDirectory == null || controller.selectedContentDirectory != device)
                    controller.selectedContentDirectory = device
                browseHome()
            }
        }
    }

    private fun isKiviRendererIP(device: IUpnpDevice): Boolean {
        val rendererIP =
            (device.device.identity as? RemoteDeviceIdentity)?.descriptorURL.toString().cleanIP()
        return currentConnectionIp == rendererIP
    }

    override fun removedDevice(device: IUpnpDevice?) {}

    override fun update(o: Observable?, arg: Any?) {}

    private fun browseHome() {
        browseTo("0")
    }

    fun browseTo(id: String, title: String?) {
        browseTo(id)
        title?.let { currentDirType = it }
    }

    private fun browseTo(id: String) {
        browseCommand?.browse(id, null, contentCallback) ?: RxBus.publish(TriggerRebirthEvent())
    }

    fun addObservers() = controller.run {
        rendererDiscovery.addObserver(this@UPnPManager)
        contentDirectoryDiscovery.addObserver(this@UPnPManager)
        addSelectedContentDirectoryObserver(this@UPnPManager)
    }

    fun removeObservers() = controller.run {
        rendererDiscovery.removeObserver(this@UPnPManager)
        contentDirectoryDiscovery.removeObserver(this@UPnPManager)
        delSelectedContentDirectoryObserver(this@UPnPManager)
    }

    fun startSlideshowTimer() {
        timerDisposable?.takeIf { !it.isDisposed }?.dispose()
        slideshowState.postValue(SlideshowProgress(false, currentProgress, false))
        timerDisposable = io.reactivex.Observable.interval(1, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ _ ->
                // todo extract this to integer resources
                if (++currentProgress <= 7) {
                    if (nextItem == null) {
                        killTimer()
                    } else
                        slideshowState.postValue(SlideshowProgress(false, currentProgress, false))
                } else {
                    nextItem?.let { playNextItem() } ?: killTimer()
                }
            }) { e -> Timber.e(e, e.message) }
    }

    private fun playNextItem() {
        currentProgress = 0
        slideshowState.postValue(SlideshowProgress(false, currentProgress, false))
        notificationActionObservable.postValue(NotificationService.NotificationAction.NEXT)
    }

    private fun killTimer() {
        currentProgress = 0
        slideshowState.postValue(SlideshowProgress(false, 0, true))
        timerDisposable?.takeIf { !it.isDisposed }?.dispose()
    }

    fun pauseSlideshowTimer() {
        timerDisposable?.takeIf { !it.isDisposed }?.dispose()
        slideshowState.postValue(SlideshowProgress(true, currentProgress, false))
    }

    companion object {
        const val KIVI_RENDERER_DISPLAY_NAME = "FunTV Fun TV MediaRenderer 1"
        const val KIVI_CONTENT_DIRECTORY_DISPLAY_NAME = "KIVI Remote"
        const val KIVI_DEBUG_CONTENT_DIRECTORY_DISPLAY_NAME = "KIVI Remote Debug"
        // For testing purposes, adjust to your renderer name
        const val KODI_RENDERER_DISPLAY_NAME = "XBMC Foundation Kodi 17.2 Git:20170523-4f53fb5"
    }
}