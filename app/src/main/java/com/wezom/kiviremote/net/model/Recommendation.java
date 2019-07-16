package com.wezom.kiviremote.net.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.util.HashMap;

public class Recommendation implements LauncherBasedData, Parcelable {
    int contentID;
    private String title;
    private String subTitle;
    private String description;
    private String imageUrl;
    private String uri;
    private int kind;
    private float imdb;
    private String monetizationType;

    public Recommendation addContent(int contentID) {
        this.contentID = contentID;
        return this;
    }

    public Recommendation addTitle(String title) {
        this.title = title;
        return this;
    }


    public Recommendation addSubtitle(String subTitle) {
        this.subTitle = subTitle;
        return this;
    }


    public Recommendation addDiscription(String description) {
        this.description = description;
        return this;
    }


    public Recommendation addImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
        return this;
    }

    public Recommendation addKind(int kind) {
        this.kind = kind;
        return this;
    }

    public Recommendation setImdb(float imdb) {
        this.imdb = imdb;
        return this;
    }

    @Override
    public String getID() {
        return this.contentID+"";
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public String getImageUrl() {
        return imageUrl;
    }

    @Override
    public String getLocalUri() {
        return uri;
    }

    @Override
    public String getPackageName() {
        return null;
    }

    @Override
    public Boolean isActive() {
        return null;
    }

    @Override
    public HashMap<String, String> getAdditionalData() {
        return null;
    }

    @Override
    public TYPE getType() {
                 return TYPE.RECOMMENDATION;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.contentID);
        dest.writeString(this.title);
        dest.writeString(this.subTitle);
        dest.writeString(this.description);
        dest.writeString(this.imageUrl);
        dest.writeString(this.uri);
        dest.writeInt(this.kind);
        dest.writeFloat(this.imdb);
        dest.writeString(this.monetizationType);
    }

    public Recommendation() {
    }

    protected Recommendation(Parcel in) {
        this.contentID = in.readInt();
        this.title = in.readString();
        this.subTitle = in.readString();
        this.description = in.readString();
        this.imageUrl = in.readString();
        this.uri = in.readString();
        this.kind = in.readInt();
        this.imdb = in.readFloat();
        this.monetizationType = in.readString();
    }

    public static final Parcelable.Creator<Recommendation> CREATOR = new Parcelable.Creator<Recommendation>() {
        @Override
        public Recommendation createFromParcel(Parcel source) {
            return new Recommendation(source);
        }

        @Override
        public Recommendation[] newArray(int size) {
            return new Recommendation[size];
        }
    };


    @Override
    public String toString() {
        return "Recommendation{" +
                "contentID=" + contentID +
                ", title='" + title + '\'' +
                ", subTitle='" + subTitle + '\'' +
                ", description='" + description + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", uri='" + uri + '\'' +
                ", kind=" + kind +
                ", imdb=" + imdb +
                ", monetizationType='" + monetizationType + '\'' +
                '}';
    }
    @Override
    public int compareTo(@NonNull LauncherBasedData o) {
        return this.getType().ordinal() - o.getType().ordinal();
    }
}