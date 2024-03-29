/**
 * Copyright (C) 2013 Aur?lien Chabot <aurelien@chabot.fr>
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

public class ArtistContainer extends DynamicContainer {
    public ArtistContainer(String id, String parentID, String title, String creator, String baseURL, Context ctx) {
        super(id, parentID, title, creator, baseURL, ctx, null, null);
        Timber.d("Create ArtistContainer of id " + id + " , " + this.id);
        uri = MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI;
    }

    @Override
    public Integer getChildCount() {
        String[] columns = {MediaStore.Audio.Artists._ID};
        Cursor cursor = ctx.getContentResolver().query(uri, columns, null, null, null);
        if (cursor == null)
            return 0;
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    @Override
    public List<Container> getContainers() {
        Timber.d("Get artist !");

        String[] columns = {
                MediaStore.Audio.Artists._ID,
                MediaStore.Audio.Artists.ARTIST,
        };

        Cursor cursor = ctx.getContentResolver().query(uri, columns, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    String artistId = "" + cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Artists._ID));
                    String artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists.ARTIST));

                    Timber.d(" artistId : " + artistId + " artistArtist : " + artist);
                    containers.add(new AlbumContainer(artistId, id, artist, artist, baseURL, ctx, artistId));

                } while (cursor.moveToNext());
            }
            cursor.close();
        }

        return containers;
    }

}
