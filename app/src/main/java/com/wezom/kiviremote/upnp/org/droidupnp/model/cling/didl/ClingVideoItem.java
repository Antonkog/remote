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

import org.fourthline.cling.support.model.Res;
import org.fourthline.cling.support.model.item.VideoItem;

import java.util.List;

public class ClingVideoItem extends ClingDIDLItem {
    public ClingVideoItem(VideoItem item) {
        super(item);
    }

    @Override
    public String getDataType() {
        return "video/*";
    }

    @Override
    public String getDescription() {
        List<Res> res = item.getResources();
        if (res != null && res.size() > 0)
            return "" + ((res.get(0).getResolution() != null) ? res.get(0).getResolution() : "");

        return "";
    }

    @Override
    public String getCount() {
        List<Res> res = item.getResources();
        if (res != null && res.size() > 0)
            return "" + ((res.get(0).getDuration() != null) ? res.get(0).getDuration().split("\\.")[0] : "");

        return "";
    }

}
