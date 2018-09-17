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
import org.fourthline.cling.support.model.item.VideoItem;
import org.seamless.util.MimeType;

import java.util.List;

import timber.log.Timber;

public class VideoContainer extends DynamicContainer {

    private static final String[] CONTAINER_COLUMNS = {
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.TITLE,
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.ARTIST,
            MediaStore.Video.Media.MIME_TYPE,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.DATE_ADDED,
            MediaStore.Images.Media.HEIGHT,
            MediaStore.Images.Media.WIDTH
    };

    private static final String[] COUNT_COLUMNS = {
            MediaStore.Video.Media._ID, MediaStore.Video.Media.DATA
    };

    public VideoContainer(String id, String parentID, String title, String creator, String baseURL, Context ctx) {
        super(id, parentID, title, creator, baseURL, ctx, null, null);
        uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        orderBy = MediaStore.Video.Media.DATE_ADDED + " DESC";
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
            if (cursor.moveToFirst()) {
                do {
                    String id = ContentDirectoryService.VIDEO_PREFIX + cursor.getInt(cursor.getColumnIndex(MediaStore.Video.Media._ID));
                    String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE));
                    String creator = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.ARTIST));
                    String filePath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA));
                    String mimeType = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE));
                    long size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE));
                    long duration = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION));
                    long height = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT));
                    long width = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH));
                    if (StringUtils.substringBeforeLastKt(filePath, "/").equals(this.title)) {
                        String extension = "";
                        int dot = filePath.lastIndexOf('.');
                        if (dot >= 0)
                            extension = filePath.substring(dot).toLowerCase();

                        Res res = new Res(new MimeType(mimeType.substring(0, mimeType.indexOf('/')),
                                mimeType.substring(mimeType.indexOf('/') + 1)), size, "http://" + baseURL + "/" + id + extension);
                        res.setDuration(duration / (1000 * 60 * 60) + ":"
                                + (duration % (1000 * 60 * 60)) / (1000 * 60) + ":"
                                + (duration % (1000 * 60)) / 1000);
                        res.setResolution((int) width, (int) height);

                        addItem(new VideoItem(id, parentID, title, creator, res));

                        Timber.v("Added video item " + title + " from " + filePath);
                    }
                } while (cursor.moveToNext());
            }
            cursor.close();
        }

        return containers;
    }
}
