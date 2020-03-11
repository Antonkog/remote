@file:Suppress("unchecked_cast")

package com.kivi.remote.presentation.base

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import com.kivi.remote.common.KiviCache
import com.kivi.remote.common.ResourceProvider
import com.kivi.remote.di.scopes.ActivityScope
import com.kivi.remote.nsd.NsdHelper
import com.kivi.remote.persistence.AppDatabase
import com.kivi.remote.presentation.home.HomeActivityViewModel
import com.kivi.remote.presentation.home.devicesearch.DeviceSearchViewModel
import com.kivi.remote.presentation.home.directories.DirectoriesViewModel
import com.kivi.remote.presentation.home.gallery.GalleryViewModel
import com.kivi.remote.presentation.home.kivi_catalog.KiviCatalogSeriesViewModel
import com.kivi.remote.presentation.home.kivi_catalog.KiviCatalogViewModel
import com.kivi.remote.presentation.home.media.MediaViewModel
import com.kivi.remote.presentation.home.player.PlayerViewModel
import com.kivi.remote.presentation.home.recentdevice.RecentDeviceViewModel
import com.kivi.remote.presentation.home.recentdevices.RecentDevicesViewModel
import com.kivi.remote.presentation.home.recommendations.RecommendationsViewModel
import com.kivi.remote.presentation.home.recommendations.deep.AppsDeepViewModel
import com.kivi.remote.presentation.home.recommendations.deep.ChannelsDeepViewModel
import com.kivi.remote.presentation.home.recommendations.deep.RecsDeepViewModel
import com.kivi.remote.presentation.home.subscriptions.subs_info.SubsInfoViewModel
import com.kivi.remote.presentation.home.subscriptions.subs_payment.SubsPaymentViewModel
import com.kivi.remote.presentation.home.subscriptions.subs_price_list.SubsPriceListViewModel
import com.kivi.remote.presentation.home.subscriptions.subs_tariff_plans.SubsTariffPlansViewModel
import com.kivi.remote.presentation.home.touchpad.TouchpadViewModel
import com.kivi.remote.upnp.UPnPManager
import javax.inject.Inject

@ActivityScope
class BaseViewModelFactory @Inject constructor(
        private val database: AppDatabase,
        private val cache: KiviCache,
        private val navController: NavController,
        private val uPnPManager: UPnPManager,
        private val nsdHelper: NsdHelper,
        private val preferences: SharedPreferences,
        private val resourceProvider: ResourceProvider) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T = when {
        modelClass.isAssignableFrom(HomeActivityViewModel::class.java) ->
            HomeActivityViewModel(database, cache, navController, preferences) as T

        modelClass.isAssignableFrom(RecommendationsViewModel::class.java) ->
            RecommendationsViewModel(navController, database, cache, preferences) as T

        modelClass.isAssignableFrom(TouchpadViewModel::class.java) ->
            TouchpadViewModel(navController) as T

        modelClass.isAssignableFrom(RecentDevicesViewModel::class.java) ->
            RecentDevicesViewModel(navController, database, preferences) as T

        modelClass.isAssignableFrom(RecentDeviceViewModel::class.java) ->
            RecentDeviceViewModel(database, navController, preferences) as T

        modelClass.isAssignableFrom(DeviceSearchViewModel::class.java) ->
            DeviceSearchViewModel(nsdHelper, navController, database, preferences) as T

        modelClass.isAssignableFrom(SubsPriceListViewModel::class.java) ->
            SubsPriceListViewModel(navController) as T

        modelClass.isAssignableFrom(SubsInfoViewModel::class.java) ->
            SubsInfoViewModel(navController) as T

        modelClass.isAssignableFrom(SubsTariffPlansViewModel::class.java) ->
            SubsTariffPlansViewModel(navController) as T

        modelClass.isAssignableFrom(SubsPaymentViewModel::class.java) ->
            SubsPaymentViewModel(navController) as T

        modelClass.isAssignableFrom(RecsDeepViewModel::class.java) ->
            RecsDeepViewModel(navController, database) as T

        modelClass.isAssignableFrom(ChannelsDeepViewModel::class.java) ->
            ChannelsDeepViewModel(navController, database) as T

        modelClass.isAssignableFrom(AppsDeepViewModel::class.java) ->
            AppsDeepViewModel(navController, cache, database) as T

        modelClass.isAssignableFrom(DirectoriesViewModel::class.java) ->
            DirectoriesViewModel(navController, uPnPManager) as T

        modelClass.isAssignableFrom(GalleryViewModel::class.java) ->
            GalleryViewModel(uPnPManager) as T

        modelClass.isAssignableFrom(MediaViewModel::class.java) ->
            MediaViewModel(navController, uPnPManager) as T

        modelClass.isAssignableFrom(PlayerViewModel::class.java) ->
            PlayerViewModel(navController, uPnPManager) as T

        modelClass.isAssignableFrom(KiviCatalogViewModel::class.java) ->
            KiviCatalogViewModel(database, navController) as T

        modelClass.isAssignableFrom(KiviCatalogSeriesViewModel::class.java) ->
            KiviCatalogSeriesViewModel(database, navController) as T

        else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.canonicalName}")
    }
}