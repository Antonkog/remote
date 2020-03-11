package com.kivi.remote.net.model;

import com.google.gson.annotations.SerializedName;


/**
 * Created by andre on 09.06.2017.
 */

public class ConnectionMessage {
    private String message;
    private boolean isSetKeyboard;
    @SerializedName("app_info")
    private AspectMessage aspectMessage;
    private AspectAvailable available;

    private boolean showKeyboard;
    private boolean hideKeyboard;
    private int volume;

    public ConnectionMessage(String message, boolean isSetKeyboard, boolean showKeyboard, boolean hideKeyboard, int volume) {
        this.message = message;
        this.isSetKeyboard = isSetKeyboard;
        this.showKeyboard = showKeyboard;
        this.hideKeyboard = hideKeyboard;
        this.volume = volume;
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

}
