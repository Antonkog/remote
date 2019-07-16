package com.wezom.kiviremote.presentation.home.tvsettings

import com.wezom.kiviremote.common.extensions.PortsUtils
import com.wezom.kiviremote.net.model.*
import timber.log.Timber


object AspectHolder {
    var message: AspectMessage? = null
    var availableSettings: AspectAvailable? = null
    var initialMsg: InitialMessage? = null


    fun clean() {
        Timber.i("cleaning aspect")
        message = null
        availableSettings = null
        initialMsg = null
    }

    fun setAspectValues(msg: AspectMessage?, available: AspectAvailable?, initMsg: InitialMessage?) {
        message = msg
        availableSettings = available
        if (initMsg != null) initialMsg = initMsg
    }

    fun hasAspectSettings(): Boolean {
        return message?.settings != null && availableSettings?.settings != null && !(availableSettings?.settings?.isEmpty()
                ?: true)
    }

    fun getInputsList(): List<Input> {
        return PortsUtils.getNewInputsList(initialMsg?.driverValueList, availableSettings, message)
    }

}
