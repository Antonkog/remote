package com.wezom.kiviremote.persistence.model;


import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "apps")
public class  ServerApp {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "app_name")
    private String appName;

    @ColumnInfo(name = "package_name")
    private String packageName;

    @ColumnInfo(name = "app_icon")
    private byte[] appIcon;

    @ColumnInfo(name = "baseIcon")
    private String baseIcon;

        @ColumnInfo(name = "uri")
        private String  uri;

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

    public String getBaseIcon() {
        return baseIcon;
    }

    public void setBaseIcon(String baseIcon) {
        this.baseIcon = baseIcon;
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

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }
}
