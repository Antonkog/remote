package com.wezom.kiviremote.bus

import com.wezom.kiviremote.common.Action

data class SendScrollEvent(val action: Action, val y: Double)