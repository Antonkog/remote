package com.wezom.kiviremote.presentation.home.tvsettings.driver_set;


import androidx.annotation.Nullable;

import com.wezom.kiviremote.R;
import com.wezom.kiviremote.presentation.home.tvsettings.TextTypedValues;

import java.util.LinkedList;


public enum Ratio implements TextTypedValues {

    VIDEO_ARC_DEFAULT(0, R.string.default_r),
    VIDEO_ARC_16x9(1, R.string.r_16x9),
    VIDEO_ARC_4x3(2, R.string.r_4x3),
    VIDEO_ARC_AUTO(3, R.string.auto);


    private int id;
    private int string;

    Ratio(int id, int string) {
        this.id = id;
        this.string = string;
    }


    @Override
    public int getStringResourceID() {
        return string;
    }

    @Override
    public int getID() {
        return id;
    }


    @Nullable
    public static Ratio getByID(int id) {
        for (Ratio port : values()) {
            if (port.id == id) {
                return port;
            }
        }
        return null;
    }

    @Nullable
    public static int getIdByResID(int stringId) {
        for (Ratio item : values()) {
            if (stringId == item.string)
                return item.id;
        }
        return -1;
    }


    public static LinkedList<Integer> getResList(int[] ids) {
        LinkedList result = new LinkedList();
        for (int i = 0; i < ids.length; i++) {
            for (Ratio port : values()) {
                if (port.id == ids[i]) {
                    result.add(port.getStringResourceID());
                }
            }
        }
        return result;
    }
}
