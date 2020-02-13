package com.kivi.remote.presentation.base

import com.kivi.remote.bus.SendActionEvent
import com.kivi.remote.bus.SendKeyEvent
import com.kivi.remote.bus.ShowHideAspectEvent
import com.kivi.remote.common.Action
import com.kivi.remote.common.RxBus


interface TvKeysViewModel {

     fun sendHomeDown() = RxBus.publish(SendActionEvent(Action.HOME_DOWN))

     fun sendHomeUp() = RxBus.publish(SendActionEvent(Action.HOME_UP))

     fun sendKeyEvent(keyEvent: Int) {
        RxBus.publish(SendKeyEvent(keyEvent))
    }
     fun launchQuickApps() = RxBus.publish(SendActionEvent(Action.LAUNCH_QUICK_APPS))

     fun showHideAspect() = RxBus.publish(ShowHideAspectEvent())

}