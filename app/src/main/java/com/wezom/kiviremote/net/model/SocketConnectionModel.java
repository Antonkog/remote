package com.wezom.kiviremote.net.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.wezom.kiviremote.common.Action;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by andre on 30.05.2017.
 */

public class SocketConnectionModel {

    @SerializedName("action")
    @Expose
    private String action;

    @SerializedName("args")
    @Expose
    private List<String> args = new ArrayList<>();

    @SerializedName("motion")
    @Expose
    private List<Double> motion = new ArrayList<>();

    @SerializedName("package_name")
    private String packageName;

    public String getAction() {
        return action;
    }

    @SerializedName("aspectMessage")
    private AspectMessage aspectMessage;

    public SocketConnectionModel setAction(Action action) {
        this.action = action.name();
        return this;
    }

    public void setArgs(List<String> args) {
        this.args = args;
    }


    public SocketConnectionModel setArg(String  arg) {
        this.args.clear();
        this.args.add(arg);
        return this;
    }

    public void setMotion(List<Double> motion) {
        this.motion = motion;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public void setAspectMessage(AspectMessage aspectMessage) {
        this.aspectMessage = aspectMessage;
    }

}
