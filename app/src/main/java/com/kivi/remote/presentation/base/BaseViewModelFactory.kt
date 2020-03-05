@file:Suppress("unchecked_cast")

package com.kivi.remote.presentation.base

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
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
import ru.terrakok.cicerone.NavigatorHolder
import ru.terrakok.cicerone.Router
import javax.inject.Inject

@ActivityScope
class BaseViewModelFactory @Inject constructor(private val database: AppDatabase,
                                               private val cache: KiviCache,
                                               private val router: Router,
                                               private val uPnPManager: UPnPManager,
                                               private val navigatorHolder: NavigatorHolder,
                                               private val nsdHelper: NsdHelper,
                                               private val preferences: SharedPreferences,
                                               private val resourceProvider: ResourceProvider) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T = when {
        modelClass.isAssignableFrom(HomeActivityViewModel::class.java) ->
            HomeActivityViewModel(database, navigatorHolder, cache, router, preferences) as T

        modelClass.isAssignableFrom(RecommendationsViewModel::class.java) ->
            RecommendationsViewModel(router, database, cache, preferences) as T

        modelClass.isAssignableFrom(TouchpadViewModel::class.java) ->
            TouchpadViewModel(router) as T

        modelClass.isAssignableFrom(RecentDevicesViewModel::class.java) ->
            RecentDevicesViewModel(router, database, preferences) as T

        modelClass.isAssignableFrom(RecentDeviceViewModel::class.java) ->
            RecentDeviceViewModel(database, router, preferences) as T

        modelClass.isAssignableFrom(DeviceSearchViewModel::class.java) ->
            DeviceSearchViewModel(nsdHelper, router, database, preferences) as T

        modelClass.isAssignableFrom(SubsPriceListViewModel::class.java) ->
            SubsPriceListViewModel(router) as T

        modelClass.isAssignableFrom(SubsInfoViewModel::class.java) ->
            SubsInfoViewModel(router) as T

        modelClass.isAssignableFrom(SubsTariffPlansViewModel::class.java) ->
            SubsTariffPlansViewModel(router) as T

        modelClass.isAssignableFrom(SubsPaymentViewModel::class.java) ->
            SubsPaymentViewModel(router) as T

        modelClass.isAssignableFrom(RecsDeepViewModel::class.java) ->
            RecsDeepViewModel(router, database) as T

        modelClass.isAssignableFrom(ChannelsDeepViewModel::class.java) ->
            ChannelsDeepViewModel(router, database) as T

        modelClass.isAssignableFrom(AppsDeepViewModel::class.java) ->
            AppsDeepViewModel(router, cache, database) as T

        modelClass.isAssignableFrom(DirectoriesViewModel::class.java) ->
            DirectoriesViewModel(router, uPnPManager) as T

        modelClass.isAssignableFrom(GalleryViewModel::class.java) ->
            GalleryViewModel(uPnPManager) as T

        modelClass.isAssignableFrom(MediaViewModel::class.java) ->
            MediaViewModel(router, uPnPManager) as T

        modelClass.isAssignableFrom(PlayerViewModel::class.java) ->
            PlayerViewModel(router, uPnPManager) as T

        modelClass.isAssignableFrom(KiviCatalogViewModel::class.java) ->
            KiviCatalogViewModel(database, router) as T

        modelClass.isAssignableFrom(KiviCatalogSeriesViewModel::class.java) ->
            KiviCatalogSeriesViewModel(database, router) as T

        else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.canonicalName}")
    }
}