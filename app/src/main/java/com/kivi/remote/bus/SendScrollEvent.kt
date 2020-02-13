package com.kivi.remote.bus

import com.kivi.remote.common.Action

data class SendScrollEvent(val action: Action, val y: Double)