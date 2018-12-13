package com.wezom.kiviremote.presentation.base;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.widget.Toast;

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
import com.wezom.kiviremote.presentation.home.main.MainFragment;
import com.wezom.kiviremote.presentation.home.ports.PortsFragment;
import com.wezom.kiviremote.presentation.home.recentdevice.RecentDeviceFragment;
import com.wezom.kiviremote.presentation.home.recentdevices.RecentDevicesFragment;
import com.wezom.kiviremote.presentation.home.recentdevices.TvSettingsFragment;
import com.wezom.kiviremote.presentation.home.remotecontrol.RemoteControlFragment;
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
                        case Screens.DEVICE_SEARCH_FRAGMENT:
                            return new DeviceSearchFragment();
                        case Screens.REMOTE_CONTROL_FRAGMENT:
                            return new RemoteControlFragment();
                        case Screens.TOUCH_PAD_FRAGMENT:
                            return new TouchpadFragment();
                        case Screens.MAIN_FRAGMENT:
                            return new MainFragment();
                        case Screens.RECENT_DEVICES_FRAGMENT:
                            return new RecentDevicesFragment();
                        case Screens.RECENT_DEVICE_FRAGMENT:
                            return RecentDeviceFragment.newInstance((RecentDevice) data);
                        case Screens.GALLERY_FRAGMENT:
                            return new GalleryFragment();
                        case Screens.DIRECTORIES_FRAGMENT:
                            return new DirectoriesFragment();
                        case Screens.TV_SETTINGS_FRAGMENT:
                            return new TvSettingsFragment();
                        case Screens.PORTS_FRAGMENT:
                            return new PortsFragment();
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
