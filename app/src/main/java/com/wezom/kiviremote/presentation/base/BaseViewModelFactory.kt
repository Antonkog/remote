@file:Suppress("unchecked_cast")

package com.wezom.kiviremote.presentation.base

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.content.SharedPreferences
import com.wezom.kiviremote.common.KiviCache
import com.wezom.kiviremote.common.ResourceProvider
import com.wezom.kiviremote.di.scopes.ActivityScope
import com.wezom.kiviremote.nsd.NsdHelper
import com.wezom.kiviremote.persistence.AppDatabase
import com.wezom.kiviremote.presentation.home.HomeActivityViewModel
import com.wezom.kiviremote.presentation.home.apps.AppsViewModel
import com.wezom.kiviremote.presentation.home.devicesearch.DeviceSearchViewModel
import com.wezom.kiviremote.presentation.home.directories.DirectoriesViewModel
import com.wezom.kiviremote.presentation.home.gallery.GalleryViewModel
import com.wezom.kiviremote.presentation.home.main.MainFragmentViewModel
import com.wezom.kiviremote.presentation.home.media.MediaViewModel
import com.wezom.kiviremote.presentation.home.ports.PortsViewModel
import com.wezom.kiviremote.presentation.home.recentdevice.RecentDeviceViewModel
import com.wezom.kiviremote.presentation.home.recentdevices.RecentDevicesViewModel
import com.wezom.kiviremote.presentation.home.recommendations.RecommendationsViewModel
import com.wezom.kiviremote.presentation.home.recommendations.deep.AppsDeepViewModel
import com.wezom.kiviremote.presentation.home.recommendations.deep.ChannelsDeepViewModel
import com.wezom.kiviremote.presentation.home.recommendations.deep.RecsDeepViewModel
import com.wezom.kiviremote.presentation.home.remotecontrol.RemoteControlViewModel
import com.wezom.kiviremote.presentation.home.subscriptions.subs_info.SubsInfoViewModel
import com.wezom.kiviremote.presentation.home.subscriptions.subs_payment.SubsPaymentViewModel
import com.wezom.kiviremote.presentation.home.subscriptions.subs_price_list.SubsPriceListViewModel
import com.wezom.kiviremote.presentation.home.subscriptions.subs_tariff_plans.SubsTariffPlansViewModel
import com.wezom.kiviremote.presentation.home.touchpad.TouchpadViewModel
import com.wezom.kiviremote.presentation.home.tvsettings.TvSettingsViewModel
import com.wezom.kiviremote.upnp.UPnPManager
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
            HomeActivityViewModel(database, navigatorHolder, cache, router, uPnPManager, preferences) as T

        modelClass.isAssignableFrom(RecentDeviceViewModel::class.java) ->
            RecentDeviceViewModel(database, router) as T

        modelClass.isAssignableFrom(MainFragmentViewModel::class.java) ->
            MainFragmentViewModel(router, uPnPManager) as T

        modelClass.isAssignableFrom(RemoteControlViewModel::class.java) ->
            RemoteControlViewModel(router) as T


        modelClass.isAssignableFrom(RecommendationsViewModel::class.java) ->
            RecommendationsViewModel(router, database, cache, preferences, uPnPManager) as T

        modelClass.isAssignableFrom(TouchpadViewModel::class.java) ->
            TouchpadViewModel(router) as T

        modelClass.isAssignableFrom(AppsViewModel::class.java) ->
            AppsViewModel(database, cache, preferences, resourceProvider) as T

        modelClass.isAssignableFrom(MediaViewModel::class.java) ->
            MediaViewModel(router, uPnPManager) as T

        modelClass.isAssignableFrom(GalleryViewModel::class.java) ->
            GalleryViewModel(uPnPManager) as T

        modelClass.isAssignableFrom(RecentDevicesViewModel::class.java) ->
            RecentDevicesViewModel(router, database, nsdHelper) as T

        modelClass.isAssignableFrom(TvSettingsViewModel::class.java) ->
            TvSettingsViewModel(router) as T

        modelClass.isAssignableFrom(DeviceSearchViewModel::class.java) ->
            DeviceSearchViewModel(nsdHelper, router, database) as T

        modelClass.isAssignableFrom(DirectoriesViewModel::class.java) ->
            DirectoriesViewModel(router, uPnPManager) as T

        modelClass.isAssignableFrom(PortsViewModel::class.java) ->
            PortsViewModel(router) as T

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

        else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.canonicalName}")
    }
}