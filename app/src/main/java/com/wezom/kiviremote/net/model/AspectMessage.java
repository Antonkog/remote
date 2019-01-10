package com.wezom.kiviremote.net.model;

import java.util.HashMap;

public class AspectMessage {

    public HashMap<String, Integer> settings;

    public AspectMessage(AspectMsgBuilder builder) {
        this.settings = builder.settings;
    }

    public AspectMessage() {
    }

    public static class AspectMsgBuilder {
        public HashMap<String, Integer> settings;

        public AspectMsgBuilder() {
            if (this.settings == null) this.settings = new HashMap<>();
        }

        public AspectMsgBuilder addValue(ASPECT_VALUE valueType, int value) {
            if (this.settings == null) this.settings = new HashMap<>();
            settings.put(valueType.name(), value);
            return this;
        }

        public AspectMessage buildAspect() {
            return new AspectMessage(this);
        }
    }

    public enum ASPECT_VALUE {
        PICTUREMODE,
        BRIGHTNESS,
        SHARPNESS,
        SATURATION,
        BACKLIGHT,
        TEMPERATURE,
        HDR,
        GREEN,
        BLUE,
        RED,
        CONTRAST,
        VIDEOARCTYPE,
        INPUT_PORT,
        SERVER_VERSION_CODE
    }

    @Override
    public String toString() {
        return "AspectMessage{" +
                "settings=" + settings.toString() +
                '}';
    }

    public Integer getCurrentPort() {
        return settings.get(ASPECT_VALUE.INPUT_PORT.name());
    }
}
