package com.wezom.kiviremote.presentation.home.tvsettings

import android.arch.lifecycle.MutableLiveData
import com.wezom.kiviremote.Screens
import com.wezom.kiviremote.bus.GotAspectEvent
import com.wezom.kiviremote.bus.NewAspectEvent
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
        Timber.e("sending aspect 3: " +builder.buildAspect().settings.toString())

    }

    fun goBack() = router.navigateTo(Screens.REMOTE_CONTROL_FRAGMENT)

    val aspectChange = MutableLiveData<GotAspectEvent?>()

    init {
        disposables += RxBus.listen(GotAspectEvent::class.java).subscribeBy(
                onNext = {
                    aspectChange.postValue(it)
                }, onError = Timber::e
        )
    }
}