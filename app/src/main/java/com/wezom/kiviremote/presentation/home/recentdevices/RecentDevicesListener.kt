package com.wezom.kiviremote.presentation.home.recentdevices

import com.wezom.kiviremote.persistence.model.RecentDevice


interface RecentDevicesListener {
    fun infoBtnChosen(recentDevice: RecentDevice, position: Int)
    fun connectDeviceChosen(recentDevice: RecentDevice, position: Int)
}