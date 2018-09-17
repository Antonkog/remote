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

import com.wezom.kiviremote.R;
import com.wezom.kiviremote.upnp.org.droidupnp.model.upnp.didl.IDIDLItem;

import org.fourthline.cling.support.model.item.Item;

public class ClingDIDLItem extends ClingDIDLObject implements IDIDLItem {

    public ClingDIDLItem(Item item) {
        super(item);
    }

    @Override
    public int getIcon() {
        return R.drawable.ic_file;
    }

    @Override
    public String getURI() {
        if (item != null) {
            if (item.getFirstResource() != null && item.getFirstResource().getValue() != null)
                return item.getFirstResource().getValue();
        }
        return null;
    }
}
