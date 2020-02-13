package com.kivi.remote.upnp.org.droidupnp.model.upnp;

public interface IDeviceDiscoveryObserver {

    void addedDevice(IUpnpDevice device);

    void removedDevice(IUpnpDevice device);
}
