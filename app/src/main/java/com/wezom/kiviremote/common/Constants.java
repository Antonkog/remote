package com.wezom.kiviremote.common;

/**
 * Created by andre on 19.05.2017.
 */

public class Constants {
    private Constants() {
    }

    // All in MS
    public static final int INITIAL_DELAY = 500;
    public static final int DPAD_EVENT_FREQUENCY = 200;
    public static final int VOLUME_EVENT_FREQUENCY = 75;
    public static final long TOUCH_EVENT_FREQUENCY = 10 ; //  < 1sec / 25 frames (<40)
    public static final long SCROLL_EVENT_FREQUENCY = 20 ; //  < 1sec / 25 frames (<40)
    public static final int ASPECT_GET_TRY = 5 ; //  < 1sec / 25 frames (<40)
    public static final int HOME_KEY_DELAY = 320;
    public static final int NO_VALUE = -1;
    public static final int VER_ASPECT_XVIII = 18;

    public static final int SERV_REALTEK = 1;
    public static final int SERV_MSTAR = 0;

    public static final int NOTIFICATION_ID = 212121;
    public static final String UNIDENTIFIED = "unidentified";

    public static final String IMAGE = "Фото";
    public static final String VIDEO = "Видео";
    public static final String AUDIO = "Аудио";

    public static final String CURRENT_CONNECTION_KEY = "current_connection";
    public static final String MUTE_STATUS_KEY = "muteStatus";
    public static final String CURSOR_SPEED_KEY = "cursor_speed";
    public static final String TAB_SELECTED_KEY = "tab_selected";
    public static final String CURRENT_CONNECTION_IP_KEY = "current_ip";
}
