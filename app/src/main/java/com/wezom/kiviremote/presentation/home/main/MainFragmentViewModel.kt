package com.wezom.kiviremote.presentation.home.main

import com.wezom.kiviremote.Screens
import com.wezom.kiviremote.bus.DisconnectEvent
import com.wezom.kiviremote.bus.SendActionEvent
import com.wezom.kiviremote.bus.SendKeyEvent
import com.wezom.kiviremote.bus.SendTextEvent
import com.wezom.kiviremote.common.Action
import com.wezom.kiviremote.common.RxBus
import com.wezom.kiviremote.presentation.base.BaseViewModel
import com.wezom.kiviremote.upnp.UPnPManager
import ru.terrakok.cicerone.Router


class MainFragmentViewModel(private val router: Router, private val uPnPManager: UPnPManager) :
    BaseViewModel() {

    fun disconnect() {
        RxBus.publish(DisconnectEvent())
    }

    fun sendText(text: String) {
        RxBus.publish(SendTextEvent(text))
    }

    fun navigateToDevices() {
        router.navigateTo(Screens.RECENT_DEVICES_FRAGMENT)
//        router.navigateTo(Screens.SUBS_PRICE_LIST_FRAGMENT)
    }

    fun sendKeyEvent(keyEvent: Int) {
        RxBus.publish(SendKeyEvent(keyEvent))
    }

    fun requestApps() {
        RxBus.publish((SendActionEvent(Action.REQUEST_APPS)))
    }

    fun startUPnPController() = uPnPManager.controller.resume()
}