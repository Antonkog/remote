package com.wezom.kiviremote.net.model;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import timber.log.Timber;

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
        if (settings != null && !settings.isEmpty())
            return settings.get(value_type.name());
        else Timber.i(" no aspect value: " + value_type.name());
        return null;
    }

    @Nullable
    public int[] getPortsSettings() {
        return getSettings(AspectAvailable.VALUE_TYPE.INPUT_PORT);
    }

    @Override
    public String toString() {
        StringBuilder  asp  = new StringBuilder();
        if (settings != null) {
            Iterator it = settings.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry <String, int[]> pair = (Map.Entry)it.next();
                asp.append(pair.getKey() + " = " );

                for (int number :pair.getValue()) {
                    asp.append(number);
                    asp.append(" ");
                }
                asp.append("\n");
            }
        }
        return asp.toString();
    }

    public AspectAvailable(HashMap<String, int[]> settings) {
        this.settings = settings;
    }
}