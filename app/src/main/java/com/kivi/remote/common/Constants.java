package com.kivi.remote.common;

/**
 * Created by andre on 19.05.2017.
 */

public class Constants {
    private Constants() {
    }

    // All in MS
    public static final int SMALL_BITMAP = 640;
    public static final int INITIAL_DELAY = 500;
    public static final long DELAY_AUTO_CONNECT = 1000;
    public static final int CONNECTION_TIMEOUT = 5000;
    public static final int RECONNECT_TRY = 3;
    public static final long DELAY_ASK_APPS = 2;
    public static final long DELAY_APPS_GET = 12000; //or show noResultDelay on swipeRefreshLayoutF
    public static final long DELAY_CHANNELS_GET = 10000; //or show noResultDelay on swipeRefreshLayoutF
    public static final long DELAY_RECONNECT = 1;
    public static final int PING_PERIOD = 2;

    public static final String CRASH_COUNTER = "crash_counter";
    public static final String LAUNCH_COUNTER = "launch_counter";
    public static final String UPDATE_SHOWING = "update_showing";

    public final static String LOG_FILE_PREFIX = "KiviLogs";
    public final static String LOG_FILE_EXTENSION = ".txt";
    public final static String MEDIA_SHARE_TXT_ID = "media_share";

    public static final int DPAD_EVENT_FREQUENCY = 200;
    public static final int VOLUME_EVENT_POINT= 1;
    public static final int VOLUME_EVENT_FREQUENCY = 75;
    public static final long TOUCH_EVENT_FREQUENCY = 10 ; //  < 1sec / 25 frames (<40)
    public static final long SCROLL_EVENT_FREQUENCY = 20 ; //  < 1sec / 25 frames (<40)
    public static final int ASPECT_GET_TRY = 3 ; //  < 1sec / 25 frames (<40)
    public static final int HOME_KEY_DELAY = 320;
    public static final int NO_VALUE = -1;
    public static final int VER_ASPECT_XVIII = 18; // AspectAvailable introduced  - inputs button, aspect settings
    public static final int VER_ASPECT_XIX = 19; // Deiver values instead of AspectAvailable
    public static final int VER_FOR_REMOTE_2= 210010254; // version of server with new API

    public static final String BUNDLE_REALUNCH_KEY = "relaunch";
    public static final int SERV_REALTEK = 1;
    public static final int SERV_MSTAR = 0;
    public static final String INPUT_PORT = "INPUT_PORT";
    public static final int INPUT_HOME_ID = -11;

    public static final int NOTIFICATION_ID = 212121;
    public static final int RESTART_APP_PI = 214212;
    public static final String UNIDENTIFIED = "unidentified";
    public static final String DARK_MODE = "dark_mode";
    public static final String AUTO_CONNECT = "auto_connect";

    public static final String IMAGE = "Фото";
    public static final String VIDEO = "Видео";
    public static final String AUDIO = "Аудио";

    public static final String CURRENT_CONNECTION_KEY = "current_connection";
    public static final String LAST_NSD_HOLDER_NAME = "current_connection";
    public static final String MUTE_STATUS_KEY = "muteStatus";
    public static final String CURSOR_SPEED_KEY = "cursor_speed";
    public static final String TAB_SELECTED_KEY = "tab_selected";
    public static final String CURRENT_CONNECTION_IP_KEY = "current_ip";

    public static final String REALTEK_INPUT_SOURCE = "persist.sys.current_input";
    public static final String SOURCE_DVB_T = "com.realtek.dtv/.tvinput.DTVTvInputService/HW33685505";
    public static final String SOURCE_DVB_C = "com.realtek.dtv/.tvinput.DTVTvInputService/HW33685504";
    public static final String SOURCE_DVB_S = "com.realtek.dtv/.tvinput.DTVTvInputService/HW33685506";
    public static final String SOURCE_ATV = "com.realtek.tv.atv/.atvinput.AtvInputService/HW33619968";
    public static final String SOURCE_AV = "com.realtek.tv.avtvinput/.AVTvInputService/HW50593792";
    public static final String SOURCE_YPBPR = "com.realtek.tv.ypptvinput/.YPPTvInputService/HW101056512";
    public static final String SOURCE_HDMI1 = "com.realtek.tv.hdmitvinput/.HDMITvInputService/HW151519232";
    public static final String SOURCE_HDMI2 = "com.realtek.tv.hdmitvinput/.HDMITvInputService/HW151519488";
    public static final String SOURCE_HDMI3 = "com.realtek.tv.hdmitvinput/.HDMITvInputService/HW151519744";
    public static final String SOURCE_VGA = "com.realtek.tv.vgatvinput/.VGATvInputService/HW117899264";

}
