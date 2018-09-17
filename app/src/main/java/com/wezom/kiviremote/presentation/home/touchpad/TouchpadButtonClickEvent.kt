package com.wezom.kiviremote.presentation.home.touchpad

import com.wezom.kiviremote.common.Action


data class TouchpadButtonClickEvent(val x: Double, val y: Double, val action: Action)