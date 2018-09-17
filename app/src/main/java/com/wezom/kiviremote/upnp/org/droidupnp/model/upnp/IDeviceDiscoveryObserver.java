package com.wezom.kiviremote.upnp.org.droidupnp.model.upnp;

public interface IDeviceDiscoveryObserver {

    void addedDevice(IUpnpDevice device);

    void removedDevice(IUpnpDevice device);
}
