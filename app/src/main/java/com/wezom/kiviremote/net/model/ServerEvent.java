package com.wezom.kiviremote.net.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by andre on 01.06.2017.
 */


public class ServerEvent {
    private String event;
    private int displayY;
    private int displayX;
    @SerializedName("app_info")
    private List<ServerAppInfo> apps;
    private int volume;

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public int getDisplayY() {
        return displayY;
    }

    public void setDisplayY(int displayY) {
        this.displayY = displayY;
    }

    public int getDisplayX() {
        return displayX;
    }

    public void setDisplayX(int displayX) {
        this.displayX = displayX;
    }

    public List<ServerAppInfo> getApps() {
        return apps;
    }

    public void setApps(List<ServerAppInfo> apps) {
        this.apps = apps;
    }

    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }
}
