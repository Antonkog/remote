package com.wezom.kiviremote.presentation.home.tvsettings

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.MutableLiveData
import com.wezom.kiviremote.App
import com.wezom.kiviremote.bus.GotAspectEvent
import com.wezom.kiviremote.bus.NewAspectEvent
import com.wezom.kiviremote.bus.RequestAspectEvent
import com.wezom.kiviremote.common.Constants
import com.wezom.kiviremote.common.PreferencesManager
import com.wezom.kiviremote.common.RxBus
import com.wezom.kiviremote.net.model.AspectMessage
import com.wezom.kiviremote.presentation.base.BaseViewModel
import io.reactivex.rxkotlin.subscribeBy
import ru.terrakok.cicerone.Router
import timber.log.Timber


class TvSettingsViewModel(private val router: Router) : BaseViewModel() {

    fun sendAspectSingleChangeEvent(valueType: AspectMessage.ASPECT_VALUE, value: Int) {
        val builder = AspectMessage.AspectMsgBuilder()
        builder.addValue(valueType, value)
        RxBus.publish(NewAspectEvent(builder.buildAspect()))
        if(valueType == AspectMessage.ASPECT_VALUE.PICTUREMODE) requestAspect()
    }

    fun goBack () = router.exit()

    fun requestAspect() =  RxBus.publish(RequestAspectEvent())

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

    val aspectChange = MutableLiveData<GotAspectEvent?>()

    init {
        disposables += RxBus.listen(GotAspectEvent::class.java).subscribeBy(
                onNext = {
                    aspectChange.postValue(it)
                }, onError = Timber::e
        )
    }
}