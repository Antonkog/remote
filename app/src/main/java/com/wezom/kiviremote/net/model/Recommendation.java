package com.wezom.kiviremote.net.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.HashMap;

public class Recommendation implements LauncherBasedData, Parcelable {
    String contentID;
    private String title;
    private String subTitle;
    private String description;
    private String imageUrl;
    private String uri;
    private String kind;
    private String imdb;
    private String monetizationType;

    public Recommendation addContentId(String contentID) {
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

    public Recommendation addKind(String kind) {
        this.kind = kind;
        return this;
    }

    public Recommendation setImdb(String imdb) {
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
        dest.writeString(this.contentID);
        dest.writeString(this.title);
        dest.writeString(this.subTitle);
        dest.writeString(this.description);
        dest.writeString(this.imageUrl);
        dest.writeString(this.uri);
        dest.writeString(this.kind);
        dest.writeString(this.imdb);
        dest.writeString(this.monetizationType);
    }

    public Recommendation() {
    }

    protected Recommendation(Parcel in) {
        this.contentID = in.readString();
        this.title = in.readString();
        this.subTitle = in.readString();
        this.description = in.readString();
        this.imageUrl = in.readString();
        this.uri = in.readString();
        this.kind = in.readString();
        this.imdb = in.readString();
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