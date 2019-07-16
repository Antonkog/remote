package com.wezom.kiviremote.net.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import org.fourthline.cling.support.model.DIDLObject;

import java.util.HashMap;

public class Channel implements LauncherBasedData, Parcelable {
    private String id;
    private String name;
    private String icon_url;
    private Boolean is_active;
    private int sort;
    private String created_at;
    private String edited_at;
    private Boolean has_timeshift;
    private Boolean adult_content;


    public Channel addName(String name) {
        this.name = name;
        return this;
    }

    public Channel addId (String id) {
        this.id = id;
        return this;
    }

    public Channel addActive(Boolean is_active) {
        this.is_active = is_active;
        return this;
    }

    public Channel addIconUrl(String icon_url) {
        this.icon_url = icon_url;
        return this;
    }

    public Channel addSort(int sort) {
        this.sort = sort;
        return this;
    }

    public Channel addEdited(String edited_at) {
        this.edited_at = edited_at;
        return this;
    }

    @Override
    public String getID() {
        return id;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public String getImageUrl() {
        return icon_url;
    }

    @Override
    public String getLocalUri() {
        return null;
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
        return TYPE.CHANNEL;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.name);
        dest.writeString(this.icon_url);
        dest.writeValue(this.is_active);
        dest.writeInt(this.sort);
        dest.writeString(this.created_at);
        dest.writeString(this.edited_at);
        dest.writeValue(this.has_timeshift);
        dest.writeValue(this.adult_content);
    }

    public Channel() {
    }

    protected Channel(Parcel in) {
        this.id = in.readString();
        this.name = in.readString();
        this.icon_url = in.readString();
        this.is_active = (Boolean) in.readValue(Boolean.class.getClassLoader());
        this.sort = in.readInt();
        this.created_at = in.readString();
        this.edited_at = in.readString();
        this.has_timeshift = (Boolean) in.readValue(Boolean.class.getClassLoader());
        this.adult_content = (Boolean) in.readValue(Boolean.class.getClassLoader());
    }

    public static final Parcelable.Creator<Channel> CREATOR = new Parcelable.Creator<Channel>() {
        @Override
        public Channel createFromParcel(Parcel source) {
            return new Channel(source);
        }

        @Override
        public Channel[] newArray(int size) {
            return new Channel[size];
        }
    };


    @Override
    public String toString() {
        return "Channel{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", icon_url='" + icon_url + '\'' +
                ", is_active=" + is_active +
                ", sort=" + sort +
                ", created_at='" + created_at + '\'' +
                ", edited_at='" + edited_at + '\'' +
                ", has_timeshift=" + has_timeshift +
                ", adult_content=" + adult_content +
                '}';
    }

    @Override
    public int compareTo(@NonNull LauncherBasedData o) {
        return this.getType().ordinal() - o.getType().ordinal();
    }
}
