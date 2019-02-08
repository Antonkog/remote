package com.wezom.kiviremote.net.model;

import com.wezom.kiviremote.presentation.home.tvsettings.driver_set.DriverValue;

import java.util.List;

public class InitialMessage {
    public String buildProp;
    public List<DriverValue> driverValueList;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for(DriverValue value : driverValueList){
            sb.append(value.toString() + "\n");
        }

        return "InitialMessage{" +
                "buildProp='" + buildProp + "\n" +
                ", portList= \n" + sb.toString() +
                '}';
    }
}