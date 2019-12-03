package com.wezom.kiviremote.persistence.model;


import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "inputs")
public class ServerInput {

    @PrimaryKey
    @ColumnInfo(name = "portNum")
    private int portNum;

    @ColumnInfo(name = "portName")
    private String portName;

    @ColumnInfo(name = "imageUrl")
    private String imageUrl;

    @ColumnInfo(name = "active")
    private Boolean active;

    @ColumnInfo(name = "inputIcon")
    private String inputIcon;

    @ColumnInfo(name = "localResource")
    private int localResource;

    public int getPortNum() {
        return portNum;
    }

    public void setPortNum(int portNum) {
        this.portNum = portNum;
    }

    public String getPortName() {
        return portName;
    }

    public void setPortName(String portName) {
        this.portName = portName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String  getInputIcon() {
        return inputIcon;
    }

    public void setInputIcon(String inputIcon) {
        this.inputIcon = inputIcon;
    }

    public int getLocalResource() {
        return localResource;
    }

    public void setLocalResource(int localResource) {
        this.localResource = localResource;
    }

}
