package com.kivi.remote.presentation.home.tvsettings.driver_set

data class DriverValue(var enumValueName: String,
                       var currentName: String,
                       val valPrimaryKey: String,
                       val intCondition: Int,
                       val active: Boolean)