package com.wezom.kiviremote.bus

import com.wezom.kiviremote.net.model.AspectAvailable
import com.wezom.kiviremote.net.model.AspectMessage

data class GotAspectEvent(val msg: AspectMessage, val available: AspectAvailable)