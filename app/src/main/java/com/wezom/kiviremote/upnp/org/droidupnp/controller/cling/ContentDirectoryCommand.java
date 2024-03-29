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

import com.wezom.kiviremote.upnp.ContentCallback;
import com.wezom.kiviremote.upnp.org.droidupnp.controller.upnp.IUPnPServiceController;
import com.wezom.kiviremote.upnp.org.droidupnp.model.cling.didl.ClingAudioItem;
import com.wezom.kiviremote.upnp.org.droidupnp.model.cling.didl.ClingDIDLContainer;
import com.wezom.kiviremote.upnp.org.droidupnp.model.cling.didl.ClingDIDLItem;
import com.wezom.kiviremote.upnp.org.droidupnp.model.cling.didl.ClingDIDLParentContainer;
import com.wezom.kiviremote.upnp.org.droidupnp.model.cling.didl.ClingImageItem;
import com.wezom.kiviremote.upnp.org.droidupnp.model.cling.didl.ClingVideoItem;
import com.wezom.kiviremote.upnp.org.droidupnp.model.upnp.IContentDirectoryCommand;
import com.wezom.kiviremote.upnp.org.droidupnp.view.DIDLObjectDisplay;

import org.fourthline.cling.controlpoint.ControlPoint;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.types.UDAServiceType;
import org.fourthline.cling.support.contentdirectory.callback.Browse;
import org.fourthline.cling.support.contentdirectory.callback.Search;
import org.fourthline.cling.support.model.BrowseFlag;
import org.fourthline.cling.support.model.DIDLContent;
import org.fourthline.cling.support.model.DIDLObject;
import org.fourthline.cling.support.model.SortCriterion;
import org.fourthline.cling.support.model.container.Container;
import org.fourthline.cling.support.model.item.AudioItem;
import org.fourthline.cling.support.model.item.ImageItem;
import org.fourthline.cling.support.model.item.Item;
import org.fourthline.cling.support.model.item.VideoItem;

import java.util.ArrayList;

import timber.log.Timber;

@SuppressWarnings("rawtypes")
public class ContentDirectoryCommand implements IContentDirectoryCommand {
    private final ControlPoint controlPoint;
    private final IUPnPServiceController controller;

    public ContentDirectoryCommand(IUPnPServiceController controller, ControlPoint controlPoint) {
        this.controller = controller;
        this.controlPoint = controlPoint;
    }

    @SuppressWarnings("unused")
    private Service getMediaReceiverRegistarService() {
        if (controller.getSelectedContentDirectory() == null)
            return null;

        return controller.getSelectedContentDirectory().getDevice().findService(
                new UDAServiceType("X_MS_MediaReceiverRegistar"));
    }

    private Service getContentDirectoryService() {
        if (controller.getSelectedContentDirectory() == null)
            return null;

        return controller.getSelectedContentDirectory().getDevice().findService(
                new UDAServiceType("ContentDirectory"));
    }

    private ArrayList<DIDLObjectDisplay> buildContentList(String parent, DIDLContent didl) {
        ArrayList<DIDLObjectDisplay> list = new ArrayList<>();

        if (parent != null)
            list.add(new DIDLObjectDisplay(new ClingDIDLParentContainer(parent)));

        for (Container item : didl.getContainers()) {
            list.add(new DIDLObjectDisplay(new ClingDIDLContainer(item)));
            Timber.v("Add container : " + item.getTitle());
        }

        for (Item item : didl.getItems()) {
            ClingDIDLItem clingItem;
            if (item instanceof VideoItem)
                clingItem = new ClingVideoItem((VideoItem) item);
            else if (item instanceof AudioItem)
                clingItem = new ClingAudioItem((AudioItem) item);
            else if (item instanceof ImageItem)
                clingItem = new ClingImageItem((ImageItem) item);
            else
                clingItem = new ClingDIDLItem(item);

            list.add(new DIDLObjectDisplay(clingItem));
            Timber.v("Add item : " + item.getTitle());

            for (DIDLObject.Property p : item.getProperties())
                Timber.v(p.getDescriptorName() + " " + p.toString());
        }

        return list;
    }

    @Override
    public void browse(String directoryID, String parent, ContentCallback callback) {
        if (getContentDirectoryService() == null)
            return;

        controlPoint.execute(new Browse(getContentDirectoryService(), directoryID, BrowseFlag.DIRECT_CHILDREN, "*", 0,
                null, new SortCriterion(true, "dc:title")) {
            @Override
            public void received(ActionInvocation actionInvocation, final DIDLContent didl) {
                callBack(didl);
            }

            @Override
            public void updateStatus(Status status) {

                Timber.v("updateStatus ! ");
            }

            @Override
            public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
                Timber.w("Fail to browse ! " + defaultMsg);
                callBack(null);
            }

            public void callBack(final DIDLContent didl) {
                if (callback != null) {
                    try {
                        if (didl != null)
                            callback.setContent(buildContentList(parent, didl));
                        callback.call();
                    } catch (Exception e) {
                        Timber.e(e, e.getMessage());
                    }
                }
            }
        });
    }

    @Override
    public void search(String search, String parent, ContentCallback callback) {
        if (getContentDirectoryService() == null)
            return;

        controlPoint.execute(new Search(getContentDirectoryService(), parent, search) {
            @Override
            public void received(ActionInvocation actionInvocation, final DIDLContent didl) {
                if (callback != null) {
                    try {
                        callback.setContent(buildContentList(parent, didl));
                        callback.call();
                    } catch (Exception e) {
                        Timber.e(e, e.getMessage());
                    }
                }
            }

            @Override
            public void updateStatus(Status status) {
                Timber.v("updateStatus ! ");
            }

            @Override
            public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
                Timber.w("Fail to browse ! " + defaultMsg);
            }
        });
    }

    public boolean isSearchAvailable() {
        if (getContentDirectoryService() == null)
            return false;

        return false;
    }
}
