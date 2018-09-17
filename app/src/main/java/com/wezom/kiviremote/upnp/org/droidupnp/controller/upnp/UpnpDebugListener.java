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

package com.wezom.kiviremote.upnp.org.droidupnp.controller.upnp;

import com.wezom.kiviremote.upnp.org.droidupnp.model.upnp.IRegistryListener;
import com.wezom.kiviremote.upnp.org.droidupnp.model.upnp.IUpnpDevice;

import timber.log.Timber;


public class UpnpDebugListener implements IRegistryListener {

    protected static final String TAG = "ClingDebugListener";

    @Override
    public void deviceAdded(final IUpnpDevice device) {
        Timber.i("New device detected : " + device.getDisplayString());
    }

    @Override
    public void deviceRemoved(final IUpnpDevice device) {
        Timber.i("Device removed : " + device.getDisplayString());
    }
}