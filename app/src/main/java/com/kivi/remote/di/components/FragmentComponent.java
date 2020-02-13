package com.kivi.remote.di.components;

import com.kivi.remote.di.modules.FragmentModule;
import com.kivi.remote.di.scopes.FragmentScope;
import com.kivi.remote.presentation.home.apps.AppsFragment;
import com.kivi.remote.presentation.home.devicesearch.DeviceSearchFragment;
import com.kivi.remote.presentation.home.directories.DirectoriesFragment;
import com.kivi.remote.presentation.home.gallery.GalleryFragment;
import com.kivi.remote.presentation.home.kivi_catalog.KiviCatalogFragment;
import com.kivi.remote.presentation.home.kivi_catalog.KiviCatalogSeriesFragment;
import com.kivi.remote.presentation.home.media.MediaFragment;
import com.kivi.remote.presentation.home.player.PlayerFragment;
import com.kivi.remote.presentation.home.recentdevice.RecentDeviceFragment;
import com.kivi.remote.presentation.home.recentdevices.RecentDevicesFragment;
import com.kivi.remote.presentation.home.recentdevices.TvSettingsFragment;
import com.kivi.remote.presentation.home.recommendations.RecommendationsFragment;
import com.kivi.remote.presentation.home.recommendations.deep.RecsAppsDeepFragment;
import com.kivi.remote.presentation.home.recommendations.deep.RecsChannelsDeepFragment;
import com.kivi.remote.presentation.home.recommendations.deep.RecsMovieDeepFragment;
import com.kivi.remote.presentation.home.subscriptions.subs_info.SubsInfoFragment;
import com.kivi.remote.presentation.home.subscriptions.subs_payment.SubsPaymentFragment;
import com.kivi.remote.presentation.home.subscriptions.subs_price_list.SubsPriceListFragment;
import com.kivi.remote.presentation.home.subscriptions.subs_tariff_plans.SubsTariffPlansFragment;
import com.kivi.remote.presentation.home.touchpad.TouchpadFragment;

import dagger.Subcomponent;

@FragmentScope
@Subcomponent(modules = FragmentModule.class)
public interface FragmentComponent {

    void inject(DeviceSearchFragment deviceSearchFragment);

    void inject(TouchpadFragment touchpadFragment);

    void inject(AppsFragment appsFragment);

    void inject(TvSettingsFragment tvSettingsFragment);

    void inject(RecentDevicesFragment devicesFragment);

    void inject(RecentDeviceFragment recentDeviceFragment);

    void inject(PlayerFragment playerFragment);

    void inject(MediaFragment mediaFragment);

    void inject(GalleryFragment galleryFragment);

    void inject(DirectoriesFragment directoriesFragment);

    void inject(RecommendationsFragment recommendationsFragment);

    void inject(RecsMovieDeepFragment recsMovieDeepFragment);

    void inject(RecsChannelsDeepFragment recsChannelsDeepFragment);

    void inject(RecsAppsDeepFragment recsAppsDeepFragment);

    void inject(SubsPriceListFragment subscriptionPriceListFragment);

    void inject(SubsInfoFragment subsInfoFragment);

    void inject(SubsTariffPlansFragment subsTariffPlansFragment);

    void inject(SubsPaymentFragment subsPaymentFragment);

    void inject(KiviCatalogFragment kiviCatalogFragment);

    void inject(KiviCatalogSeriesFragment kiviCatalogSeriesFragment);

}