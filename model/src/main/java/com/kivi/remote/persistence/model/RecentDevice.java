package com.kivi.remote.persistence.model;

import java.io.Serializable;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "recent_devices", indices = {@Index(value = {"actual_name"}, unique = true)})
public class RecentDevice implements Comparable<RecentDevice>, Serializable {

    public RecentDevice(String actualName) {
        this.actualName = replace032(actualName);
        this.online = true;
    }

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "actual_name")
    private String actualName;

    @ColumnInfo(name = "user_defined_name")
    private String userDefinedName;

    @ColumnInfo(name = "online")
    private boolean online;

    @ColumnInfo(name = "wasConnected")
    private Long wasConnected;

    public void setActualName(String actualName) {
        this.actualName = actualName;
    }

    public void setUserDefinedName(String userDefinedName) {
        this.userDefinedName = userDefinedName;
    }

    private String replace032(String actualName){
        return actualName.replace("\\032", " ").replace("\\03", "");
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public Long getWasConnected() {
        return wasConnected;
    }

    public void setWasConnected(Long wasConnected) {
        this.wasConnected = wasConnected;
    }

    public String getActualName() {
        return replace032(actualName);
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
}
