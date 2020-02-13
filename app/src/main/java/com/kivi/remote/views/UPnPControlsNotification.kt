package com.kivi.remote.views

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.kivi.remote.R
import com.kivi.remote.presentation.home.HomeActivity
import com.kivi.remote.services.CleanupService
import com.kivi.remote.services.NotificationService


class UPnPControlsNotification(context: Context, packageName: String, layoutId: Int) :
    RemoteViews(packageName, layoutId) {

    init {
        val play = context.applicationContext.getIntent(HomeActivity::class.java)
        val next = context.applicationContext.getIntent(NotificationService::class.java)
        val previous = context.applicationContext.getIntent(CleanupService::class.java)

        play.action = ACTION_PLAY
        next.action = ACTION_NEXT
        previous.action = ACTION_PREVIOUS

        val playPending = PendingIntent.getActivity(
            context.applicationContext,
            100,
            play,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val nextPending = PendingIntent.getService(
            context.applicationContext,
            101,
            next,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val previousPending = PendingIntent.getService(
            context.applicationContext,
            102,
            previous,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        setOnClickPendingIntent(R.id.notification_play, playPending)
        setOnClickPendingIntent(R.id.notification_next, nextPending)
        setOnClickPendingIntent(R.id.notification_previous, previousPending)
    }

    companion object {
        const val ACTION_PLAY = "com.kivi.remote.ACTION_PLAY"
        const val ACTION_NEXT = "com.kivi.remote.ACTION_NEXT"
        const val ACTION_PREVIOUS = "com.kivi.remote.ACTION_PREVIOUS"

        private fun Context.getIntent(cls: Class<*>) = Intent(this, cls)
    }
}