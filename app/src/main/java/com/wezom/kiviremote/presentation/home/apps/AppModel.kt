package com.wezom.kiviremote.presentation.home.apps


data class AppModel(val appName: String, val appPackage: String) : Comparable<AppModel> {
    override fun compareTo(other: AppModel): Int {
        return 0
    }
}
