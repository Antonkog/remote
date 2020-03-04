package com.kivi.remote;


import com.kivi.remote.common.PreferencesManager;
import com.kivi.remote.di.components.ApplicationComponent;
import com.kivi.remote.di.components.DaggerApplicationComponent;
import com.kivi.remote.di.modules.ApplicationModule;
import com.kivi.remote.di.modules.CiceroneModule;
import com.kivi.remote.kivi_catalog.Constants;

import androidx.multidex.MultiDexApplication;
import ru.terrakok.cicerone.Cicerone;
import timber.log.Timber;

/**
 * Created by andre on 19.05.2017.
 */
public class App extends MultiDexApplication {
    private ApplicationComponent appComponent;
//    private Thread.UncaughtExceptionHandler mDefaultUEH;
//    private Thread.UncaughtExceptionHandler mCaughtExceptionHandler =
//            new Thread.UncaughtExceptionHandler() {
//                @Override
//                public void uncaughtException(Thread thread, Throwable ex) {
//                    // Custom logic goes here
//                    com.kivi.remote.common.FileUtilsKt.appendLog("thread \n" + thread.getName() + " message \n" + ex.toString());
//                    PreferencesManager.INSTANCE.incrementCrashCounter();
//                    // This will make Crashlytics do its job
//                    mDefaultUEH.uncaughtException(thread, ex);
//                }
//            };

    public ApplicationComponent getApplicationComponent() {
        return appComponent;
    }


    @Override
    public void onCreate() {
        super.onCreate();

//        mDefaultUEH = Thread.getDefaultUncaughtExceptionHandler();
//        Thread.setDefaultUncaughtExceptionHandler(mCaughtExceptionHandler);

        Constants.updateAppVersion().subscribe((integer, throwable) -> {
        });

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }

        appComponent = DaggerApplicationComponent
                .builder()
                .applicationModule(new ApplicationModule(this))
                .ciceroneModule(new CiceroneModule(Cicerone.create()))
                .build();

        PreferencesManager.INSTANCE.incrementOnAppLaunch();
    }

    public static boolean isDarkMode() {
        return PreferencesManager.INSTANCE.getDarkMode();
    }
}




