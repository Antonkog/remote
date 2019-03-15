package com.wezom.kiviremote.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.arch.lifecycle.LifecycleService
import android.arch.lifecycle.Observer
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.os.Build
import android.support.annotation.RequiresApi
import android.support.v4.app.NotificationCompat
import android.widget.RemoteViews
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.NotificationTarget
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.wezom.kiviremote.App
import com.wezom.kiviremote.R
import com.wezom.kiviremote.common.Constants.NOTIFICATION_ID
import com.wezom.kiviremote.presentation.home.HomeActivity
import com.wezom.kiviremote.presentation.home.gallery.GalleryFragment
import com.wezom.kiviremote.upnp.UPnPManager
import com.wezom.kiviremote.upnp.org.droidupnp.model.upnp.IRendererState
import io.reactivex.disposables.Disposable
import timber.log.Timber
import javax.inject.Inject

private typealias MediaType = GalleryFragment.MediaType

class NotificationService : LifecycleService() {

    @Inject
    lateinit var uPnPManager: UPnPManager

    private lateinit var noImagePreviewBitmap: Bitmap
    private lateinit var noVideoPreviewBitmap: Bitmap
    private lateinit var notificationTarget: NotificationTarget

    private var notificationManager: NotificationManager? = null
    private var flowDisposable: Disposable? = null
    private var isInterrupted: Boolean = false

    private val previousPending: PendingIntent by lazy {
        PendingIntent.getService(this, 0, intentWithAction(ACTION_PREVIOUS), 0)
    }
    private val playPending: PendingIntent by lazy {
        PendingIntent.getService(this, 0, intentWithAction(ACTION_PLAY), 0)
    }
    private val pausePending: PendingIntent by lazy {
        PendingIntent.getService(this, 0, intentWithAction(ACTION_PAUSE), 0)
    }
    private val nextPending: PendingIntent by lazy {
        PendingIntent.getService(this, 0, intentWithAction(ACTION_NEXT), 0)
    }
    private val startSlideshowPending: PendingIntent by lazy {
        PendingIntent.getService(this, 0, intentWithAction(ACTION_SLIDESHOW_START), 0)
    }
    private val stopSlideshowPending: PendingIntent by lazy {
        PendingIntent.getService(this, 0, intentWithAction(ACTION_SLIDESHOW_STOP), 0)
    }
    private val notificationDismissedPending: PendingIntent by lazy {
        PendingIntent.getService(this, 0, intentWithAction(ACTION_DISMISSED), 0)
    }

    private fun Context.intentWithAction(action: String): Intent =
        Intent(this, NotificationService::class.java).apply { this.action = action }

    private val ongoingNotification: Notification
        get() = NotificationCompat.Builder(this, CHANNEL_ID)
            .setOngoing(true)
            .setSmallIcon(android.R.color.transparent)
            .setOnlyAlertOnce(true)
            .setColor(this.resources.getColor(R.color.colorPrimary))
            .setContent(remoteViews)
            .setContentIntent(HomeActivity.getDismissIntent(this))
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setDeleteIntent(notificationDismissedPending)
            .build()

    private val autoCloseableNotification: Notification
        get() =
            NotificationCompat.Builder(this, CHANNEL_ID)
                .setAutoCancel(true)
                .setSmallIcon(android.R.color.transparent)
                .setOnlyAlertOnce(true)
                .setColor(this.resources.getColor(R.color.colorPrimary))
                .setContent(remoteViews)
                .setContentIntent(HomeActivity.getDismissIntent(this))
                .setDeleteIntent(notificationDismissedPending)
                .setPriority(NotificationCompat.PRIORITY_MIN).build()


    private var remoteViews: RemoteViews? = null
    private var notification: Notification? = null

    private val progressObserver = java.util.Observer { _, arg ->
        if (uPnPManager.currentMediaType == GalleryFragment.MediaType.VIDEO && !isInterrupted) {
            val model = arg as UPnPManager.RendererModel

            if (model.state == IRendererState.State.PLAY) {
                remoteViews?.run {
                    setImageViewResource(R.id.notification_play, R.drawable.ic_panel_pause)
                    setOnClickPendingIntent(R.id.notification_play, pausePending)
                }
                notification = ongoingNotification
            }

            if (model.state == IRendererState.State.PAUSE) {
                remoteViews?.run {
                    setImageViewResource(R.id.notification_play, R.drawable.ic_panel_play)
                    setOnClickPendingIntent(R.id.notification_play, playPending)
                }
                notification = autoCloseableNotification
            }

            if (model.state == IRendererState.State.STOP) {
                remoteViews?.run {
                    setImageViewResource(R.id.notification_play, R.drawable.ic_panel_play)
                    setOnClickPendingIntent(R.id.notification_play, playPending)
                }
                notification = autoCloseableNotification
            }

            notification?.let { notificationManager?.notify(NOTIFICATION_ID, notification) }
        }
    }

