package com.wezom.kiviremote.presentation.home.recentdevices

import android.net.nsd.NsdServiceInfo
import com.wezom.kiviremote.persistence.model.RecentDevice
import java.io.Serializable

data class TvDeviceInfo(var recentDevice: RecentDevice, var nsdServiceInfoWrapper: NsdServiceInfo?, var indexInRecentList: Int = -1): Serializable {
    override fun toString(): String {
        return "TvDeviceInfo(recentDevice=$recentDevice, nsdServiceInfoWrapper=$nsdServiceInfoWrapper, indexInRecentList=$indexInRecentList)"
    }
}