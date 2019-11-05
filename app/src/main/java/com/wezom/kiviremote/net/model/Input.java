package com.wezom.kiviremote.net.model;

import android.os.Parcel;

import androidx.annotation.NonNull;

import com.wezom.kiviremote.persistence.model.ServerInput;

import java.util.HashMap;

public class Input implements LauncherBasedData {
    int portNum;
    private String portName;
    private String imageUrl;
    private String uri;
    private Boolean active;
    private String inputIcon;

    public Input addImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
        return this;
    }


    public Input addPortNum(int portNum) {
        this.portNum = portNum;
        return this;
    }

    public Input addPortName(String portName) {
        this.portName = portName;
        return this;
    }

    public Input addUri(String uri) {
        this.uri = uri;
        return this;
    }

    public Input addActive(Boolean active) {
        this.active = active;
        return this;
    }


    public Input addInputIcon(String inputIcon) {
        this.inputIcon = inputIcon;
        return this;
    }

    public int getIntID() {
        return portNum;
    }

    @Override
    public String getID() {
        return "" + portNum;
    }

    @Override
    public String getName() {
        return portName;
    }

    @Override
    public String getImageUrl() {
        return imageUrl;
    }

    @Override
    public String getLocalUri() {
        return uri;
    }

    public int getPortNum() {
        return portNum;
    }

    public String getPortName() {
        return portName;
    }

    @Override
    public String getPackageName() {
        return null;
    }

    @Override
    public Boolean isActive() {
        return active;
    }

    @Override
    public HashMap<String, String> getAdditionalData() {
        return null;
    }

    @Override
    public TYPE getType() {
        return TYPE.INPUT;
    }

    public String getInputIcon() {
        return inputIcon;
    }

    public Input() {
    }

    public Input(ServerInput input) {
        this.active = input.getActive();
        this.imageUrl = input.getImageUrl();
        this.portName = input.getPortName();
        this.inputIcon =input.getInputIcon();
        this.portNum = input.getPortNum();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.portNum);
        dest.writeString(this.portName);
        dest.writeString(this.imageUrl);
        dest.writeString(this.uri);
        dest.writeValue(this.active);
        dest.writeString(this.inputIcon);
    }

    protected Input(Parcel in) {
        this.portNum = in.readInt();
        this.portName = in.readString();
        this.imageUrl = in.readString();
        this.uri = in.readString();
        this.active = (Boolean) in.readValue(Boolean.class.getClassLoader());
        this.inputIcon = in.readString();
    }

    public static final Creator<Input> CREATOR = new Creator<Input>() {
        @Override
        public Input createFromParcel(Parcel source) {
            return new Input(source);
        }

        @Override
        public Input[] newArray(int size) {
            return new Input[size];
        }
    };

    @Override
    public int compareTo(@NonNull LauncherBasedData o) {
        return this.getType().ordinal() - o.getType().ordinal();
    }

    @Override
    public String toString() {
        return "Input{" +
                "portNum=" + portNum +
                ", portName='" + portName + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", uri='" + uri + '\'' +
                ", active=" + active +
                ", inputIcon=" + inputIcon;
    }
}