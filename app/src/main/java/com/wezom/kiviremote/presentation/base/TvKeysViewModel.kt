package com.wezom.kiviremote.presentation.base

import com.wezom.kiviremote.bus.SendActionEvent
import com.wezom.kiviremote.bus.SendKeyEvent
import com.wezom.kiviremote.bus.ShowHideAspectEvent
import com.wezom.kiviremote.common.Action
import com.wezom.kiviremote.common.RxBus


interface TvKeysViewModel {

     fun sendHomeDown() = RxBus.publish(SendActionEvent(Action.HOME_DOWN))

     fun sendHomeUp() = RxBus.publish(SendActionEvent(Action.HOME_UP))

     fun sendKeyEvent(keyEvent: Int) {
        RxBus.publish(SendKeyEvent(keyEvent))
    }
     fun launchQuickApps() = RxBus.publish(SendActionEvent(Action.LAUNCH_QUICK_APPS))

     fun showHideAspect() = RxBus.publish(ShowHideAspectEvent())

}