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
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Pair;

import com.kivi.remote.R;
import com.kivi.remote.common.ImageInfo;
import com.kivi.remote.common.Utils;
import com.kivi.remote.common.VideoInfo;
import com.kivi.remote.common.extensions.StringUtils;
import com.kivi.remote.upnp.org.droidupnp.model.cling.localcontent.CustomContainer;
import com.kivi.remote.upnp.org.droidupnp.model.cling.localcontent.ImageContainer;
import com.kivi.remote.upnp.org.droidupnp.model.cling.localcontent.VideoContainer;

import org.fourthline.cling.support.contentdirectory.AbstractContentDirectoryService;
import org.fourthline.cling.support.contentdirectory.ContentDirectoryErrorCode;
import org.fourthline.cling.support.contentdirectory.ContentDirectoryException;
import org.fourthline.cling.support.contentdirectory.DIDLParser;
import org.fourthline.cling.support.model.BrowseFlag;
import org.fourthline.cling.support.model.BrowseResult;
import org.fourthline.cling.support.model.DIDLContent;
import org.fourthline.cling.support.model.SortCriterion;
import org.fourthline.cling.support.model.container.Container;
import org.fourthline.cling.support.model.item.Item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import timber.log.Timber;

import static com.kivi.remote.common.Constants.IMAGE;
import static com.kivi.remote.common.Constants.VIDEO;
import static com.kivi.remote.common.Utils.getImageDirectoriesPreviews;
import static com.kivi.remote.common.Utils.getVideoDirectoriesPreviews;

public class ContentDirectoryService extends AbstractContentDirectoryService {

    public static final String CONTENTDIRECTORY_SERVICE = "pref_contentDirectoryService";
    public static final String CONTENTDIRECTORY_NAME = "pref_contentDirectoryService_name";
    public static final String CONTENTDIRECTORY_SHARE = "pref_contentDirectoryService_share";
    public static final String CONTENTDIRECTORY_VIDEO = "pref_contentDirectoryService_video";
    public static final String CONTENTDIRECTORY_AUDIO = "pref_contentDirectoryService_audio";
    public static final String CONTENTDIRECTORY_IMAGE = "pref_contentDirectoryService_image";

    public final static char SEPARATOR = '$';

    // Type
    public final static int ROOT_ID = 0;
    private final static int VIDEO_ID = 1;
    private final static int AUDIO_ID = 2;
    private final static int IMAGE_ID = 3;

    // Type subfolder
    private final static int ALL_ID = 0;
    private final static int FOLDER_ID = 1;
    private final static int ARTIST_ID = 2;
    private final static int ALBUM_ID = 3;

    // Prefix item
    public final static String VIDEO_PREFIX = "v-";
    public final static String AUDIO_PREFIX = "a-";
    public final static String IMAGE_PREFIX = "i-";
    public final static String DIRECTORY_PREFIX = "d-";

    private Context context;
    private static String baseURL;

    private Map<String, Pair<String, ImageContainer>> imageContainers;
    private Map<String, Pair<String, VideoContainer>> videoContainers;

    public void setContext(Context context) {
        this.context = context;
    }

    public void initContent() {
        Timber.d("Init UPnP content");
        int entryId = 10;

        imageContainers = new HashMap<>();
        videoContainers = new HashMap<>();

        Set<ImageInfo> allImages = Utils.getAllImages(context);
        Set<VideoInfo> allVideos = Utils.getAllVideos(context);

        Set<String> imagePaths = new HashSet<>();
        Set<String> videoPaths = new HashSet<>();

        String imageDirectoryPreview = null;
        String videoDirectoryPreview = null;

        String currentImageDir = null;
        String currentVideoDir = null;

        for (ImageInfo imageInfo : allImages) {
            String imagePath = StringUtils.substringBeforeLastKt(imageInfo.getData(), "/");

            if (currentImageDir == null)
                currentImageDir = imagePath;

            if (!currentImageDir.equals(imagePath)) {
                imageDirectoryPreview = null;
                currentImageDir = imagePath;
            }

            if (imageDirectoryPreview == null) {
                imageDirectoryPreview = imageInfo.getData();
                getImageDirectoriesPreviews().put(imagePath, imageDirectoryPreview);
            }
            imagePaths.add(imagePath);
        }

        for (VideoInfo videoInfo : allVideos) {
            String videoPath = StringUtils.substringBeforeLastKt(videoInfo.getData(), "/");

            if (currentVideoDir == null)
                currentVideoDir = videoPath;

            if (!currentVideoDir.equals(videoPath)) {
                videoDirectoryPreview = null;
                currentVideoDir = videoPath;
            }

            if (videoDirectoryPreview == null) {
                videoDirectoryPreview = videoInfo.getData();
                getVideoDirectoriesPreviews().put(videoPath, videoDirectoryPreview);
            }

            videoPaths.add(videoPath);
        }

        for (String path : imagePaths) {
            Timber.d("Image directory path: " + path);
            imageContainers.put(String.valueOf(entryId),
                    new Pair<>(path, (new ImageContainer(String.valueOf(entryId++),
                            "0", path,
                            "IMAGE", baseURL, context))));
        }

        for (String path : videoPaths) {
            Timber.d("Video directory path: " + path);
            videoContainers.put(String.valueOf(entryId),
                    new Pair<>
                            (path, (new VideoContainer(String.valueOf(entryId++),
                                    "0", path,
                                    "VIDEO", baseURL, context))));
        }
    }

