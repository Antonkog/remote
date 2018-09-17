package com.wezom.kiviremote.di.components;


import com.wezom.kiviremote.di.modules.ActivityModule;
import com.wezom.kiviremote.di.modules.ApplicationModule;
import com.wezom.kiviremote.di.scopes.ApplicationScope;
import com.wezom.kiviremote.receivers.NetworkChangeReceiver;
import com.wezom.kiviremote.services.CleanupService;
import com.wezom.kiviremote.services.NotificationService;

import dagger.Component;

@ApplicationScope
@Component(modules = {ApplicationModule.class})
public interface ApplicationComponent {
    ActivityComponent provideActivityComponent(ActivityModule activityModule);

    void inject(NetworkChangeReceiver networkChangeReceiver);

    void inject(CleanupService service);

    void inject(NotificationService service);
}
