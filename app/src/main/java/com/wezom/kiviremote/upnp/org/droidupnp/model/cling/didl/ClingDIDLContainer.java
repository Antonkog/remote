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

package com.wezom.kiviremote.upnp.org.droidupnp.model.cling.didl;

import com.wezom.kiviremote.upnp.org.droidupnp.model.upnp.didl.IDIDLContainer;

import org.fourthline.cling.support.model.container.Container;

public class ClingDIDLContainer extends ClingDIDLObject implements IDIDLContainer {

    public ClingDIDLContainer(Container item) {
        super(item);
    }

    public String getCount() {
        return "" + getChildCount();
    }

    @Override
    public int getChildCount() {
        if (item == null || !(item instanceof Container))
            return 0;

        Integer i = ((Container) item).getChildCount();

        if (i == null)
            return 0;

        return i;
    }
}
