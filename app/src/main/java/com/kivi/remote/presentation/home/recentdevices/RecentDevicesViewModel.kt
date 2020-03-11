package com.kivi.remote.presentation.home.recentdevices

import android.content.Context
import android.content.SharedPreferences
import android.net.nsd.NsdServiceInfo
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import com.kivi.remote.common.Constants
import com.kivi.remote.common.extensions.backToMain
import com.kivi.remote.common.extensions.string
import com.kivi.remote.common.restartApp
import com.kivi.remote.persistence.AppDatabase
import com.kivi.remote.persistence.model.RecentDevice
import com.kivi.remote.presentation.base.BaseViewModel
import timber.log.Timber

class RecentDevicesViewModel(private val navController: NavController,
                             private val database: AppDatabase,
                             preferences: SharedPreferences) : BaseViewModel() {

    var lastNsdHolderName by preferences.string(Constants.UNIDENTIFIED, key = Constants.LAST_NSD_HOLDER_NAME)

    val recentDevices = MutableLiveData<List<RecentDevice>>()

    fun navigateToRecentDevice(data: RecentDevice) = navController.navigate(RecentDevicesFragmentDirections.actionRecentDevicesFragmentToRecentDeviceFragment(data))  //navController.navigate(R.id.action_recentDevicesFragment_to_recentDeviceFragment)

    fun connect(data: NsdServiceInfo, contetx: Context) {
        lastNsdHolderName = data.serviceName
        restartApp(contetx)
    }

    fun requestRecentDevices() {
        disposables += database.recentDeviceDao().fiveByConnection.backToMain()
                .subscribe({ result -> recentDevices.postValue(result) }, { t -> Timber.e(t, t.message) })
    }
}