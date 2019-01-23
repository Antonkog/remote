package com.wezom.kiviremote.bus

import com.wezom.kiviremote.common.Constants
import com.wezom.kiviremote.net.model.AspectAvailable
import com.wezom.kiviremote.net.model.AspectMessage

data class GotAspectEvent(val msg: AspectMessage, val available: AspectAvailable) {
     fun getManufacture(): Int {
        if (available != null && msg != null) {
            var isRealtek = msg?.manufacture ?: Constants.NO_VALUE
            if(isRealtek == Constants.NO_VALUE){
                val versionCode = msg?.serverVersionCode
                versionCode.let {
                    isRealtek = available.getManufacture(versionCode) // only 18 server version
                }
            }
            return isRealtek;
        }
        return Constants.NO_VALUE
    }
}