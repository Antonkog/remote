package com.kivi.remote;


import android.content.Intent;
import android.net.Uri;

import com.bumptech.glide.request.target.ViewTarget;
import com.crashlytics.android.Crashlytics;
import com.kivi.remote.common.FileUtilsKt;
import com.kivi.remote.common.PreferencesManager;
import com.kivi.remote.di.components.ApplicationComponent;
import com.kivi.remote.di.components.DaggerApplicationComponent;
import com.kivi.remote.di.modules.ApplicationModule;
import com.kivi.remote.di.modules.CiceroneModule;
import com.kivi.remote.kivi_catalog.Constants;

import androidx.multidex.MultiDexApplication;
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
        Constants.updateAppVersion().subscribe((integer, throwable) -> { });

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

        sutupCrashHandler();
    }



    private void sutupCrashHandler() {
// Make myHandler the new default uncaught exception handler.
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                Timber.e(e);
                PreferencesManager.INSTANCE.incrementCrashCounter();
//                sendLogFile();
            }
        });
    }

//(4) Start an email app (also in my SendLog Activity):

    private void sendLogFile() {
        String fullName = FileUtilsKt.extractLogToFile(this);
        if (fullName == null)
            return;
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("plain/text");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"log@mydomain.com"});
        intent.putExtra(Intent.EXTRA_SUBJECT, "MyApp log file");
        intent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + fullName));
        intent.putExtra(Intent.EXTRA_TEXT, "Log file attached."); // do this so some email clients don't complain about empty body.
        startActivity(intent);
    }


    public static boolean isDarkMode() {
        return PreferencesManager.INSTANCE.getDarkMode();
    }}
