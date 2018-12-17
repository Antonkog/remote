package com.wezom.kiviremote.presentation.home.tvsettings.driver_set;

import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

import com.wezom.kiviremote.R;
import com.wezom.kiviremote.presentation.home.tvsettings.TextTypedValues;

import java.util.LinkedList;


public enum HDRValues implements TextTypedValues {
    HDR_OPEN_LEVEL_OFF(0, R.string.off),
    HDR_OPEN_LEVEL_AUTO(1, R.string.auto),
    HDR_OPEN_LEVEL_LOW(2, R.string.low),
    HDR_OPEN_LEVEL_MIDDLE(3, R.string.middle),
    HDR_OPEN_LEVEL_HIGH(4, R.string.high);

    @StringRes
    private int stringRes;
    private int id;

    HDRValues(int id, int stringRes) {
        this.id = id;
        this.stringRes = stringRes;
    }

    @Nullable
    public static HDRValues getByID(int id) {
        for (HDRValues item : values()) {
            if (id == item.id)
                return item;
        }
        return null;
    }


    @Nullable
    public static int getIdByResID(int stringId) {
        for (HDRValues item : values()) {
            if (stringId == item.stringRes)
                return item.id;
        }
        return -1;
    }

    @Override
    public int getStringResourceID() {
        return stringRes;
    }

    @Override
    public int getID() {
        return id;
    }

    public  static LinkedList<Integer> getResList(int[] ids) {
        LinkedList result = new LinkedList();
        for (int i = 0; i < ids.length; i++) {
            for (HDRValues port : values()) {
                if (port.id == ids[i]) {
                    result.add(port.getStringResourceID());
                }
            }
        }
        return result;
    }

}
