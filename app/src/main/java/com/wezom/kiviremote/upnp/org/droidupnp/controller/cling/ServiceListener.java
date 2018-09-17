/**
 * Copyright (C) 2013 Aurélien Chabot <aurelien@chabot.fr>
 * <p>
 * This file is part of DroidUPNP.
 * <p>
 * DroidUPNP is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * DroidUPNP is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with DroidUPNP.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.wezom.kiviremote.upnp.org.droidupnp.controller.cling;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;

import com.wezom.kiviremote.common.Utils;
import com.wezom.kiviremote.upnp.org.droidupnp.model.cling.CDevice;
import com.wezom.kiviremote.upnp.org.droidupnp.model.cling.CRegistryListener;
import com.wezom.kiviremote.upnp.org.droidupnp.model.mediaserver.ContentDirectoryService;
import com.wezom.kiviremote.upnp.org.droidupnp.model.mediaserver.MediaServer;
import com.wezom.kiviremote.upnp.org.droidupnp.model.upnp.ICallableFilter;
import com.wezom.kiviremote.upnp.org.droidupnp.model.upnp.IRegistryListener;
import com.wezom.kiviremote.upnp.org.droidupnp.model.upnp.IServiceListener;
import com.wezom.kiviremote.upnp.org.droidupnp.model.upnp.IUpnpDevice;

import org.fourthline.cling.android.AndroidUpnpService;
import org.fourthline.cling.controlpoint.ControlPoint;
import org.fourthline.cling.model.ValidationException;
import org.fourthline.cling.model.meta.Device;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

import timber.log.Timber;

@SuppressWarnings("rawtypes")
public class ServiceListener implements IServiceListener {

    protected AndroidUpnpService upnpService;
    protected ArrayList<IRegistryListener> waitingListener;

    private MediaServer mediaServer = null;
    private Context ctx = null;

    public ServiceListener(Context ctx) {
        waitingListener = new ArrayList<>();
        this.ctx = ctx;
    }

    @Override
    public void initContent() {
        if (mediaServer != null)
            mediaServer.initContent();
    }

    @Override
    public void refresh() {
        upnpService.getControlPoint().search();
    }

    @Override
    public ControlPoint getControlPoint() {
        return upnpService.getControlPoint();
    }

    @Override
    public Collection<IUpnpDevice> getDeviceList() {
        ArrayList<IUpnpDevice> deviceList = new ArrayList<>();
        if (upnpService != null && upnpService.getRegistry() != null) {
            for (Device device : upnpService.getRegistry().getDevices()) {
                deviceList.add(new CDevice(device));
            }
        }
        return deviceList;
    }

    @Override
    public Collection<IUpnpDevice> getFilteredDeviceList(ICallableFilter filter) {
        ArrayList<IUpnpDevice> deviceList = new ArrayList<>();
        try {
            if (upnpService != null && upnpService.getRegistry() != null) {
                for (Device device : upnpService.getRegistry().getDevices()) {
                    IUpnpDevice upnpDevice = new CDevice(device);
                    filter.setDevice(upnpDevice);

                    if (filter.call())
                        deviceList.add(upnpDevice);
                }
            }
        } catch (Exception e) {
            Timber.e(e, e.getMessage());
        }
        return deviceList;
    }

    protected ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            Timber.i("Service connexion");

            upnpService = (AndroidUpnpService) service;

            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ctx);
            if (sharedPref.getBoolean(ContentDirectoryService.CONTENTDIRECTORY_SERVICE, true)) {
                try {
                    // Local content directory
                    if (mediaServer == null) {
                        mediaServer = new MediaServer(new Random().nextInt(49151 - 1024) + 1024, Utils.getLocalIpAddress(ctx), ctx);
                        mediaServer.start();
                    } else {
                        mediaServer.restart();
                    }
                    upnpService.getRegistry().addDevice(mediaServer.getDevice());
                } catch (UnknownHostException | ValidationException e1) {
                    Timber.e(e1, "Creating demo device failed");
                    Timber.e(e1, "exception " + e1.getMessage());
                } catch (IOException e3) {
                    Timber.e(e3, "Starting http server failed");
                    Timber.e(e3, "exception", e3);
                }
            } else if (mediaServer != null) {
                mediaServer.stop();
                mediaServer = null;
            }

            for (IRegistryListener registryListener : waitingListener) {
                addListenerSafe(registryListener);
            }

            // Search asynchronously for all devices, they will respond soon
            upnpService.getControlPoint().search();
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            Timber.i("Service disconnected");
            upnpService = null;
        }
    };

    @Override
    public ServiceConnection getServiceConnexion() {
        return serviceConnection;
    }

    public AndroidUpnpService getUpnpService() {
        return upnpService;
    }

    @Override
    public void addListener(IRegistryListener registryListener) {
        Timber.d("Add Listener !");
        if (upnpService != null)
            addListenerSafe(registryListener);
        else
            waitingListener.add(registryListener);
    }

    private void addListenerSafe(IRegistryListener registryListener) {
        assert upnpService != null;
        Timber.d("Add Listener Safe !");

        // Get ready for future device advertisements
        upnpService.getRegistry().addListener(new CRegistryListener(registryListener));

        // Now add all devices to the list we already know about
        for (Device device : upnpService.getRegistry().getDevices()) {
            registryListener.deviceAdded(new CDevice(device));
        }
    }

    @Override
    public void removeListener(IRegistryListener registryListener) {
        Timber.d("remove listener");
        if (upnpService != null)
            removeListenerSafe(registryListener);
        else
            waitingListener.remove(registryListener);
    }

    private void removeListenerSafe(IRegistryListener registryListener) {
        assert upnpService != null;
        Timber.d("remove listener Safe");
        upnpService.getRegistry().removeListener(new CRegistryListener(registryListener));
    }

    @Override
    public void clearListener() {
        waitingListener.clear();
    }

    @Override
    public String getGeneratedSerial() {
        return mediaServer.getGeneratedSerial();
    }
}
