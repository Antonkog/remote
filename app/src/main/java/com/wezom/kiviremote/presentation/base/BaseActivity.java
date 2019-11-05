package com.wezom.kiviremote.presentation.base;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.wezom.kiviremote.App;
import com.wezom.kiviremote.R;
import com.wezom.kiviremote.Screens;
import com.wezom.kiviremote.common.Utils;
import com.wezom.kiviremote.di.components.ActivityComponent;
import com.wezom.kiviremote.di.components.ApplicationComponent;
import com.wezom.kiviremote.di.modules.ActivityModule;
import com.wezom.kiviremote.navigation.SupportFragmentNavigator;
import com.wezom.kiviremote.persistence.model.RecentDevice;
import com.wezom.kiviremote.presentation.home.devicesearch.DeviceSearchFragment;
import com.wezom.kiviremote.presentation.home.directories.DirectoriesFragment;
import com.wezom.kiviremote.presentation.home.gallery.GalleryFragment;
import com.wezom.kiviremote.presentation.home.media.MediaFragment;
import com.wezom.kiviremote.presentation.home.recentdevice.RecentDeviceFragment;
import com.wezom.kiviremote.presentation.home.recentdevices.RecentDevicesFragment;
import com.wezom.kiviremote.presentation.home.recommendations.RecommendationsFragment;
import com.wezom.kiviremote.presentation.home.recommendations.deep.RecsAppsDeepFragment;
import com.wezom.kiviremote.presentation.home.recommendations.deep.RecsChannelsDeepFragment;
import com.wezom.kiviremote.presentation.home.recommendations.deep.RecsMovieDeepFragment;
import com.wezom.kiviremote.presentation.home.subscriptions.subs_info.SubsInfoFragment;
import com.wezom.kiviremote.presentation.home.subscriptions.subs_payment.SubsPaymentFragment;
import com.wezom.kiviremote.presentation.home.subscriptions.subs_price_list.PricePerTime;
import com.wezom.kiviremote.presentation.home.subscriptions.subs_tariff_plans.SubsTariffPlansFragment;
import com.wezom.kiviremote.presentation.home.touchpad.TouchpadFragment;

import ru.terrakok.cicerone.Navigator;
import ru.terrakok.cicerone.commands.Command;

import static com.wezom.kiviremote.navigation.AnimationType.FADE_ANIM;

public abstract class BaseActivity extends AppCompatActivity {
    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }


    protected Navigator baseNavigator =
            new SupportFragmentNavigator(getSupportFragmentManager(),
                    R.id.activity_home_container,
                    FADE_ANIM) {
                @Override
                public void applyCommands(Command[] commands) {
                    super.applyCommands(commands);
                }

                @Override
                protected Fragment createFragment(String screenKey, Object data) {
                    Utils.hideKeyboard(BaseActivity.this);
                    switch (screenKey) {
                        case Screens.TOUCH_PAD_FRAGMENT:
                            return new TouchpadFragment();
                        case Screens.DEVICE_SEARCH_FRAGMENT:
                            return new DeviceSearchFragment();
                        case Screens.RECOMMENDATIONS_FRAGMENT:
                            return new RecommendationsFragment();
                        case Screens.RECS_APPS_DEEP_FRAGMENT:
                            return new RecsAppsDeepFragment();
                        case Screens.RECS_MOVIE_DEEP_FRAGMENT:
                            return new RecsMovieDeepFragment();
                        case Screens.RECS_CHANNELS_DEEP_FRAGMENT:
                            return new RecsChannelsDeepFragment();
                        case Screens.MEDIA_FRAGMENT:
                            return new MediaFragment();
                        case Screens.GALLERY_FRAGMENT:
                            return new GalleryFragment();
                        case Screens.DIRECTORIES_FRAGMENT:
                            return new DirectoriesFragment();
                        case Screens.RECENT_DEVICES_FRAGMENT:
                            return new RecentDevicesFragment();
                        case Screens.RECENT_DEVICE_FRAGMENT:
                            return RecentDeviceFragment.newInstance((RecentDevice) data);
                        case Screens.SUBS_PRICE_LIST_FRAGMENT:
                            return new RecsMovieDeepFragment();
                        case Screens.SUBS_INFO_FRAGMENT:
                            return SubsInfoFragment.newInstance((PricePerTime) data);
                        case Screens.SUBS_TARIFF_PLANS_FRAGMENT:
                            return new SubsTariffPlansFragment();
                        case Screens.SUBS_PAYMENT_FRAGMENT:
                            return new SubsPaymentFragment();
                        default:
                            throw new IllegalStateException("Unknown screen");
                    }
                }

                @Override
                protected void showSystemMessage(String message) {
                    Toast.makeText(BaseActivity.this, message, Toast.LENGTH_SHORT).show();
                }

                @Override
                protected void exit() {
                    finish();
                }
            };

    private ActivityComponent activityComponent;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        activityComponent = getApplicationComponent().provideActivityComponent(new ActivityModule(this));
        super.onCreate(savedInstanceState);
        injectDependency();
    }

    ApplicationComponent getApplicationComponent() {
        return ((App) getApplication()).getApplicationComponent();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public abstract void injectDependency();

    public ActivityComponent getActivityComponent() {
        return activityComponent;
    }

    @Override
    public void onBackPressed() {
        Utils.hideKeyboard(this);
        super.onBackPressed();
    }


}
