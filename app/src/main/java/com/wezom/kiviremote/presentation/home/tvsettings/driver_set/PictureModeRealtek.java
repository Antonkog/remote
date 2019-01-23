package com.wezom.kiviremote.presentation.home.tvsettings.driver_set;


import android.support.annotation.Nullable;

import com.wezom.kiviremote.R;
import com.wezom.kiviremote.presentation.home.tvsettings.TextTypedValues;

import java.util.LinkedList;


public enum PictureModeRealtek implements TextTypedValues {
//realtek
    PICTURE_MODE_NORMAL(9, R.string.normal),
    PICTURE_MODE_SOFT(2, R.string.soft),
    PICTURE_MODE_USER(0, R.string.user),
    PICTURE_MODE_AUTO(7, R.string.auto),
    PICTURE_MODE_MOVIE(4, R.string.movie),
    PICTURE_MODE_SPORT(5, R.string.sport),
    PICTURE_MODE_GAME(6, R.string.game),
    PICTURE_MODE_VIVID(1, R.string.vivid);
    // PICTURE_MODE_ECONOMY(10, R.string.economy);


    private int id;
    private int string;

    PictureModeRealtek(int id, int string) {
        this.id = id;
        this.string = string;
    }


    @Nullable
    public static PictureModeRealtek getByID(int id) {
        for (PictureModeRealtek port : values()) {
            if (port.id == id) {
                return port;
            }
        }
        return null;
    }


    public static LinkedList<Integer> getResList(int[] ids) {
        LinkedList result = new LinkedList();
        for (int i = 0; i < ids.length; i++) {
            for (PictureModeRealtek port : values()) {
                if (port.id == ids[i]) {
                    result.add(port.getStringResourceID());
                }
            }
        }
        return result;
    }

    @Nullable
    public static int getIdByResID(int stringId) {
        for (PictureModeRealtek item : values()) {
            if (stringId == item.string)
                return item.id;
        }
        return -1;
    }

    @Override
    public int getStringResourceID() {
        return string;
    }

    @Override
    public int getID() {
        return id;
    }
}
