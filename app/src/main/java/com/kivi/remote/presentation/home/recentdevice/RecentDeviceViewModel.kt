package com.kivi.remote.presentation.home.recentdevice

import android.content.SharedPreferences
import androidx.navigation.NavController
import com.kivi.remote.R
import com.kivi.remote.common.Constants
import com.kivi.remote.common.extensions.string
import com.kivi.remote.persistence.AppDatabase
import com.kivi.remote.persistence.model.RecentDevice
import com.kivi.remote.presentation.base.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber


class RecentDeviceViewModel(val database: AppDatabase, val navController: NavController, preferences: SharedPreferences) : BaseViewModel() {

    var lastNsdHolderName by preferences.string(Constants.UNIDENTIFIED, key = Constants.LAST_NSD_HOLDER_NAME)

    fun saveChanges(model: RecentDevice) {
        GlobalScope.launch(Dispatchers.Default) {
            val update = database.recentDeviceDao().update(model)
            if (update > 0) Timber.e("setting new name in db: $update")
        }
    }

    fun goBack(recentDevice: RecentDevice) {
        GlobalScope.launch(Dispatchers.Default) {
            database.recentDeviceDao().removeByName(recentDevice.actualName)
        }
        navController.navigate(R.id.action_recentDeviceFragment_to_deviceSearchFragment)
//        navController.navigate(R.id.action_global_deviceSearchFragment)
    }

}