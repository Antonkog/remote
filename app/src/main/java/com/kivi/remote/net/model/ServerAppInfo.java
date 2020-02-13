package com.kivi.remote.net.model;


import android.os.Parcel;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import java.util.Arrays;
import java.util.HashMap;

public class ServerAppInfo implements LauncherBasedData {
    @SerializedName("application_name")
    private String applicationName;

    @SerializedName("package_name")
    private String packageName;

    @SerializedName("app_icon")
    private byte[] appIcon;

    @SerializedName("baseIcon")
    private String baseIcon;

    @SerializedName("uri")
    private String imageUri;


    public ServerAppInfo(String applicationName, String packageName, String baseIcon) {
        this.applicationName = applicationName;
        this.packageName = packageName;
        this.baseIcon = baseIcon;
    }

    public String getApplicationName() {
        return applicationName;
    }

    @Override
    public String getID() {
        return packageName;
    }

    @Override
    public String getName() {
        return applicationName;
    }

    @Override
    public String getImageUrl() {
        return null;
    }

    @Override
    public String getLocalUri() {
        return null;
    }

    public String getPackageName() {
        return packageName;
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
        return TYPE.APPLICATION;
    }

    public byte[] getAppIcon() {
        return appIcon;
    }

    public String getBaseIcon() {
        return baseIcon;
    }

    public String getImageUri() {
        return imageUri;
    }

    @Override
    public int compareTo(@NonNull LauncherBasedData o) {
        return this.getType().ordinal() - o.getType().ordinal();
    }


    public ServerAppInfo() {
    }

    @Override
    public String toString() {
        return "ServerAppInfo{" +
                "applicationName='" + applicationName + '\'' +
                ", packageName='" + packageName + '\'' +
                ", appIcon=" + Arrays.toString(appIcon) +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.applicationName);
        dest.writeString(this.packageName);
        dest.writeByteArray(this.appIcon);
        dest.writeString(this.baseIcon);
    }

    protected ServerAppInfo(Parcel in) {
        this.applicationName = in.readString();
        this.packageName = in.readString();
        this.appIcon = in.createByteArray();
        this.baseIcon = in.readString();
    }

    public static final Creator<ServerAppInfo> CREATOR = new Creator<ServerAppInfo>() {
        @Override
        public ServerAppInfo createFromParcel(Parcel source) {
            return new ServerAppInfo(source);
        }

        @Override
        public ServerAppInfo[] newArray(int size) {
            return new ServerAppInfo[size];
        }
    };

}
