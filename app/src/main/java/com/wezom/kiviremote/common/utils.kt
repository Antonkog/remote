@file:JvmName("Utils")

package com.wezom.kiviremote.common

import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.Intent.*
import android.content.res.Resources
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.MediaStore
import android.support.v4.content.ContextCompat
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.wezom.kiviremote.App
import com.wezom.kiviremote.common.Constants.BUNDLE_REALUNCH_KEY
import com.wezom.kiviremote.presentation.splash.SplashActivity
import io.reactivex.SingleTransformer
import io.reactivex.android.schedulers.AndroidSchedulers
import timber.log.Timber
import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.UnknownHostException
import java.util.*


data class VideoInfo(val data: String, val title: String)
data class ImageInfo(val data: String, val title: String)

private const val keyRestartIntents = "phoenix_restart"

val imageDirectoriesPreviews: Map<String, String> = mutableMapOf()
val videoDirectoriesPreviews: Map<String, String> = mutableMapOf()

fun getAllVideos(context: Context): HashSet<VideoInfo> {
    val videoItemHashSet = HashSet<VideoInfo>()
    val projection = arrayOf(MediaStore.Video.VideoColumns.DATA, MediaStore.Video.Media.TITLE)
    val cursor = context.contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            null
    )
    with(cursor) {
        try {
            moveToFirst()
            do {
                val data = getString(getColumnIndexOrThrow(MediaStore.Video.VideoColumns.DATA))
                val title = getString(getColumnIndexOrThrow(MediaStore.Video.Media.TITLE))
                videoItemHashSet.add(VideoInfo(data, title))
            } while (cursor.moveToNext())
            close()
        } catch (e: Exception) {
            Timber.e(e, e.message)
        }
    }

    return videoItemHashSet
}

fun getAllImages(context: Context): HashSet<ImageInfo> {
    val imagesHashSet = HashSet<ImageInfo>()
    val projection = arrayOf(MediaStore.Images.ImageColumns.DATA, MediaStore.Images.Media.TITLE)
    val cursor = context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            null
    )
    with(cursor) {
        try {
            moveToFirst()
            do {
                val data = getString(getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATA))
                val title = getString(getColumnIndexOrThrow(MediaStore.Images.Media.TITLE))
                imagesHashSet.add(ImageInfo(data, title))
            } while (cursor.moveToNext())
            close()
        } catch (e: Exception) {
            Timber.e(e, e.message)
        }
    }

    return imagesHashSet
}

fun isNetworkAvailable(context: Context): Boolean {
    val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetworkInfo = connectivityManager.activeNetworkInfo
    return activeNetworkInfo != null && activeNetworkInfo.isConnected
}

fun isKitKatOrHigher(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT

fun hideKeyboard(activity: Activity) {
    val view = activity.currentFocus ?: View(activity)
    val inputMethodManager =
            activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
}

fun showKeyboard(activity: Activity) {
    val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)
}

fun dpToPx(context: Context, dps: Int) = Math.round(context.resources.displayMetrics.density * dps)

fun getColorCompat(context: Context, resId: Int) = ContextCompat.getColor(context, resId)

fun <T> applyToMainThreadSchedulers(): SingleTransformer<T, T> = SingleTransformer { observable ->
    observable.observeOn(AndroidSchedulers.mainThread()).doOnError(Throwable::printStackTrace)
}

fun getStatusBarHeight(resources: Resources): Int {
    var result = 0
    val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
    if (resourceId > 0) {
        result = resources.getDimensionPixelSize(resourceId)
    }
    return result
}

private fun getLocalIpAddressFromIntf(intfName: String): InetAddress? {
    try {
        val intf = NetworkInterface.getByName(intfName)
        if (intf.isUp) {
            val enumIpAddr = intf.inetAddresses
            while (enumIpAddr.hasMoreElements()) {
                val inetAddress = enumIpAddr.nextElement()
                if (!inetAddress.isLoopbackAddress && inetAddress is Inet4Address)
                    return inetAddress
            }
        }
    } catch (e: Exception) {
        Timber.w("Unable to get ip address for interface $intfName")
    }

    return null
}

@Throws(UnknownHostException::class)
fun getLocalIpAddress(ctx: Context): InetAddress {
    val wifiManager = ctx.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    val wifiInfo = wifiManager.connectionInfo
    val ipAddress = wifiInfo.ipAddress
    if (ipAddress != 0)
        return InetAddress.getByName(
                String.format(
                        "%d.%d.%d.%d",
                        ipAddress and 0xff, ipAddress shr 8 and 0xff,
                        ipAddress shr 16 and 0xff, ipAddress shr 24 and 0xff
                )
        )

    Timber.w("No ip address isAvailable through wifi uPnPManager, try to get it manually")

    var inetAddress: InetAddress? = getLocalIpAddressFromIntf("wlan0")

    if (inetAddress != null) {
        Timber.w("Got an ip for interface wlan0")
        return inetAddress
    }

    inetAddress = getLocalIpAddressFromIntf("usb0")
    if (inetAddress != null) {
        Timber.w("Got an ip for interface usb0")
        return inetAddress
    }

    return InetAddress.getByName("0.0.0.0")
}

fun triggerRebirth(context: Context, vararg intents: Intent) {
    val intent = Intent(context, SplashActivity::class.java)
    intent.addFlags(FLAG_ACTIVITY_NEW_TASK) // In case we are called with non-Activity context.
    intent.putParcelableArrayListExtra(keyRestartIntents, java.util.ArrayList(intents.asList()))
    context.startActivity(intent)
    if (context is Activity) {
        context.finish()
    }
    Runtime.getRuntime().exit(0) // Kill kill kill!
}

fun triggerRebirth(context: Context) {
    triggerRebirth(context, getRestartIntent(context))
}

fun restartAppColorScheme(ctx: Activity?) {
    if (ctx != null) {
        PreferencesManager.setDarkMode(!App.isDarkMode())
//        val i = ctx.packageManager.getLaunchIntentForPackage(ctx.packageName)
//        val pi = PendingIntent.getActivity(ctx, Constants.RESTART_APP_PI, i, PendingIntent.FLAG_CANCEL_CURRENT)
//        val alarmMgr = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
//        alarmMgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, pi)
//        System.exit(0)

        val i = ctx.intent
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        i.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
        i.putExtra(BUNDLE_REALUNCH_KEY, true)
        ctx.finish()
        ctx.startActivity(i)
    }
}

fun restartApp(ctx: Context) {

    try {
        val i = ctx.packageManager
                .getLaunchIntentForPackage(ctx.packageName)
        val pi = PendingIntent.getActivity(ctx, Constants.RESTART_APP_PI, i, PendingIntent.FLAG_CANCEL_CURRENT)
        val alarmMgr = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmMgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, pi)
        System.exit(0)
    } catch (a: java.lang.Exception) {
        Timber.e(a)
    }
}

private fun getRestartIntent(context: Context): Intent {
    val defaultIntent = Intent(ACTION_MAIN, null).apply {
        addFlags(FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_CLEAR_TASK)
        addCategory(CATEGORY_DEFAULT)
    }

    with(context) {
        packageManager.queryIntentActivities(defaultIntent, 0).forEach {
            if (it.activityInfo.packageName == packageName) {
                defaultIntent.component = ComponentName(packageName, it.activityInfo.name)
                return defaultIntent
            }
        }

        throw IllegalStateException(
                "Unable to determine default activity for $packageName"
                        + ". Does an activity specify the DEFAULT category in its intent filter?"
        )
    }
}