    public void setBaseURL(String baseURL) {
        ContentDirectoryService.baseURL = baseURL;
    }

    @Override
    public BrowseResult browse(String objectID, BrowseFlag browseFlag,
                               String filter, long firstResult, long maxResults,
                               SortCriterion[] orderby) throws ContentDirectoryException {
        Timber.d("Will browse " + objectID);

        if (context == null) {
            throw new IllegalStateException("Context can't be null");
        }

        try {
            DIDLContent didl = new DIDLContent();
            TextUtils.StringSplitter ss = new TextUtils.SimpleStringSplitter(SEPARATOR);
            ss.setString(objectID);

            int type = -1;
            ArrayList<Integer> subtype = new ArrayList<>();

            for (String s : ss) {
                int i = Integer.parseInt(s);
                if (type == -1) {
                    type = i;
//                    if (type != ROOT_ID && type != VIDEO_ID && type != AUDIO_ID && type != IMAGE_ID)
//                        throw new ContentDirectoryExcept  ion(ContentDirectoryErrorCode.NO_SUCH_OBJECT, "Invalid type!");
                } else {
                    subtype.add(i);
                }
            }

            if (type < 10) {
                Container container = null;

                Timber.d("Browsing type " + type);
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);

                Container rootContainer = new CustomContainer("" + ROOT_ID, "" + ROOT_ID,
                        context.getString(R.string.app_name), context.getString(R.string.app_name), baseURL);

                // Video
                Container videoContainer = null, allVideoContainer = null;
                if (sharedPref.getBoolean(CONTENTDIRECTORY_VIDEO, true)) {
                    videoContainer = new CustomContainer("" + VIDEO_ID, "" + ROOT_ID,
                            VIDEO, context.getString(R.string.app_name), baseURL);
                    rootContainer.addContainer(videoContainer);
                    rootContainer.setChildCount(rootContainer.getChildCount() + 1);

                    if (videoContainers != null) {
                        for (Map.Entry<String, Pair<String, VideoContainer>> item : videoContainers.entrySet()) {
                            videoContainer.addContainer(item.getValue().second);
                        }
                        videoContainer.setChildCount(videoContainers.size());
                    } else
                        videoContainer.setChildCount(0);
                }

                // todo Uncomment this when audio functionality will be added
//            // Audio
//            Container audioContainer = null, artistAudioContainer = null, albumAudioContainer = null,
//                    allAudioContainer = null;
//            if (sharedPref.getBoolean(CONTENTDIRECTORY_AUDIO, true)) {
//                audioContainer = new CustomContainer("" + AUDIO_ID, "" + ROOT_ID,
//                        AUDIO, context.getString(R.string.app_name), baseURL);
//                rootContainer.addContainer(audioContainer);
//                rootContainer.setChildCount(rootContainer.getChildCount() + 1);
//
//                artistAudioContainer = new ArtistContainer("" + ARTIST_ID, "" + AUDIO_ID,
//                        "Artist", context.getString(R.string.app_name), baseURL, context);
//                audioContainer.addContainer(artistAudioContainer);
//                audioContainer.setChildCount(audioContainer.getChildCount() + 1);
//
//                albumAudioContainer = new AlbumContainer("" + ALBUM_ID, "" + AUDIO_ID,
//                        "Album", context.getString(R.string.app_name), baseURL, context, null);
//                audioContainer.addContainer(albumAudioContainer);
//                audioContainer.setChildCount(audioContainer.getChildCount() + 1);
//
//                allAudioContainer = new AudioContainer("" + ALL_ID, "" + AUDIO_ID,
//                        "All", context.getString(R.string.app_name), baseURL, context, null, null);
//                audioContainer.addContainer(allAudioContainer);
//                audioContainer.setChildCount(audioContainer.getChildCount() + 1);
//            }

                // Image
                Container imageContainer = null;
                ImageContainer allImageContainer = null;
                if (sharedPref.getBoolean(CONTENTDIRECTORY_IMAGE, true)) {
                    imageContainer = new CustomContainer("" + IMAGE_ID, "" + ROOT_ID, IMAGE,
                            context.getString(R.string.app_name), baseURL);
                    rootContainer.addContainer(imageContainer);
                    rootContainer.setChildCount(rootContainer.getChildCount() + 1);

                    if (imageContainers != null) {
                        for (Map.Entry<String, Pair<String, ImageContainer>> item : imageContainers.entrySet()) {
                            imageContainer.addContainer(item.getValue().second);
                        }
                        imageContainer.setChildCount(imageContainers.size());
                    } else
                        imageContainer.setChildCount(0);
                }

                if (subtype.size() == 0) {
                    if (type == ROOT_ID) container = rootContainer;
//                if (type == AUDIO_ID) container = audioContainer;
                    if (type == VIDEO_ID) container = videoContainer;
                    if (type == IMAGE_ID) container = imageContainer;
                } else {
                    if (type == VIDEO_ID) {
                        if (subtype.get(0) == ALL_ID) {
                            Timber.d("Listing all videos...");
                            container = allVideoContainer;
                        }
                    }
//                    else if (type == AUDIO_ID) {
//                        if (subtype.size() == 1) {
                    // todo Uncomment this when audio functionality will be added
//                        if (subtype.get(0) == ARTIST_ID) {
//                            Timber.d("Listing all artists...");
//                            container = artistAudioContainer;
//                        } else if (subtype.get(0) == ALBUM_ID) {
//                            Timber.d("Listing album of all artists...");
//                            container = albumAudioContainer;
//                        } else if (subtype.get(0) == ALL_ID) {
//                            Timber.d("Listing all songs...");
//                            container = allAudioContainer;
//                        }
                    // and others...
//                        } else if (subtype.size() == 2 && subtype.get(0) == ARTIST_ID) {
//                            String artistId = "" + subtype.get(1);
//                            String parentId = "" + AUDIO_ID + SEPARATOR + subtype.get(0);
//                            Timber.d("Listing album of artist " + artistId);
//                            container = new AlbumContainer(artistId, parentId, "",
//                                    context.getString(R.string.app_name), baseURL, context, artistId);
//                        } else if (subtype.size() == 2 && subtype.get(0) == ALBUM_ID) {
//                            String albumId = "" + subtype.get(1);
//                            String parentId = "" + AUDIO_ID + SEPARATOR + subtype.get(0);
//                            Timber.d("Listing song of album " + albumId);
//                            container = new AudioContainer(albumId, parentId, "",
//                                    context.getString(R.string.app_name), baseURL, context, null, albumId);
//                        } else if (subtype.size() == 3 && subtype.get(0) == ARTIST_ID) {
//                            String albumId = "" + subtype.get(2);
//                            String parentId = "" + AUDIO_ID + SEPARATOR + subtype.get(0) + SEPARATOR + subtype.get(1);
//                            Timber.d("Listing song of album " + albumId + " for artist " + subtype.get(1));
//                            container = new AudioContainer(albumId, parentId, "",
//                                    context.getString(R.string.app_name), baseURL, context, null, albumId);
//                        }
//                    }
                    else if (type == IMAGE_ID) {
                        if (subtype.get(0) == ALL_ID) {
                            Timber.d("Listing all images...");
                            container = allImageContainer;
                        }
                    }
                }

                if (container != null) {
                    Timber.d("List container...");

                    // Get container first
                    for (Container c : container.getContainers())
                        didl.addContainer(c);

                    Timber.d("List item...");

                    // Then get item
                    for (Item i : container.getItems())
                        didl.addItem(i);

                    Timber.d("Return result...");

                    int count = container.getChildCount();
                    Timber.d("Child count : " + count);
                    String answer;
                    try {
                        answer = new DIDLParser().generate(didl);
                    } catch (Exception ex) {
                        throw new ContentDirectoryException(
                                ContentDirectoryErrorCode.CANNOT_PROCESS, ex.toString());
                    }

                    if (answer.length() > 150)
                        Timber.d("answer : " + answer.substring(0, 150));
                    else
                        Timber.d("answer : " + answer);

                    return new BrowseResult(answer, count, count);
                }
            } else {
                Container container;

                Timber.d("Browsing type " + type);

                if (type > imageContainers.size() - 1 + 10)
                    container = videoContainers.get(objectID).second;
                else
                    container = imageContainers.get(objectID).second;

                if (container != null) {
                    Timber.d("List container...");

                    // Get container first
                    for (Container c : container.getContainers())
                        didl.addContainer(c);

                    Timber.d("List item...");

                    // Then get item
                    for (Item i : container.getItems())
                        didl.addItem(i);

                    Timber.d("Return result...");

                    int count = container.getChildCount();
                    Timber.d("Child count : " + count);
                    String answer;
                    try {
                        answer = new DIDLParser().generate(didl);
                    } catch (Exception ex) {
                        throw new ContentDirectoryException(
                                ContentDirectoryErrorCode.CANNOT_PROCESS, ex.toString());
                    }

                    if (answer.length() > 150)
                        Timber.d("answer : " + answer.substring(0, 150));
                    else
                        Timber.d("answer : " + answer);

                    return new BrowseResult(answer, count, count);
                }
            }
        } catch (Exception ex) {
            throw new ContentDirectoryException(
                    ContentDirectoryErrorCode.CANNOT_PROCESS, ex.toString());
        }

        Timber.e("No container for this ID !!!");
        throw new ContentDirectoryException(ContentDirectoryErrorCode.NO_SUCH_OBJECT);
    }
}
