package com.wezom.kiviremote.net.model;

import java.util.HashMap;

public class AspectAvailable {

    public enum VALUE_TYPE {
        INPUT_PORT,
        RATIO,
        TEMPERATUREVALUES,
        HDR,
        PICTUREMODE
    }

    public HashMap<String, int[]> settings;

    @Override
    public String toString() {
        return "AspectAvailable{" +
                "settings=\n" + settings.toString() +
                "\n}";
    }

    public AspectAvailable(HashMap<String, int[]> settings) {
        this.settings = settings;
    }
}