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

package com.kivi.remote.upnp.org.droidupnp.model.mediaserver;

import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

import com.kivi.remote.R;
import com.kivi.remote.upnp.fi.iki.elonen.SimpleWebServer;

import org.fourthline.cling.binding.annotations.AnnotationLocalServiceBinder;
import org.fourthline.cling.model.DefaultServiceManager;
import org.fourthline.cling.model.ValidationError;
import org.fourthline.cling.model.ValidationException;
import org.fourthline.cling.model.meta.DeviceDetails;
import org.fourthline.cling.model.meta.DeviceIdentity;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.LocalService;
import org.fourthline.cling.model.meta.ManufacturerDetails;
import org.fourthline.cling.model.meta.ModelDetails;
import org.fourthline.cling.model.types.DeviceType;
import org.fourthline.cling.model.types.UDADeviceType;
import org.fourthline.cling.model.types.UDN;

import java.io.File;
import java.net.InetAddress;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import timber.log.Timber;

public class MediaServer extends SimpleWebServer {

    private UDN udn = null;
    private LocalDevice localDevice = null;
    private LocalService localService = null;
    private Context ctx = null;

    private final int port;
    private static InetAddress localAddress;
    private final String serial;

    private ContentDirectoryService contentDirectoryService;

    public MediaServer(int port, InetAddress localAddress, Context ctx) throws ValidationException {
        super(null, port, null, true);
        this.port = port;

        Timber.i("Creating media server !");

        localService = new AnnotationLocalServiceBinder()
                .read(ContentDirectoryService.class);

        localService.setManager(new DefaultServiceManager<ContentDirectoryService>(
                localService, ContentDirectoryService.class));

        udn = UDN.valueOf(new UUID(0, 10).toString());

        serial = String.valueOf(new Random().nextInt(2147483647) + 100000000000000L);

        MediaServer.localAddress = localAddress;
        this.ctx = ctx;
        createLocalDevice();
        contentDirectoryService = (ContentDirectoryService) localService.getManager().getImplementation();
        contentDirectoryService.setBaseURL(getAddress());
        contentDirectoryService.setContext(ctx);
    }

    public void initContent() {
        if (contentDirectoryService != null)
            contentDirectoryService.initContent();
    }

    public void restart() {
        Timber.d("Restart mediaServer");
//		try {
//			stop();
//			createLocalDevice();
//			start();
//		} catch (Exception e) {
//			Timber.e(e, e.getMessage());
//		}
    }

    public void createLocalDevice() throws ValidationException {
        String version = "";
        try {
            version = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Timber.e(e, "Application version name not found");
        }

        DeviceDetails details = new DeviceDetails(
                ctx.getString(R.string.app_name),
                new ManufacturerDetails(ctx.getString(R.string.app_name), ctx.getString(R.string.app_url)),
                new ModelDetails(ctx.getString(R.string.app_name), ctx.getString(R.string.app_url)),
                serial, version);

        List<ValidationError> l = details.validate();
        for (ValidationError v : l) {
            Timber.e("Validation pb for property " + v.getPropertyName());
            Timber.e("Error is " + v.getMessage());
        }


        DeviceType type = new UDADeviceType("MediaServer", 1);

        localDevice = new LocalDevice(new DeviceIdentity(udn), type, details, localService);
    }


    public LocalDevice getDevice() {
        return localDevice;
    }

    public String getAddress() {
        return localAddress.getHostAddress() + ":" + port;
    }

    public class InvalidIdentificatorException extends Exception {
        public InvalidIdentificatorException() {
            super();
        }

        public InvalidIdentificatorException(String message) {
            super(message);
        }
    }

    class ServerObject {
        ServerObject(String path, String mime) {
            this.path = path;
            this.mime = mime;
        }

        public String path;
        public String mime;
    }

    private ServerObject getFileServerObject(String id) throws InvalidIdentificatorException {
        try {
            // Remove extension
            int dot = id.lastIndexOf('.');
            if (dot >= 0)
                id = id.substring(0, dot);

            // Try to get media id
            int mediaId = Integer.parseInt(id.substring(3));
            Timber.v("media of id is " + mediaId);

            Uri uri = null;

            if (id.startsWith("/" + ContentDirectoryService.AUDIO_PREFIX)) {
                Timber.v("Ask for audio");
                uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            } else if (id.startsWith("/" + ContentDirectoryService.VIDEO_PREFIX)) {
                Timber.v("Ask for video");
                uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
            } else if (id.startsWith("/" + ContentDirectoryService.IMAGE_PREFIX)) {
                Timber.v("Ask for image");
                uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            }

            if (uri != null) {
                String[] columns = new String[]{MediaStore.MediaColumns.DATA, MediaStore.MediaColumns.MIME_TYPE};
                String where = MediaStore.MediaColumns._ID + "=?";
                String[] whereVal = {"" + mediaId};

                String path = null;
                String mime = null;
                Cursor cursor = ctx.getContentResolver().query(uri, columns, where, whereVal, null);

                if (cursor != null && cursor.moveToFirst()) {
                    path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA));
                    mime = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE));
                }
                cursor.close();
                if (path != null)
                    return new ServerObject(path, mime);
            }
        } catch (Exception e) {
            Timber.e(e, "Error while parsing " + id);
            Timber.e(e, "exception", e);
        }

        throw new InvalidIdentificatorException(id + " was not found in media database");
    }

    @Override
    public Response serve(String uri, Method method, Map<String, String> header, Map<String, String> parms,
                          Map<String, String> files) {
        Response res = null;

        Timber.i("Serve uri : " + uri);

        for (Map.Entry<String, String> entry : header.entrySet())
            Timber.d("Header : key=" + entry.getKey() + " value=" + entry.getValue());

        for (Map.Entry<String, String> entry : parms.entrySet())
            Timber.d("Params : key=" + entry.getKey() + " value=" + entry.getValue());

        for (Map.Entry<String, String> entry : files.entrySet())
            Timber.d("Files : key=" + entry.getKey() + " value=" + entry.getValue());

        try {
            try {
                ServerObject obj = getFileServerObject(uri);

                Timber.i("Will serve " + obj.path);
                res = serveFile(new File(obj.path), obj.mime, header);
            } catch (InvalidIdentificatorException e) {
                return new Response(Response.Status.NOT_FOUND, MIME_PLAINTEXT, "Error 404, file not found.");
            } catch (Exception e) {
                Timber.i("exception while serving  "+ e.getMessage());
            }

            if (res != null) {
                String version = "1.0";
                try {
                    version = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0).versionName;
                } catch (PackageManager.NameNotFoundException e) {
                    Timber.e(e, "Application version name not found");
                }

                // Some DLNA header option
                res.addHeader("realTimeInfo.dlna.org", "DLNA.ORG_TLAG=*");
                res.addHeader("contentFeatures.dlna.org", "");
                res.addHeader("transferMode.dlna.org", "Streaming");
                res.addHeader("Server", "DLNADOC/1.50 UPnP/1.0 Cling/2.0 KIVI Remote/" + version + " Android/" + Build.VERSION.RELEASE);
            } else {
                Timber.i("MediaServer Response is null");
            }

            return res;
        } catch (Exception e) {
            Timber.e(e, "Unexpected error while serving file");
            Timber.e(e, "exception", e);
        }

        return new Response(Response.Status.INTERNAL_ERROR, MIME_PLAINTEXT, "INTERNAL ERROR: unexpected error.");
    }

    public String getGeneratedSerial() {
        return serial;
    }
}
