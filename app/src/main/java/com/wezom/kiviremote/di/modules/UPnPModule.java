package com.wezom.kiviremote.di.modules;


import com.wezom.kiviremote.di.scopes.ApplicationScope;
import com.wezom.kiviremote.upnp.org.droidupnp.controller.cling.Factory;
import com.wezom.kiviremote.upnp.org.droidupnp.controller.cling.ServiceController;
import com.wezom.kiviremote.upnp.org.droidupnp.controller.upnp.IUPnPServiceController;
import com.wezom.kiviremote.upnp.org.droidupnp.model.upnp.IFactory;

import dagger.Binds;
import dagger.Module;


@Module
public abstract class UPnPModule {
    @Binds
    @ApplicationScope
    public abstract IFactory bindFactory(Factory factory);

    @Binds
    @ApplicationScope
    public abstract IUPnPServiceController bindController(ServiceController controller);
}
