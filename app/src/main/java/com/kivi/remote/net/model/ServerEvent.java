package com.kivi.remote.net.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by andre on 01.06.2017.
 */


public class ServerEvent {
    private String event;
    @SerializedName("app_info")
    private List<ServerAppInfo> apps;
    private int volume;

    @SerializedName("args")
    @Expose
    private List<String> args = new ArrayList<>();
    @SerializedName("values")
    @Expose
    private List<Float> values = new ArrayList<>();

    private AspectMessage aspectMessage;
    private AspectAvailable availableAspectValues;
    private List<PreviewContent> previewContents;
    private List<PreviewCommonStructure> previewCommonStructures;

    public String getEvent() {
        return event;
    }


    public List<ServerAppInfo> getApps() {
        return apps;
    }

    public List<PreviewContent> getPreviewContents() {
        return previewContents;
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

    public List<PreviewCommonStructure> getPreviewCommonStructures() {
        return previewCommonStructures;
    }
}
