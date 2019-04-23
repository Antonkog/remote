package com.wezom.kiviremote.persistence.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.Serializable;

@Entity(tableName = "recent_devices", indices = {@Index(value = {"actual_name"}, unique = true)})
public class RecentDevice implements Parcelable, Comparable<RecentDevice>, Serializable {

    public RecentDevice(String actualName, @Nullable String userDefinedName) {
        this.actualName = actualName;
        this.userDefinedName = userDefinedName;
    }

    @Ignore
    public RecentDevice(int id, String actualName, String userDefinedName) {
        this.id = id;
        this.actualName = actualName;
        this.userDefinedName = userDefinedName;
    }


    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "actual_name")
    private String actualName;

    @ColumnInfo(name = "user_defined_name")
    private String userDefinedName;

    @Ignore
    private boolean online;

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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
                "id=" + id +
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
        dest.writeInt(this.id);
        dest.writeString(this.actualName);
        dest.writeString(this.userDefinedName);
    }

    protected RecentDevice(Parcel in) {
        this.id = in.readInt();
        this.actualName = in.readString();
        this.userDefinedName = in.readString();
    }

    public static final Creator<RecentDevice> CREATOR = new Creator<RecentDevice>() {
        @Override
        public RecentDevice createFromParcel(Parcel in) {
            return new RecentDevice(in);
        }

        @Override
        public RecentDevice[] newArray(int size) {
            return new RecentDevice[size];
        }
    };
}
