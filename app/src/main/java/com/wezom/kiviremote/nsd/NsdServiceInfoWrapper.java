package com.wezom.kiviremote.nsd;

import android.net.nsd.NsdServiceInfo;

/**
 * Created by andre on 25.05.2017.
 */

public class NsdServiceInfoWrapper {
    private NsdServiceInfo service;
    private String serviceName;

    NsdServiceInfoWrapper(NsdServiceInfo service) {
        this(service, service.getServiceName());
    }

    public NsdServiceInfoWrapper(NsdServiceInfo service, String serviceName) {
        this.service = service;
        this.serviceName = serviceName;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null)
            return false;

        if (this == object) {
            return true;
        }

        NsdServiceInfoWrapper serviceInfoWrapper = (NsdServiceInfoWrapper) object;
        return service.getServiceName().equals(serviceInfoWrapper.service.getServiceName());
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + (this.service.getServiceName() != null ? this.service.getServiceName().hashCode() : 0);
        return hash;
    }

    public NsdServiceInfo getService() {
        return service;
    }

    public String getServiceName() {
        return serviceName;
    }

    public NsdServiceInfoWrapper setServiceName(String serviceName) {
        this.serviceName = serviceName;
        return this;
    }
}
