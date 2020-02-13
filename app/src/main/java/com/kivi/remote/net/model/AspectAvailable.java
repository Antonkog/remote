package com.kivi.remote.net.model;

import com.kivi.remote.common.Constants;

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
    private int getPictureModeLength() {
        if (getSettings(VALUE_TYPE.PICTUREMODE) == null) return 0;
        return getSettings(VALUE_TYPE.PICTUREMODE).length;
    }

    @Nullable
    public int[] getPortsSettings() {
        return getSettings(AspectAvailable.VALUE_TYPE.INPUT_PORT);
    }

    @Override
    public String toString() {
        StringBuilder asp = new StringBuilder();
        if (settings != null) {
            Iterator it = settings.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, int[]> pair = (Map.Entry) it.next();
                asp.append(pair.getKey() + " = ");

                for (int number : pair.getValue()) {
                    asp.append(number);
                    asp.append(" ");
                }
                asp.append("\n");
            }
        }
        return asp.toString();
    }

    public int getManufacture(Integer serverVerisonCode) {
        int serv = Constants.NO_VALUE;
        if (serverVerisonCode == null) return serv;
        if (serverVerisonCode <= Constants.VER_ASPECT_XVIII) {//todo: that is valid for server version <= 18 and made to  determine aspect settings for old versions of server
            if (getPictureModeLength() == 9) serv = Constants.SERV_REALTEK;
            else if (getPictureModeLength() == 5) serv = Constants.SERV_MSTAR;
        }
        return serv;
    }

    public AspectAvailable(HashMap<String, int[]> settings) {
        this.settings = settings;
    }
}