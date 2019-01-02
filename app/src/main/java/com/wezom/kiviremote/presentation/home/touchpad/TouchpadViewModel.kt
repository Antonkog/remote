package com.wezom.kiviremote.presentation.home.touchpad

import android.arch.lifecycle.MutableLiveData
import com.wezom.kiviremote.Screens
import com.wezom.kiviremote.bus.GotAspectEvent
import com.wezom.kiviremote.bus.SendCursorCoordinatesEvent
import com.wezom.kiviremote.bus.SendScrollEvent
import com.wezom.kiviremote.common.Action
import com.wezom.kiviremote.common.RxBus
import com.wezom.kiviremote.presentation.base.BaseViewModel
import com.wezom.kiviremote.presentation.base.TvKeysViewModel
import com.wezom.kiviremote.presentation.home.tvsettings.AspectHolder
import io.reactivex.rxkotlin.subscribeBy
import ru.terrakok.cicerone.Router
import timber.log.Timber


class TouchpadViewModel (private val router: Router) : BaseViewModel(), TvKeysViewModel {

    val aspectSeen = MutableLiveData<Boolean?>()

    init {
        disposables += RxBus.listen(GotAspectEvent::class.java).subscribeBy(
                onNext = {
                    Timber.e("got aspect2: " + AspectHolder.availableSettings.toString() + AspectHolder.message.toString())
                    if (AspectHolder.availableSettings != null && AspectHolder.message != null) {
                        aspectSeen.postValue(true)
                    } else {
                        aspectSeen.postValue(false)
                    }
                }, onError = Timber::e
        )
    }

    fun sendMotionMessage(x: Double, y: Double) {
        RxBus.publish(SendCursorCoordinatesEvent(x, y))
    }

    fun sendClickMessage(x: Double, y: Double, buttonType: Action) {
        Timber.d("sendClickMessage %s %s %s ", x, y, buttonType)
        RxBus.publish(TouchpadButtonClickEvent(x, y, buttonType))
    }

    fun sendScrollEvent(scrollTopToBottom: Boolean, y: Double) {
        RxBus.publish(SendScrollEvent(scrollTopToBottom, y))
    }

    fun goToInputSettings() = router.navigateTo(Screens.PORTS_FRAGMENT)

}