package com.wezom.kiviremote.presentation.home.tvsettings.driver_set;


import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.wezom.kiviremote.R;
import com.wezom.kiviremote.presentation.home.tvsettings.TextTypedValues;

import java.util.LinkedList;

public enum TemperatureValues implements TextTypedValues {

    COLOR_TEMP_NATURE(1, R.string.nature),
    COLOR_TEMP_WARMER(2, R.string.warmer),
    COLOR_TEMP_WARM(3, R.string.warm),
    COLOR_TEMP_COOL(4, R.string.cool),
    COLOR_TEMP_COOLER(5, R.string.cooler);


    @StringRes
    private int stringRes;
    private int id;

    @Nullable
    public static TemperatureValues getByID(int id) {
        for (TemperatureValues item : values()) {
            if (id == item.id)
                return item;
        }
        return null;
    }


    @Nullable
    public static int getIdByResID(int stringId) {
        for (TemperatureValues item : values()) {
            if (stringId == item.stringRes)
                return item.id;
        }
        return -1;
    }

    TemperatureValues(int id, int stringRes) {
        this.id = id;
        this.stringRes = stringRes;
    }

    public static LinkedList<Integer> getResList(int[] ids) {
        LinkedList result = new LinkedList();
        for (int i = 0; i < ids.length; i++) {
            for (TemperatureValues port : values()) {
                if (port.id == ids[i]) {
                    result.add(port.getStringResourceID());
                }
            }
        }
        return result;
    }

    @Override
    public int getStringResourceID() {
        return stringRes;
    }

    @Override
    public int getID() {
        return id;
    }
}