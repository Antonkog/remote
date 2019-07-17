package com.wezom.kiviremote.presentation.home.recentdevices

import com.wezom.kiviremote.nsd.NsdServiceInfoWrapper
import com.wezom.kiviremote.persistence.model.RecentDevice
import java.io.Serializable

data class TvDeviceInfo(var recentDevice: RecentDevice, var nsdServiceInfoWrapper: NsdServiceInfoWrapper?, var indexInRecentList: Int = -1): Serializable