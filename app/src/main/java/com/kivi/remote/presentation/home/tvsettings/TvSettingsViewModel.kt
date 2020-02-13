package com.kivi.remote.presentation.home.tvsettings

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.MutableLiveData
import com.kivi.remote.App
import com.kivi.remote.bus.GotAspectEvent
import com.kivi.remote.bus.NewAspectEvent
import com.kivi.remote.bus.RequestAspectEvent
import com.kivi.remote.common.Constants
import com.kivi.remote.common.PreferencesManager
import com.kivi.remote.common.RxBus
import com.kivi.remote.net.model.AspectMessage
import com.kivi.remote.presentation.base.BaseViewModel
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