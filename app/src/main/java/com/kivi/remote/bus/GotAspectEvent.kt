package com.kivi.remote.bus


import com.kivi.remote.common.Constants
import com.kivi.remote.net.model.AspectAvailable
import com.kivi.remote.net.model.AspectMessage

data class GotAspectEvent(val msg: AspectMessage?, val available: AspectAvailable?) {
    fun getManufacture(): Int {
        if (available != null && msg != null) {
            var isRealtek = msg.manufacture
            if (isRealtek == Constants.NO_VALUE) isRealtek = available.getManufacture(msg.serverVersionCode)
            return isRealtek
        }
        return Constants.NO_VALUE
    }

    fun hasAspectSettings(): Boolean {
        return msg?.settings != null && available?.settings != null && !available.settings.isEmpty()
    }
}