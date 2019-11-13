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
    private InitialMessage initialMessage;
    private AspectMessage aspectMessage;
    private AspectAvailable available;

    private boolean showKeyboard;
    private boolean hideKeyboard;
    private int volume;
    private boolean disconnect;

    public ConnectionMessage(String message, boolean isSetKeyboard, boolean showKeyboard, boolean hideKeyboard, int volume, boolean disconnect) {
        this.message = message;
        this.isSetKeyboard = isSetKeyboard;
        this.showKeyboard = showKeyboard;
        this.hideKeyboard = hideKeyboard;
        this.volume = volume;
        this.disconnect = disconnect;
    }



    public ConnectionMessage addInitial (InitialMessage initialMessage) {
        this.initialMessage = initialMessage;
        return this;
    }

    public ConnectionMessage addAvailable (AspectAvailable available) {
        this.available = available;
        return this;
    }

    public ConnectionMessage addAspectMessage(AspectMessage aspectMessage) {
        this.aspectMessage = aspectMessage;
        return this;
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

    public InitialMessage getInitialMessage() {
        return initialMessage;
    }

    public AspectMessage getAspectMessage() {
        return aspectMessage;
    }

    public AspectAvailable getAvailable() {
        return available;
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
