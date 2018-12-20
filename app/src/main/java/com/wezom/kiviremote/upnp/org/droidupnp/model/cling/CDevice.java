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

package com.wezom.kiviremote.upnp.org.droidupnp.model.cling;

import com.wezom.kiviremote.upnp.org.droidupnp.model.upnp.IUpnpDevice;

import org.fourthline.cling.model.meta.Action;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.types.ServiceType;
import org.fourthline.cling.model.types.UDAServiceType;

import timber.log.Timber;

@SuppressWarnings("rawtypes")
public class CDevice implements IUpnpDevice {

    private static final String TAG = "ClingDevice";

    private Device device;

    public CDevice(Device device) {
        this.device = device;
    }

    @Override
    public Device getDevice() {
        return device;
    }

    @Override
    public String getDisplayString() {
        return device.getDisplayString();
    }

    @Override
    public String getFriendlyName() {
        return (device.getDetails() != null && device.getDetails().getFriendlyName() != null) ? device.getDetails()
                .getFriendlyName() : getDisplayString();
    }

    @Override
    public boolean equals(IUpnpDevice otherDevice) {
        if (getDevice() == null ||
                getDevice().getIdentity() == null ||
                getDevice().getIdentity().getUdn() == null ||
                otherDevice == null ||
                otherDevice.getDevice() == null ||
                otherDevice.getDevice().getIdentity() == null ||
                otherDevice.getDevice().getIdentity().getUdn() == null)
            return false;
        return getDevice().getIdentity().getUdn().equals(otherDevice.getDevice().getIdentity().getUdn());
    }

    @Override
    public String getUID() {
        return device.getIdentity().getUdn().toString();
    }

    @Override
    public String getExtendedInformation() {
        String info = "";
        if (device.findServiceTypes() != null)
            for (ServiceType cap : device.findServiceTypes()) {
                info += "\n\t" + cap.getType() + " : " + cap.toFriendlyString();
            }
        return info;
    }

    @Override
    public void printService() {
        Service[] services = device.findServices();
        for (Service service : services) {
            Timber.i( "\t Service : " + service);
            for (Action a : service.getActions()) {
                Timber.i( "\t\t Action : " + a);
            }
        }
    }

    @Override
    public boolean asService(String service) {
        return (device.findService(new UDAServiceType(service)) != null);
    }

    @Override
    public String getManufacturer() {
        return device.getDetails().getManufacturerDetails().getManufacturer();
    }

    @Override
    public String getManufacturerURL() {
        try {
            return device.getDetails().getManufacturerDetails().getManufacturerURI().toString();
        } catch (Exception e) {
            return "";
        }
    }

    @Override
    public String getModelName() {
        try {
            return device.getDetails().getModelDetails().getModelName();
        } catch (Exception e) {
            return "";
        }
    }

    @Override
    public String getModelDesc() {
        try {
            return device.getDetails().getModelDetails().getModelDescription();
        } catch (Exception e) {
            return "";
        }
    }

    @Override
    public String getModelNumber() {
        try {
            return device.getDetails().getModelDetails().getModelNumber();
        } catch (Exception e) {
            return "";
        }
    }

    @Override
    public String getModelURL() {
        try {
            return device.getDetails().getModelDetails().getModelURI().toString();
        } catch (Exception e) {
            return "";
        }
    }

    @Override
    public String getXMLURL() {
        try {
            return device.getDetails().getBaseURL().toString();
        } catch (Exception e) {
            return "";
        }
    }

    @Override
    public String getPresentationURL() {
        try {
            return device.getDetails().getPresentationURI().toString();
        } catch (Exception e) {
            return "";
        }
    }

    @Override
    public String getSerialNumber() {
        try {
            return device.getDetails().getSerialNumber();
        } catch (Exception e) {
            return "";
        }
    }

    @Override
    public String getUDN() {
        try {
            return device.getIdentity().getUdn().toString();
        } catch (Exception e) {
            return "";
        }
    }

    @Override
    public boolean isFullyHydrated() {
        return device.isFullyHydrated();
    }
}
