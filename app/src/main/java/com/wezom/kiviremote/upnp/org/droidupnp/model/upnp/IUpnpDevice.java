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

package com.wezom.kiviremote.upnp.org.droidupnp.model.upnp;

import org.fourthline.cling.model.meta.Device;

public interface IUpnpDevice {

    String getDisplayString();

    String getFriendlyName();

    String getExtendedInformation();

    String getManufacturer();

    String getManufacturerURL();

    String getModelName();

    String getModelDesc();

    String getModelNumber();

    String getModelURL();

    String getXMLURL();

    String getPresentationURL();

    String getSerialNumber();

    String getUDN();

    boolean equals(IUpnpDevice otherDevice);

    String getUID();

    boolean asService(String service);

    void printService();

    boolean isFullyHydrated();

    Device getDevice();

    @Override
    String toString();
}
