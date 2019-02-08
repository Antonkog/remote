package com.wezom.kiviremote.common


enum class Action {
    REQUEST_INITIAL,
    REQUEST_VOLUME,
    REQUEST_APPS,
    REQUEST_ASPECT,
    LAUNCH_APP,
    PING,
    SWITCH_OFF,
    SCROLL,//old
    SCROLL_TOP_TO_BOTTOM,
    SCROLL_BOTTOM_TO_TOP,
    HOME_DOWN,
    HOME_UP,
    LAUNCH_QUICK_APPS,
    OPEN_SETTINGS,
    motion,
    leftClick,
    keyevent,
    text,
    NAME_CHANGE,
}