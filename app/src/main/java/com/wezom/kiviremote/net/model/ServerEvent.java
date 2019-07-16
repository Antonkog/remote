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
    private InitialMessage initialMessage;
    private List<PreviewCommonStructure> previewCommonStructures;
    private List<Recommendation> recommendations;
    private List<Recommendation> favorites;
    private List<Channel> channels;
    private List<Input> inputs;


    public String getEvent() {
        return event;
    }

    public InitialMessage getInitialMessage() {
        return initialMessage;
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

    public List<Recommendation> getRecommendations() {
        return recommendations;
    }

    public List<Recommendation> getFavorites() {
        return favorites;
    }

    public List<Channel> getChannels() {
        return channels;
    }

    public List<Input> getInputs() {
        return inputs;
    }

    public List<PreviewCommonStructure> getPreviewCommonStructures() {
        return previewCommonStructures;
    }
}
