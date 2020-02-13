package com.kivi.remote.presentation.home.recentdevices

import com.kivi.remote.persistence.model.RecentDevice


interface RecentDevicesListener {
    fun infoBtnChosen(recentDevice: RecentDevice, position: Int)
    fun connectDeviceChosen(recentDevice: RecentDevice, position: Int)
}