    private val actionObserver = Observer<NotificationAction> {
        when (it) {
            NotificationAction.NEXT -> playNextItem()
        }
    }

    private val slideshowProgressObserver = Observer<UPnPManager.SlideshowProgress> {
        if (uPnPManager.currentMediaType == GalleryFragment.MediaType.IMAGE && !isInterrupted)
            it?.let {
                when {
                    it.terminate -> {
                        remoteViews?.run {
                            setImageViewResource(R.id.notification_play, R.drawable.ic_panel_play)
                            setOnClickPendingIntent(R.id.notification_play, null)
                        }
                        notification = autoCloseableNotification
                    }

                    it.pauseState -> remoteViews?.run {
                        setImageViewResource(R.id.notification_play, R.drawable.ic_panel_play)
                        setOnClickPendingIntent(R.id.notification_play, startSlideshowPending)
                        notification = autoCloseableNotification
                    }

                    else -> remoteViews?.run {
                        setImageViewResource(R.id.notification_play, R.drawable.ic_panel_pause)
                        setOnClickPendingIntent(R.id.notification_play, stopSlideshowPending)
                        notification = ongoingNotification
                    }
                }
                notification?.let { notificationManager?.notify(NOTIFICATION_ID, notification) }
            }
    }

    sealed class NotificationAction {
        object NEXT : NotificationAction()
    }

