package com.wezom.kiviremote.net.model;


import com.google.gson.annotations.SerializedName;

public class ServerAppInfo {
    @SerializedName("application_name")
    private String applicationName;

    @SerializedName("package_name")
    private String packageName;

    @SerializedName("app_icon")
    private byte[] appIcon;

    public String getApplicationName() {
        return applicationName;
    }

    public String getPackageName() {
        return packageName;
    }

    public byte[] getAppIcon() {
        return appIcon;
    }
}
