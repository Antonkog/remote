package com.wezom.kiviremote.net.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;


/**
 * Created by andre on 09.06.2017.
 */

public class ConnectionMessage {
    private String message;
    private boolean isSetKeyboard;
    @SerializedName("app_info")
    private List<ServerAppInfo> appList;
    private InitialMessage initialMessage;
    private AspectMessage aspectMessage;
    private AspectAvailable available;
    private boolean showKeyboard;
    private boolean hideKeyboard;
    private int volume;
    private boolean disconnect;

    public ConnectionMessage(String message, boolean isSetKeyboard, List<ServerAppInfo> appList, InitialMessage initialMessage, AspectMessage aspectMessage, AspectAvailable available, boolean showKeyboard, boolean hideKeyboard, int volume, boolean disconnect) {
        this.message = message;
        this.isSetKeyboard = isSetKeyboard;
        this.appList = appList;
        this.initialMessage = initialMessage;
        this.aspectMessage = aspectMessage;
        this.available = available;
        this.showKeyboard = showKeyboard;
        this.hideKeyboard = hideKeyboard;
        this.volume = volume;
        this.disconnect = disconnect;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSetKeyboard() {
        return isSetKeyboard;
    }

    public List<ServerAppInfo> getAppList() {
        return appList;
    }

    public InitialMessage getInitialMessage() {
        return initialMessage;
    }

    public AspectMessage getAspectMessage() {
        return aspectMessage;
    }

    public AspectAvailable getAvailable() {
        return available;
    }

    public void setAvailable(AspectAvailable available) {
        this.available = available;
    }

    public boolean isShowKeyboard() {
        return showKeyboard;
    }

    public boolean isHideKeyboard() {
        return hideKeyboard;
    }

    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }

    public boolean isDisconnect() {
        return disconnect;
    }

}
