package com.wezom.kiviremote.net.model;

import android.content.Context;

import java.util.HashMap;

public class AspectAvailableMock {

    public enum VALUE_TYPE {
        INPUT_PORT,
        RATIO,
        TEMPERATUREVALUES,
        HDR,
        PICTUREMODE
    }

    /*
    /todo : remove after test
        AspectHolder.INSTANCE.setAvailableSettings(new AspectAvailable(AspectAvailableMock.getRtkValues(this)));
        AspectHolder.INSTANCE.setMessage(new AspectMessage.AspectMsgBuilder()
                .addValue(AspectMessage.ASPECT_VALUE.BACKLIGHT, 2)
                .addValue(AspectMessage.ASPECT_VALUE.SATURATION, 50)
                .addValue(AspectMessage.ASPECT_VALUE.SHARPNESS, 42)
                .addValue(AspectMessage.ASPECT_VALUE.BRIGHTNESS, 52)
                .addValue(AspectMessage.ASPECT_VALUE.CONTRAST, 52)

                .addValue(AspectMessage.ASPECT_VALUE.GREEN, 23)
                .addValue(AspectMessage.ASPECT_VALUE.BLUE, 32)
                .addValue(AspectMessage.ASPECT_VALUE.RED, 23)

                .addValue(AspectMessage.ASPECT_VALUE.HDR, 22)
                .addValue(AspectMessage.ASPECT_VALUE.TEMPERATURE, 62)
                .addValue(AspectMessage.ASPECT_VALUE.VIDEOARCTYPE, 9)
                .buildAspect());

     */


    public static AspectMessage getTestMessage(){
        return new AspectMessage.AspectMsgBuilder()
                .addValue(AspectMessage.ASPECT_VALUE.BACKLIGHT, 2)
                .addValue(AspectMessage.ASPECT_VALUE.SATURATION, 50)
                .addValue(AspectMessage.ASPECT_VALUE.SHARPNESS, 42)
                .addValue(AspectMessage.ASPECT_VALUE.BRIGHTNESS, 52)
                .addValue(AspectMessage.ASPECT_VALUE.CONTRAST, 52)

                .addValue(AspectMessage.ASPECT_VALUE.GREEN, 23)
                .addValue(AspectMessage.ASPECT_VALUE.BLUE, 32)
                .addValue(AspectMessage.ASPECT_VALUE.RED, 23)

                .addValue(AspectMessage.ASPECT_VALUE.HDR, 22)
                .addValue(AspectMessage.ASPECT_VALUE.TEMPERATURE, 62)
                .addValue(AspectMessage.ASPECT_VALUE.VIDEOARCTYPE, 9)
                .buildAspect();
    }


public static HashMap<String, int[]>  getRtkValues(Context context) {
    HashMap<String, int[]> currentSettings = new HashMap<>();
    int[] picture = {0, 1, 2, 3, 4, 5, 6, 7, 9};
    int[] ratio = {1, 5, 9, 10};
    int[] temperatureValues = {1, 2, 3, 4, 5};
    int[] hdrValues = {0, 1, 2, 3, 4};

    currentSettings.put(VALUE_TYPE.RATIO.name(), ratio);
    currentSettings.put(VALUE_TYPE.TEMPERATUREVALUES.name(), picture);
    currentSettings.put(VALUE_TYPE.PICTUREMODE.name(), temperatureValues);
    currentSettings.put(VALUE_TYPE.HDR.name(), hdrValues);

    return currentSettings;


}


    public static HashMap<String, int[]>  getAllAvailableValues(Context context) {
        HashMap<String, int[]> currentSettings = new HashMap<>();
        int[] picture = {0, 1, 2, 3, 4, 5, 6, 7, 9};
        int[] ratio = {0, 1, 2, 3, 5, 9, 10};
        int[] temperatureValues = {1, 2, 3, 4, 5};
        int[] hdrValues = {0, 1, 2, 3, 4};

        currentSettings.put(VALUE_TYPE.RATIO.name(), ratio);
        currentSettings.put(VALUE_TYPE.TEMPERATUREVALUES.name(), picture);
        currentSettings.put(VALUE_TYPE.PICTUREMODE.name(), temperatureValues);
        currentSettings.put(VALUE_TYPE.HDR.name(), hdrValues);

        return currentSettings;
    }


    public static HashMap<String, int[]>  getMtcValues(Context context) {
        HashMap<String, int[]> currentSettings = new HashMap<>();
        int[] picture = {1, 2, 3, 5, 7};
        int[] ratio = {0, 1, 2, 3};
        int[] temperatureValues = {1, 2, 3, 4, 5};
        int[] hdrValues = {0, 1, 2, 3, 4};

        currentSettings.put(VALUE_TYPE.RATIO.name(), ratio);
        currentSettings.put(VALUE_TYPE.TEMPERATUREVALUES.name(), picture);
        currentSettings.put(VALUE_TYPE.PICTUREMODE.name(), temperatureValues);
        currentSettings.put(VALUE_TYPE.HDR.name(), hdrValues);

        return currentSettings;

    }

}