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
import io.reactivex.rxkotlin.subscribeBy
import ru.terrakok.cicerone.Router
import timber.log.Timber


class TouchpadViewModel (private val router: Router) : BaseViewModel(), TvKeysViewModel {

    val aspectSeen = MutableLiveData<GotAspectEvent>()

    init {
        disposables += RxBus.listen(GotAspectEvent::class.java).subscribeBy(
                onNext = {
                    aspectSeen.postValue(it)
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

    fun sendScrollEvent(action: Action, y: Double) {
        RxBus.publish(SendScrollEvent(action, y))
    }

    fun goToInputSettings() = router.navigateTo(Screens.PORTS_FRAGMENT)

}