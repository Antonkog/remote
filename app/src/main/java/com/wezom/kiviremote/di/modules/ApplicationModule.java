package com.wezom.kiviremote.di.modules;

import android.app.ActivityManager;
import android.app.Application;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.wezom.kiviremote.common.KiviCache;
import com.wezom.kiviremote.common.PreferencesManager;
import com.wezom.kiviremote.common.ResourceProvider;
import com.wezom.kiviremote.di.scopes.ApplicationScope;
import com.wezom.kiviremote.nsd.NsdHelper;
import com.wezom.kiviremote.persistence.AppDatabase;
import com.wezom.kiviremote.upnp.UPnPManager;
import com.wezom.kiviremote.upnp.org.droidupnp.controller.upnp.IUPnPServiceController;
import com.wezom.kiviremote.upnp.org.droidupnp.model.upnp.IFactory;

import dagger.Module;
import dagger.Provides;

@Module(includes = {CiceroneModule.class, UPnPModule.class})
public class ApplicationModule {
    private Application application;

    public ApplicationModule(Application application) {
        this.application = application;
        PreferencesManager.INSTANCE.init(application);
    }

    @Provides
    @ApplicationScope
    Context provideApplicationContext() {
        return application;
    }

    @Provides
    @ApplicationScope
    Application provideApplication() {
        return application;
    }

    @Provides
    @ApplicationScope
    static AppDatabase provideDatabase(Context context) {
        return Room.databaseBuilder(context, AppDatabase.class, "kivi-db")
                .fallbackToDestructiveMigration()
                .build();
    }

    @Provides
    @ApplicationScope
    static KiviCache provideKiviCache(Context context) {
        return new KiviCache(getCacheSize(context));
    }

    private static int getCacheSize(Context context) {
        return (((ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE))
                .getMemoryClass() * 1024) / 10;
    }

    @Provides
    @ApplicationScope
    static SharedPreferences provideSharedPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Provides
    @ApplicationScope
    static NsdHelper provideNsdManager(Context context) {
        return new NsdHelper(context);
    }

    @Provides
    @ApplicationScope
    static PreferencesManager providePreferencesManager() {
        return PreferencesManager.INSTANCE;
    }

    @Provides
    @ApplicationScope
    static UPnPManager provideManager(IUPnPServiceController controller, IFactory factory, SharedPreferences preferences) {
        return new UPnPManager(controller, factory, preferences);
    }

    @Provides
    @ApplicationScope
    static ResourceProvider provideResourceProvider(Context context) {
        return new ResourceProvider(context);
    }
}