    override fun onCreate() {
        super.onCreate()
        (application as App).applicationComponent.inject(this)
        notificationManager =
                applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        noImagePreviewBitmap =
                BitmapFactory.decodeResource(resources, R.drawable.placeholder_image)
        noVideoPreviewBitmap =
                BitmapFactory.decodeResource(resources, R.drawable.placeholder_video)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager!!)
        }

        setupObservers()
    }

    private fun setupObservers() {
        uPnPManager.addObserver(progressObserver)
        uPnPManager.notificationActionObservable.observe(this, actionObserver)
        uPnPManager.slideshowState.observe(this, slideshowProgressObserver)
        flowDisposable = uPnPManager.flowSubject.subscribe({ isInterrupted ->
            this.isInterrupted = isInterrupted
            if (isInterrupted)
                notificationManager?.cancel(NOTIFICATION_ID)
        }, { e -> Timber.e(e, e.message) })
    }

    private fun removeObservers() {
        uPnPManager.deleteObserver(progressObserver)
        uPnPManager.notificationActionObservable.removeObserver(actionObserver)
        uPnPManager.slideshowState.removeObserver(slideshowProgressObserver)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                ACTION_START_FOREGROUND -> showNotification(it)
                ACTION_STOP_FOREGROUND -> stop()
                ACTION_PLAY -> {
                    notification = ongoingNotification
                    uPnPManager.resume()
                }
                ACTION_PAUSE -> {
                    notification = autoCloseableNotification
                    uPnPManager.pause()
                }
                ACTION_NEXT -> {
                    notification = ongoingNotification
                    playNextItem()
                }
                ACTION_PREVIOUS -> {
                    notification = ongoingNotification
                    playPreviousItem()
                }
                ACTION_SLIDESHOW_START -> {
                    notification = ongoingNotification
                    startSlideshow()
                }
                ACTION_SLIDESHOW_STOP -> {
                    notification = autoCloseableNotification
                    pauseSlideshow()
                }
                ACTION_DISMISSED -> {
                    uPnPManager.stop()
                }
                else -> Timber.i("Unknown action")
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    private fun startSlideshow() = uPnPManager.startSlideshowTimer()

    private fun pauseSlideshow() = uPnPManager.pauseSlideshowTimer()

    private fun stop() {
        removeObservers()
        stopForeground(true)
        notificationManager?.cancel(NOTIFICATION_ID)
        stopSelf()
    }

    private fun showNotification(intent: Intent) {
        remoteViews = RemoteViews(packageName, R.layout.layout_notification)

        val title = intent.extras.getString(ITEM_TITLE)
        val url = intent.extras.getString(ITEM_URL)
        val type = intent.extras.getString(ITEM_TYPE)

        remoteViews?.run {
            setOnClickPendingIntent(R.id.notification_previous, previousPending)
            setOnClickPendingIntent(R.id.notification_next, nextPending)

            setImageViewResource(R.id.notification_thumbnail, R.mipmap.ic_launcher)
            setTextViewText(R.id.notification_title, title)

            notification = when (GalleryFragment.MediaType.valueOf(type)) {
                MediaType.IMAGE -> {
                    setOnClickPendingIntent(R.id.notification_play, startSlideshowPending)
                    autoCloseableNotification
                }
                MediaType.VIDEO -> {
                    setOnClickPendingIntent(R.id.notification_play, pausePending)
                    ongoingNotification
                }
            }
        }

        notificationTarget = NotificationTarget(
            this,
            R.id.notification_thumbnail,
            remoteViews,
            notification,
            NOTIFICATION_ID
        )
        when (type) {
            GalleryFragment.MediaType.IMAGE.name -> loadNotificationPreview(
                notificationTarget,
                url,
                noImagePreviewBitmap
            )
            GalleryFragment.MediaType.VIDEO.name -> loadNotificationPreview(
                notificationTarget,
                url,
                noVideoPreviewBitmap
            )
        }
    }

    private fun loadNotificationPreview(
        notificationTarget: NotificationTarget,
        url: String,
        image: Bitmap
    ) {
        Glide.with(this)
            .asBitmap()
            .load(url)
            .into(object : SimpleTarget<Bitmap>(120, 120) {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>) {
                    notificationTarget.onResourceReady(resource?: image , transition)
                }

                override fun onLoadFailed(errorDrawable: Drawable?) {
                    notificationTarget.onResourceReady(image, null)
                }
            })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val importance = NotificationManager.IMPORTANCE_LOW
        val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance)
        channel.description = CHANNEL_DESCRIPTION
        channel.setShowBadge(false)
        channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        notificationManager.createNotificationChannel(channel)
    }

    private fun playNextItem() {
        if(::notificationTarget.isInitialized) {
            uPnPManager.nextItem?.let {
                uPnPManager.launchItem(it.item, it.position, it.type)
                when (it.type) {
                    GalleryFragment.MediaType.IMAGE -> loadNotificationPreview(
                            notificationTarget,
                            it.uri,
                            noImagePreviewBitmap
                    )
                    GalleryFragment.MediaType.VIDEO -> loadNotificationPreview(
                            notificationTarget,
                            it.uri,
                            noVideoPreviewBitmap
                    )
                }

                remoteViews?.setTextViewText(R.id.notification_title, it.title)
                notificationManager?.notify(NOTIFICATION_ID, notification)
            }
        }
    }

    private fun playPreviousItem() {
        if(::notificationTarget.isInitialized) {
            uPnPManager.previousItem?.let {
                uPnPManager.launchItem(it.item, it.position, it.type)
                when (it.type) {
                    GalleryFragment.MediaType.IMAGE -> loadNotificationPreview(
                            notificationTarget,
                            it.uri,
                            noImagePreviewBitmap
                    )
                    GalleryFragment.MediaType.VIDEO -> loadNotificationPreview(
                            notificationTarget,
                            it.uri,
                            noVideoPreviewBitmap
                    )
                }

                remoteViews?.setTextViewText(R.id.notification_title, it.title)
                notificationManager?.notify(NOTIFICATION_ID, notification)
            }
        }
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        stop()
    }

    companion object {
        const val ACTION_START_FOREGROUND =
            "com.wezom.kiviremote.services.notification.START_FOREGROUND"

        const val ACTION_STOP_FOREGROUND =
            "com.wezom.kiviremote.services.notification.STOP_FOREGROUND"

        const val ACTION_NEXT = "com.wezom.kiviremote.services.notification.NEXT"

        const val ACTION_PREVIOUS = "com.wezom.kiviremote.services.notification.PREVIOUS"

        const val ACTION_PLAY = "com.wezom.kiviremote.services.notification.PLAY"

        const val ACTION_PAUSE = "com.wezom.kiviremote.services.notification.PAUSE"

        const val ACTION_SLIDESHOW_START =
            "com.wezom.kiviremote.services.notification.SLIDESHOW_START"

        const val ACTION_SLIDESHOW_STOP =
            "com.wezom.kiviremote.services.notification.SLIDESHOW_PAUSE"

        const val ACTION_DISMISSED = "com.wezom.kiviremote.services.notification.DISMISSED"

        const val CHANNEL_ID = "kivi_channel"

        const val CHANNEL_NAME = "KIVI channel"

        const val CHANNEL_DESCRIPTION = "Used to notify user about current media item being played"

        const val ITEM_TITLE = "title"

        const val ITEM_URL = "url"

        const val ITEM_POSITION = "position"

        const val ITEM_TYPE = "type"
    }
}