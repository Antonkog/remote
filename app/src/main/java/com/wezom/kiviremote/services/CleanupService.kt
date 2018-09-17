package com.wezom.kiviremote.services

import android.app.IntentService
import android.app.Service
import android.content.Intent
import com.wezom.kiviremote.App
import com.wezom.kiviremote.upnp.UPnPManager
import javax.inject.Inject


class CleanupService : IntentService("cleanup_service") {

    @Inject
    lateinit var manager: UPnPManager

    override fun onCreate() {
        super.onCreate()
        (application as App).applicationComponent.inject(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int) = Service.START_STICKY

    override fun onHandleIntent(intent: Intent?) {
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        manager.stop()
//        uPnPManager.controller.pause()
        // todo delete notification channel
        stopSelf()
    }
}