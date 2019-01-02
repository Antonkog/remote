package com.wezom.kiviremote.net.model;

import org.jetbrains.annotations.Nullable;

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

    private int[] getSettings(VALUE_TYPE value_type) {
        if(settings!= null && !settings.isEmpty())
        return settings.get(value_type.name());
        return null;
    }

    @Nullable
    public  int[] getPorsSettings() {
       return getSettings(AspectAvailable.VALUE_TYPE.INPUT_PORT);
    }

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