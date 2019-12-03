package com.wezom.kiviremote;


import androidx.multidex.MultiDexApplication;

import com.bumptech.glide.request.target.ViewTarget;
import com.crashlytics.android.Crashlytics;
import com.wezom.kiviremote.common.PreferencesManager;
import com.wezom.kiviremote.di.components.ApplicationComponent;
import com.wezom.kiviremote.di.components.DaggerApplicationComponent;
import com.wezom.kiviremote.di.modules.ApplicationModule;
import com.wezom.kiviremote.di.modules.CiceroneModule;

import io.fabric.sdk.android.Fabric;
import ru.terrakok.cicerone.Cicerone;
import timber.log.Timber;

/**
 * Created by andre on 19.05.2017.
 */
public class App extends MultiDexApplication {
    private ApplicationComponent appComponent;

    public ApplicationComponent getApplicationComponent() {
        return appComponent;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ViewTarget.setTagId(R.id.glide_tag);//deprecated in glide 4.8 (now 4.6)

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        } else {
            Fabric.with(this, new Crashlytics());
        }

        appComponent = DaggerApplicationComponent
                .builder()
                .applicationModule(new ApplicationModule(this))
                .ciceroneModule(new CiceroneModule(Cicerone.create()))
                .build();
    }

    public static boolean isDarkMode() {
        return  PreferencesManager.INSTANCE.getDarkMode();
    }
}
