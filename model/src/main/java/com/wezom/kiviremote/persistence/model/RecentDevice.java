package com.wezom.kiviremote.persistence.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.io.Serializable;

@Entity(tableName = "recent_devices", indices = {@Index(value = {"actual_name"}, unique = true)})
public class RecentDevice implements Parcelable, Comparable<RecentDevice>, Serializable {

    public RecentDevice(String actualName, String userDefinedName, boolean online) {
        this.actualName = actualName;
        this.userDefinedName = userDefinedName;
        this.online = online;
    }

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "actual_name")
    private String actualName;

    @ColumnInfo(name = "user_defined_name")
    private String userDefinedName;

    @ColumnInfo(name = "online")
    private boolean online;



    public void setActualName(String actualName) {
        this.actualName = actualName;
    }

    public void setUserDefinedName(String userDefinedName) {
        this.userDefinedName = userDefinedName;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public String getActualName() {
        return actualName;
    }

    public String getUserDefinedName() {
        return userDefinedName;
    }


    @Override
    public int compareTo(@NonNull RecentDevice o) {
        if (isOnline()) {
            if (o.isOnline()) return 0;
            else return -1;
        } else {
            if (o.isOnline()) return 1;
            else return 0;
        }
    }

    @Override
    public String toString() {
        return "RecentDevice{" +
                ", actualName='" + actualName + '\'' +
                ", userDefinedName='" + userDefinedName + '\'' +
                ", online=" + online +
                '}';
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.actualName);
        dest.writeString(this.userDefinedName);
        dest.writeByte(this.online ? (byte) 1 : (byte) 0);
    }

    protected RecentDevice(Parcel in) {
        this.actualName = in.readString();
        this.userDefinedName = in.readString();
        this.online = in.readByte() != 0;
    }

    public static final Creator<RecentDevice> CREATOR = new Creator<RecentDevice>() {
        @Override
        public RecentDevice createFromParcel(Parcel source) {
            return new RecentDevice(source);
        }

        @Override
        public RecentDevice[] newArray(int size) {
            return new RecentDevice[size];
        }
    };
}
