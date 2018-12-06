package com.wezom.kiviremote.presentation.home.tvsettings.driver_set;

import android.content.Context;
import android.support.annotation.Nullable;

import com.wezom.kiviremote.R;


public enum Ratio {

    VIDEO_ARC_DEFAULT(1, R.string.default_r),
    VIDEO_ARC_16x9(9, R.string.r_16x9),
    VIDEO_ARC_4x3(5, R.string.r_4x3),
    VIDEO_ARC_AUTO(10, R.string.auto);

    int id;
    int string;

    Ratio(int id, int string) {
        this.id = id;
        this.string = string;
    }

    public int getId() {
        return id;
    }

    public int getString() {
        return string;
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



    public static int getIdByString(CharSequence name, Context context){
        for (Ratio item : values()) {
            if(name.equals(context.getResources().getString(item.string)))
                return item.id;
        }
        return -1;
    }


}
