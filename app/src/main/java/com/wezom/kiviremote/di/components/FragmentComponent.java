package com.wezom.kiviremote.di.components;

import com.wezom.kiviremote.di.modules.FragmentModule;
import com.wezom.kiviremote.di.scopes.FragmentScope;
import com.wezom.kiviremote.presentation.home.apps.AppsFragment;
import com.wezom.kiviremote.presentation.home.devicesearch.DeviceSearchFragment;
import com.wezom.kiviremote.presentation.home.directories.DirectoriesFragment;
import com.wezom.kiviremote.presentation.home.gallery.GalleryFragment;
import com.wezom.kiviremote.presentation.home.main.MainFragment;
import com.wezom.kiviremote.presentation.home.media.MediaFragment;
import com.wezom.kiviremote.presentation.home.ports.PortsFragment;
import com.wezom.kiviremote.presentation.home.recentdevice.RecentDeviceFragment;
import com.wezom.kiviremote.presentation.home.recentdevices.RecentDevicesFragment;
import com.wezom.kiviremote.presentation.home.recentdevices.TvSettingsFragment;
import com.wezom.kiviremote.presentation.home.recommendations.RecommendationsFragment;
import com.wezom.kiviremote.presentation.home.recommendations.deep.RecsAppsDeepFragment;
import com.wezom.kiviremote.presentation.home.recommendations.deep.RecsChannelsDeepFragment;
import com.wezom.kiviremote.presentation.home.recommendations.deep.RecsMovieDeepFragment;
import com.wezom.kiviremote.presentation.home.remotecontrol.RemoteControlFragment;
import com.wezom.kiviremote.presentation.home.subscriptions.subs_info.SubsInfoFragment;
import com.wezom.kiviremote.presentation.home.subscriptions.subs_payment.SubsPaymentFragment;
import com.wezom.kiviremote.presentation.home.subscriptions.subs_price_list.SubsPriceListFragment;
import com.wezom.kiviremote.presentation.home.subscriptions.subs_tariff_plans.SubsTariffPlansFragment;
import com.wezom.kiviremote.presentation.home.touchpad.TouchpadFragment;

import dagger.Subcomponent;

@FragmentScope
@Subcomponent(modules = FragmentModule.class)
public interface FragmentComponent {

    void inject(DeviceSearchFragment deviceSearchFragment);

    void inject(RemoteControlFragment remoteControlFragment);

    void inject(TouchpadFragment touchpadFragment);

    void inject(MainFragment mainFragment);

    void inject(AppsFragment appsFragment);

    void inject(TvSettingsFragment tvSettingsFragment);

    void inject(RecentDevicesFragment devicesFragment);

    void inject(RecentDeviceFragment recentDeviceFragment);

    void inject(MediaFragment mediaFragment);

    void inject(GalleryFragment galleryFragment);

    void inject(DirectoriesFragment directoriesFragment);

    void inject(RecommendationsFragment recommendationsFragment);

    void inject(PortsFragment portsFragment);

    void inject(RecsMovieDeepFragment recsMovieDeepFragment);

    void inject(RecsChannelsDeepFragment recsChannelsDeepFragment);

    void inject(RecsAppsDeepFragment recsAppsDeepFragment);

    void inject(SubsPriceListFragment subscriptionPriceListFragment);

    void inject(SubsInfoFragment subsInfoFragment);

    void inject(SubsTariffPlansFragment subsTariffPlansFragment);

    void inject(SubsPaymentFragment subsPaymentFragment);
}
