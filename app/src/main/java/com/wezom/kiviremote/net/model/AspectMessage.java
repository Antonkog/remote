package com.wezom.kiviremote.net.model;
import java.util.HashMap;

import io.reactivex.annotations.Nullable;

public class AspectMessage {

    public HashMap<String, Integer> settings;

    public AspectMessage(AspectMsgBuilder builder) {
        this.settings = builder.settings;
    }

    public static class AspectMsgBuilder {
        public HashMap<String, Integer> settings;

        public AspectMsgBuilder(ASPECT_VALUE valueType, int value) {
            if (this.settings == null) this.settings = new HashMap<>();
            this.settings.put(valueType.name(), value);
        }

        public AspectMsgBuilder() {
            if (this.settings == null) this.settings = new HashMap<>();
        }

        public AspectMsgBuilder(@Nullable AspectMessage message) {
            if(message!= null && message.settings!=null)
            this.settings = message.settings;
            else this.settings = new HashMap<>();
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
        VIDEOARCTYPE
    }

    @Override
    public String toString() {
        return "AspectMessage{" +
                "settings=" + settings.toString() +
                '}';
    }
}
