package com.wezom.kiviremote.presentation.home.apps


data class AppModel(val appName: String, val appPackage: String) : Comparable<AppModel> {
    override fun compareTo(other: AppModel): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
