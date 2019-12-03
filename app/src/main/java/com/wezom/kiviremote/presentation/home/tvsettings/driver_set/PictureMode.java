package com.wezom.kiviremote.presentation.home.tvsettings.driver_set;



import androidx.annotation.Nullable;

import com.wezom.kiviremote.R;
import com.wezom.kiviremote.presentation.home.tvsettings.TextTypedValues;

import java.util.LinkedList;


public enum PictureMode implements TextTypedValues {
    PICTURE_MODE_NORMAL(1, R.string.normal),
    PICTURE_MODE_SOFT(2, R.string.soft),
    PICTURE_MODE_USER(3, R.string.user),
    PICTURE_MODE_AUTO(5, R.string.economy),
    PICTURE_MODE_VIVID(7, R.string.vivid);

    private int id;
    private int string;

    PictureMode(int id, int string) {
        this.id = id;
        this.string = string;
    }


    @Nullable
    public static PictureMode getByID(int id) {
        for (PictureMode port : values()) {
            if (port.id == id) {
                return port;
            }
        }
        return null;
    }

    public static LinkedList<Integer> getResList(int[] ids) {
        LinkedList result = new LinkedList();
        for (int i = 0; i < ids.length; i++) {
            for (PictureMode port : values()) {
                if (port.id == ids[i]) {
                    result.add(port.getStringResourceID());
                }
            }
        }
        return result;
    }

    @Nullable
    public static int getIdByResID(int stringId) {
        for (PictureMode item : values()) {
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
