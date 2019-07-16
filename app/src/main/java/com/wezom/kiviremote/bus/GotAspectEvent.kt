package com.wezom.kiviremote.bus


import com.wezom.kiviremote.common.Constants
import com.wezom.kiviremote.common.extensions.PortsUtils
import com.wezom.kiviremote.net.model.*
import com.wezom.kiviremote.presentation.home.tvsettings.AspectHolder

data class GotAspectEvent(val msg: AspectMessage?, val available: AspectAvailable?, val initMsg: InitialMessage?) {
    fun getManufacture(): Int {
        if (available != null && msg != null) {
            var isRealtek = msg.manufacture
            if (isRealtek == Constants.NO_VALUE) isRealtek = available.getManufacture(msg.serverVersionCode)
            return isRealtek;
        }
        return Constants.NO_VALUE
    }

    fun hasAspectSettings(): Boolean {
        return msg?.settings != null && available?.settings != null && !available.settings.isEmpty()
    }


    fun getInputsList(): List<Input> {
        return PortsUtils.getNewInputsList(AspectHolder.initialMsg?.driverValueList, AspectHolder.availableSettings, AspectHolder.message)
    }
}