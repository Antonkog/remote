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
    private boolean showKeyboard;
    private boolean hideKeyboard;
    private int volume;
    private boolean disconnect;

    private AspectMessage aspectMessage;

    public ConnectionMessage(String message, boolean isSetKeyboard, List<ServerAppInfo> appList, boolean showKeyboard, boolean hideKeyboard, int volume, boolean disconnect, AspectMessage aspectMessage) {
        this.message = message;
        this.isSetKeyboard = isSetKeyboard;
        this.appList = appList;
        this.showKeyboard = showKeyboard;
        this.hideKeyboard = hideKeyboard;
        this.volume = volume;
        this.disconnect = disconnect;
        this.aspectMessage = aspectMessage;
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

    public AspectMessage getAspectMessage() {
        return aspectMessage;
    }
}
