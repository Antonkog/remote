package com.wezom.kiviremote.bus


data class SendScrollEvent(val scrollTopToBottom: Boolean, val y: Double)