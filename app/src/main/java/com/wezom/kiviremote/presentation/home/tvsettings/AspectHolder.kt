package com.wezom.kiviremote.presentation.home.tvsettings

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
}
