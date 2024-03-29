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

import android.content.ServiceConnection;

import org.fourthline.cling.controlpoint.ControlPoint;

import java.util.Collection;

public interface IServiceListener {

    void addListener(IRegistryListener registryListener);

    void removeListener(IRegistryListener registryListener);

    void clearListener();

    void refresh();

    Collection<IUpnpDevice> getDeviceList();

    Collection<IUpnpDevice> getFilteredDeviceList(ICallableFilter filter);

    ServiceConnection getServiceConnexion();

    String getGeneratedSerial();

    ControlPoint getControlPoint();

    void initContent();
}
