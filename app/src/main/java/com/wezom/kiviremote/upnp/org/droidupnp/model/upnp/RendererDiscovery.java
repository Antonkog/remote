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


import com.wezom.kiviremote.upnp.org.droidupnp.controller.upnp.IUPnPServiceController;

public class RendererDiscovery extends DeviceDiscovery {

    protected static final String TAG = "RendererDeviceFragment";

    public RendererDiscovery(IUPnPServiceController controller, IServiceListener serviceListener) {
        super(controller, serviceListener);
    }

    @Override
    protected ICallableFilter getCallableFilter() {
        return new CallableRendererFilter();
    }

    @Override
    protected boolean isSelected(IUpnpDevice device) {
        return controller != null
                && controller.getSelectedRenderer() != null
                && device.equals(controller.getSelectedRenderer());
    }

    @Override
    protected void select(IUpnpDevice device) {
        select(device, false);
    }

    @Override
    protected void select(IUpnpDevice device, boolean force) {
        controller.setSelectedRenderer(device, force);
    }

    @Override
    protected void removed(IUpnpDevice d) {
        if (controller != null && controller.getSelectedRenderer() != null
                && d.equals(controller.getSelectedRenderer()))
            controller.setSelectedRenderer(null);
    }
}
