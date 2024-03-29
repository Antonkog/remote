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

package com.wezom.kiviremote.upnp.org.droidupnp.model.cling.localcontent;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;

import com.wezom.kiviremote.common.extensions.StringUtils;
import com.wezom.kiviremote.upnp.org.droidupnp.model.mediaserver.ContentDirectoryService;

import org.fourthline.cling.support.model.Res;
import org.fourthline.cling.support.model.container.Container;
import org.fourthline.cling.support.model.item.ImageItem;
import org.seamless.util.MimeType;

import java.util.List;

import timber.log.Timber;


public class ImageContainer extends DynamicContainer {

    private static final String[] CONTAINER_COLUMNS = {
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.TITLE,
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.MIME_TYPE,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.HEIGHT,
            MediaStore.Images.Media.WIDTH,
            MediaStore.Images.Media.DATE_ADDED,
    };

    private static final String[] COUNT_COLUMNS = {
            MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA
    };

    public ImageContainer(String id, String parentID, String title, String creator, String baseURL, Context ctx) {
        super(id, parentID, title, creator, baseURL, ctx, null, null);
        uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        orderBy = MediaStore.Images.Media.DATE_ADDED + " DESC";
    }

    @Override
    public Integer getChildCount() {
        int childCount = 0;
        Cursor cursor = ctx.getContentResolver().query(uri, COUNT_COLUMNS, where, whereVal, orderBy);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String filePath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
                if (StringUtils.substringBeforeLastKt(filePath, "/").equals(this.title)) {
                    childCount++;
                }
            } while (cursor.moveToNext());
            cursor.close();
        }
        return childCount;
    }

    @Override
    public List<Container> getContainers() {
        getItems().clear();

        Cursor cursor = ctx.getContentResolver().query(uri, CONTAINER_COLUMNS, where, whereVal, orderBy);
        if (cursor != null) {
            if (cursor.moveToFirst())
                do {
                    String id = ContentDirectoryService.IMAGE_PREFIX + cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media._ID));
                    String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.TITLE));
                    String filePath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
                    String mimeType = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE));
                    long size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE));
                    long height = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT));
                    long width = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH));

                    if (StringUtils.substringBeforeLastKt(filePath, "/").equals(this.title)) {
                        String extension = "";
                        int dot = filePath.lastIndexOf('.');
                        if (dot >= 0)
                            extension = filePath.substring(dot).toLowerCase();

                        Res res = new Res(new MimeType(mimeType.substring(0, mimeType.indexOf('/')),
                                mimeType.substring(mimeType.indexOf('/') + 1)), size, "http://" + baseURL + "/" + id + extension);
                        res.setResolution((int) width, (int) height);

                        addItem(new ImageItem(id, parentID, title, "", res));

                        Timber.v("Added image item " + title + " from " + filePath);
                    }
                } while (cursor.moveToNext());
            cursor.close();
        }

        return containers;
    }
}
