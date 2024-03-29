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

import org.fourthline.cling.support.model.container.Container;

import java.util.List;

import timber.log.Timber;

public class AlbumContainer extends DynamicContainer {
    protected String artist = null;
    protected String artistId = null;

    public AlbumContainer(String id, String parentID, String title, String creator, String baseURL, Context ctx, String artistId) {
        super(id, parentID, title, creator, baseURL, ctx, null, null);

        this.artistId = artistId;
        if (artistId == null)
            uri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI;
        else
            uri = MediaStore.Audio.Artists.Albums.getContentUri("external", Integer.parseInt(artistId));
    }

    @Override
    public Integer getChildCount() {
        String[] columns;
        if (artistId == null)
            columns = new String[]{MediaStore.Audio.Albums._ID};
        else
            columns = new String[]{MediaStore.Audio.Artists.Albums.ALBUM};

        Cursor cursor = ctx.getContentResolver().query(uri, columns, where, whereVal, orderBy);
        if (cursor == null)
            return 0;
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    @Override
    public List<Container> getContainers() {
        Timber.d("Get albums !");

        String[] columns;
        if (artistId == null)
            columns = new String[]{MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ALBUM};
        else
            columns = new String[]{MediaStore.Audio.Artists.Albums.ALBUM};

        Cursor cursor = ctx.getContentResolver().query(uri, columns, where, whereVal, orderBy);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    String albumId = null;
                    String album;
                    if (artistId == null) {
                        albumId = "" + cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Albums._ID));
                        album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM));
                    } else {
                        album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists.Albums.ALBUM));

                        String[] columns2 = new String[]{MediaStore.Audio.Albums._ID};
                        String where2 = MediaStore.Audio.Albums.ALBUM + "=?";
                        String[] whereVal2 = {album};

                        Cursor cursor2 = ctx.getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                                columns2, where2, whereVal2, null);
                        if (cursor2.moveToFirst())
                            albumId = "" + cursor2.getInt(cursor2.getColumnIndex(MediaStore.Audio.Albums._ID));
                        cursor2.close();
                    }

                    if (albumId != null && album != null) {
                        Timber.d(" current " + id + " albumId : " + albumId + " album : " + album);
                        containers.add(new AudioContainer(albumId, id, album, artist, baseURL, ctx, null, albumId));
                    } else {
                        Timber.d("Unable to get albumId or album");
                    }

                } while (cursor.moveToNext());
            }
            cursor.close();
        }

        return containers;
    }

}
