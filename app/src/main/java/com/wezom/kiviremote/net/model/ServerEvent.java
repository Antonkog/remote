package com.wezom.kiviremote.net.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by andre on 01.06.2017.
 */


public class ServerEvent {
    private String event;
    @SerializedName("app_info")
    private List<ServerAppInfo> apps;
    private int volume;
    private AspectMessage aspectMessage;
    private AspectAvailable availableAspectValues;

    public String getEvent() {
        return event;
    }

    public List<ServerAppInfo> getApps() {
        return apps;
    }

    public int getVolume() {
        return volume;
    }

    public AspectMessage getAspectMessage() {
        return aspectMessage;
    }

    public AspectAvailable getAvailableAspectValues() {
        return availableAspectValues;
    }
}
