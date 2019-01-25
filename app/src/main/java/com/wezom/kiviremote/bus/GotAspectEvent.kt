package com.wezom.kiviremote.bus

import com.wezom.kiviremote.common.Constants
import com.wezom.kiviremote.net.model.AspectAvailable
import com.wezom.kiviremote.net.model.AspectMessage

data class GotAspectEvent(val msg: AspectMessage, val available: AspectAvailable) {
    fun getManufacture(): Int {
        if (available != null && msg != null) {
            var isRealtek = msg?.manufacture
            if (isRealtek == Constants.NO_VALUE) isRealtek = available.getManufacture(msg?.serverVersionCode)
            return isRealtek;
        }
        return Constants.NO_VALUE
    }
    fun hasAspectSettings(): Boolean {
        return msg!=null && available!=null && msg.settings!=null && available.settings!=null
    }
    fun hasManufacture(): Boolean {
        return when (getManufacture()) {
            Constants.SERV_REALTEK -> true
            Constants.SERV_MSTAR -> true
            else -> false
        }
    }
}