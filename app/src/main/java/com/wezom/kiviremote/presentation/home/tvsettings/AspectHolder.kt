package com.wezom.kiviremote.presentation.home.tvsettings

import com.wezom.kiviremote.common.Constants
import com.wezom.kiviremote.common.Constants.SERV_MSTAR
import com.wezom.kiviremote.common.Constants.SERV_REALTEK
import com.wezom.kiviremote.net.model.AspectAvailable
import com.wezom.kiviremote.net.model.AspectMessage
import timber.log.Timber


object AspectHolder {
    var message: AspectMessage? = null
    var availableSettings: AspectAvailable? = null
    fun clean() {
        Timber.i("cleaning aspect")
        message = null
        availableSettings = null
    }

    fun getManufacture(): Int {
        if (availableSettings != null && message != null) {
            var isRealtek = message?.manufacture ?: Constants.NO_VALUE
            if (isRealtek == Constants.NO_VALUE) isRealtek = availableSettings!!.getManufacture(message?.serverVersionCode)
            return isRealtek
        }
        return Constants.NO_VALUE
    }

    fun hasAspectSettings(): Boolean {
        return availableSettings != null && message != null && message?.settings?.isEmpty() ?: false && availableSettings?.settings?.isEmpty() ?: false
    }

    fun hasManufacture(): Boolean {
        return when (getManufacture()) {
            SERV_REALTEK -> true
            SERV_MSTAR -> true
            else -> {
                false
            }
        }
    }
}
