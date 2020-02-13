package com.kivi.remote.di.components;


import com.kivi.remote.di.modules.ActivityModule;
import com.kivi.remote.di.modules.ApplicationModule;
import com.kivi.remote.di.scopes.ApplicationScope;
import com.kivi.remote.receivers.NetworkChangeReceiver;
import com.kivi.remote.services.CleanupService;
import com.kivi.remote.services.NotificationService;

import dagger.Component;

@ApplicationScope
@Component(modules = {ApplicationModule.class})
public interface ApplicationComponent {
    ActivityComponent provideActivityComponent(ActivityModule activityModule);

    void inject(NetworkChangeReceiver networkChangeReceiver);

    void inject(CleanupService service);

    void inject(NotificationService service);
}
