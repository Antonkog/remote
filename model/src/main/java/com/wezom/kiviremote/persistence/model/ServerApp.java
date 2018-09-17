package com.wezom.kiviremote.persistence.model;


import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "apps")
public class ServerApp {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "app_name")
    private String appName;

    @ColumnInfo(name = "package_name")
    private String packageName;

    @ColumnInfo(name = "app_icon")
    private byte[] appIcon;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public byte[] getAppIcon() {
        return appIcon;
    }

    public void setAppIcon(byte[] appIcon) {
        this.appIcon = appIcon;
    }
}
