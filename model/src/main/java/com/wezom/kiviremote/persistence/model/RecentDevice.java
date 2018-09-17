package com.wezom.kiviremote.persistence.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

@Entity(tableName = "recent_devices", indices = {@Index(value = {"actual_name"}, unique = true)})
public class RecentDevice implements Parcelable {

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

    protected RecentDevice(Parcel in) {
        id = in.readInt();
        actualName = in.readString();
        userDefinedName = in.readString();
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

    public void setActualName(String actualName) {
        this.actualName = actualName;
    }

    public String getUserDefinedName() {
        return userDefinedName;
    }

    public void setUserDefinedName(String userDefinedName) {
        this.userDefinedName = userDefinedName;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeString(actualName);
        parcel.writeString(userDefinedName);
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
