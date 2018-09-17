package com.wezom.kiviremote.presentation.home.touchpad

import com.wezom.kiviremote.bus.SendActionEvent
import com.wezom.kiviremote.bus.SendCursorCoordinatesEvent
import com.wezom.kiviremote.bus.SendKeyEvent
import com.wezom.kiviremote.bus.SendScrollEvent
import com.wezom.kiviremote.common.Action
import com.wezom.kiviremote.common.RxBus
import com.wezom.kiviremote.presentation.base.BaseViewModel
import timber.log.Timber


class TouchpadViewModel : BaseViewModel() {

    fun sendMotionMessage(x: Double, y: Double) {
        RxBus.publish(SendCursorCoordinatesEvent(x, y))
    }

    fun sendClickMessage(x: Double, y: Double, buttonType: Action) {
        Timber.d("sendClickMessage %s %s %s ", x, y, buttonType)
        RxBus.publish(TouchpadButtonClickEvent(x, y, buttonType))
    }

    fun sendScrollEvent(y: Double) {
        RxBus.publish(SendScrollEvent(y))
    }

    fun sendKeyEvent(keyEvent: Int) {
        RxBus.publish(SendKeyEvent(keyEvent))
    }

    fun sendHomeDown() = RxBus.publish(SendActionEvent(Action.HOME_DOWN))

    fun sendHomeUp() = RxBus.publish(SendActionEvent(Action.HOME_UP))

    fun launchQuickApps() = RxBus.publish(SendActionEvent(Action.LAUNCH_QUICK_APPS))
}