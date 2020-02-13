package com.kivi.remote.net.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.kivi.remote.bus.RemotePlayerEvent;
import com.kivi.remote.common.Action;

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

    public SocketConnectionModel setArgs(List<String> args) {
        this.args = args;
        return this;
    }


    public SocketConnectionModel setArg(String arg) {
        this.args.clear();
        this.args.add(arg);
        return this;
    }

    public SocketConnectionModel setMotion(List<Double> motion) {
        this.motion = motion;
        return this;
    }

    public SocketConnectionModel setSeekTo(RemotePlayerEvent event) {
        if (event != null && event.getArgs() != null && event.getArgs().get(0) != null) {
            this.motion.clear();
            this.motion.add(event.getArgs().get(0).doubleValue());
        }
        return this;
    }


    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public void setAspectMessage(AspectMessage aspectMessage) {
        this.aspectMessage = aspectMessage;
    }

}
