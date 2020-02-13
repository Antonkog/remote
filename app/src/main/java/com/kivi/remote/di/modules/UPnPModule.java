package com.kivi.remote.di.modules;


import com.kivi.remote.di.scopes.ApplicationScope;
import com.kivi.remote.upnp.org.droidupnp.controller.cling.Factory;
import com.kivi.remote.upnp.org.droidupnp.controller.cling.ServiceController;
import com.kivi.remote.upnp.org.droidupnp.controller.upnp.IUPnPServiceController;
import com.kivi.remote.upnp.org.droidupnp.model.upnp.IFactory;

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
