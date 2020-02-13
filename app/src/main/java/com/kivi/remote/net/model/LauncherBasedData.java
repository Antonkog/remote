package com.kivi.remote.net.model;

import android.os.Parcelable;

import java.util.HashMap;

public interface LauncherBasedData extends Parcelable, Comparable<LauncherBasedData> {
    String getID ();
    String getName();
    String getImageUrl();
    String getLocalUri();
    String getPackageName();
    Boolean isActive();
    HashMap<String,String> getAdditionalData();

    TYPE getType();

    enum TYPE  {
        FAVOURITE,
        RECOMMENDATION,
        CHANNEL,
        APPLICATION,
        INPUT
    }
}
