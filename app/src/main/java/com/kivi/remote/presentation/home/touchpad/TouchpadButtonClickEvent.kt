package com.kivi.remote.presentation.home.touchpad

import com.kivi.remote.common.Action


data class TouchpadButtonClickEvent(val x: Double, val y: Double, val action: Action)