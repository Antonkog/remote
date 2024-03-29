package com.wezom.kiviremote.presentation.home.ports;

import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;

import com.wezom.kiviremote.R;
import com.wezom.kiviremote.common.Constants;
import com.wezom.kiviremote.upnp.org.droidupnp.view.Port;

import java.util.ArrayList;
import java.util.List;

import static com.wezom.kiviremote.presentation.home.ports.InputSourceHelper.INPUT_PORT.INPUT_SOURCE_NONE;


/**
 * Created by s.gudym on 18.12.2017.
 */

public class InputSourceHelper {
    private static String TAG = InputSourceHelper.class.getSimpleName();

    public enum INPUT_PORT {
        INPUT_SOURCE_VGA(0, "vga", R.string.vga, R.drawable.ic_tv, 10, Constants.SOURCE_VGA),//ic_settings_input_component_24dp
        INPUT_SOURCE_ATV(1, "atv", R.string.atv, R.drawable.ic_tv, 60, Constants.SOURCE_ATV),//ic_settings_input_antenna_24dp
        INPUT_SOURCE_CVBS(2, "av", R.string.av, R.drawable.ic_av, 70, Constants.SOURCE_AV),//ic_settings_input_component_24dp
        INPUT_SOURCE_CVBS2(3, "cvbs2", R.string.input_cvbs, R.drawable.ic_av, 10),
        INPUT_SOURCE_CVBS3(4, "cvbs3", R.string.input_cvbs, R.drawable.ic_av, 10),
        INPUT_SOURCE_CVBS4(5, "cvbs4", R.string.input_cvbs, R.drawable.ic_av, 10),
        INPUT_SOURCE_CVBS5(6, "cvbs5", R.string.input_cvbs, R.drawable.ic_av, 10),
        INPUT_SOURCE_CVBS6(7, "cvbs6", R.string.input_cvbs, R.drawable.ic_av, 10),
        INPUT_SOURCE_CVBS7(8, "cvbs7", R.string.input_cvbs, R.drawable.ic_av, 10),
        INPUT_SOURCE_CVBS8(9, "cvbs8", R.string.input_cvbs, R.drawable.ic_av, 10),
        INPUT_SOURCE_CVBS_MAX(10, "cvbs_max", R.string.input_cvbs, R.drawable.ic_av, 10),
        INPUT_SOURCE_SVIDEO(11, "svideo", R.string.input_svideo, R.drawable.ic_av, 10),
        INPUT_SOURCE_SVIDEO2(12, "svideo2", R.string.input_svideo, R.drawable.ic_av, 10),
        INPUT_SOURCE_SVIDEO3(13, "svideo3", R.string.input_svideo, R.drawable.ic_av, 10),
        INPUT_SOURCE_SVIDEO4(14, "svideo4", R.string.input_svideo, R.drawable.ic_av, 10),
        INPUT_SOURCE_SVIDEO_MAX(15, "svideo_max", R.string.input_svideo, R.drawable.ic_av, 10),
        INPUT_SOURCE_YPBPR(16, "ypbpr", R.string.ypbpr, R.drawable.ic_av, 10, Constants.SOURCE_YPBPR),//ic_settings_input_component_24dp
        INPUT_SOURCE_YPBPR2(17, "ypbpr2", R.string.ypbpr, R.drawable.ic_av, 10),
        INPUT_SOURCE_YPBPR3(18, "ypbpr3", R.string.ypbpr, R.drawable.ic_av, 10),
        INPUT_SOURCE_YPBPR_MAX(19, "ypbpr_max", R.string.ypbpr, R.drawable.ic_av, 10),
        INPUT_SOURCE_SCART(20, "scart", R.string.input_scart, R.drawable.ic_av, 10),
        INPUT_SOURCE_SCART2(21, "scart2", R.string.input_scart, R.drawable.ic_av, 10),
        INPUT_SOURCE_SCART_MAX(22, "scart_max", R.string.input_scart, R.drawable.ic_av, 10),

        INPUT_SOURCE_HDMI(23, "hdmi", R.string.hdmi, R.drawable.ic_hdmi, 85),//ic_settings_input_hdmi_24dp
        INPUT_SOURCE_HDMI2(24, "hdmi2", R.string.hdmi2, R.drawable.ic_hdmi, 84),//ic_settings_input_hdmi_24dp
        INPUT_SOURCE_HDMI3(25, "hdmi3", R.string.hdmi, R.drawable.ic_hdmi, 83),//ic_settings_input_hdmi_24dp
        INPUT_SOURCE_HDMI4(26, "hdmi4", R.string.hdmi4, R.drawable.ic_hdmi, 82),//ic_settings_input_hdmi_24dp
        INPUT_SOURCE_HDMI_MAX(27, "hdmi_max", R.string.hdmi, R.drawable.ic_hdmi, 81),//ic_settings_input_hdmi_24dp
        INPUT_SOURCE_DTV(28, "dtv", R.string.dtv, R.drawable.ic_tv, 100, Constants.SOURCE_DVB_T),//ic_settings_input_antenna_24dp
        INPUT_SOURCE_DVI(29, "dvi", R.string.dvi, R.drawable.ic_tv, 10),//ic_settings_input_antenna_24dp
        INPUT_SOURCE_DVI2(30, "dvi2", R.string.dvi, R.drawable.ic_av, 10),
        INPUT_SOURCE_DVI3(31, "dvi3", R.string.dvi, R.drawable.ic_av, 10),
        INPUT_SOURCE_DVI4(32, "dvi4", R.string.dvi, R.drawable.ic_av, 10),
        INPUT_SOURCE_DVI_MAX(33, "dvi_max", R.string.dvi, R.drawable.ic_av, 10),
        INPUT_SOURCE_STORAGE(34, "storage", R.string.input_storage, R.drawable.ic_av, 10),
        INPUT_SOURCE_KTV(35, "ktv", R.string.input_ktv, R.drawable.ic_tv, 10),
        INPUT_SOURCE_JPEG(36, "jpeg", R.string.input_jpeg, R.drawable.ic_av, 10),
        INPUT_SOURCE_DTV2(37, "dtv2", R.string.dtv, R.drawable.ic_tv, 10),
        INPUT_SOURCE_STORAGE2(38, "storege2", R.string.input_storage, R.drawable.ic_av, 10),
        INPUT_SOURCE_DIV3(39, "div3", R.string.app_name, R.drawable.ic_av, 10),
        INPUT_SOURCE_SCALER_OP(40, "scaler_op", R.string.app_name, R.drawable.ic_av, 10),
        INPUT_SOURCE_RUV(41, "ruv", R.string.input_ruv, R.drawable.ic_av, 10),
        INPUT_SOURCE_VGA2(42, "vga2", R.string.vga, R.drawable.ic_av, 10),
        INPUT_SOURCE_VGA3(43, "vga3", R.string.vga, R.drawable.ic_av, 10),
        INPUT_SOURCE_NUM(44, "num", R.string.app_name, R.drawable.ic_av, 10),
        INPUT_SOURCE_NONE(45, "none", R.string.input_none, R.drawable.ic_tv, 10),
        INPUT_SOURCE_DVBS(46, "dvbs", R.string.dvbs, R.drawable.ic_dvb_s, 20, Constants.SOURCE_DVB_S),
        INPUT_SOURCE_DVBC(47, "dvbс", R.string.dvbc, R.drawable.ic_dvb_c, 20, Constants.SOURCE_DVB_C);

        private int id;
        private String baseName;
        @StringRes
        private int nameResoucre;
        @DrawableRes
        int drawable;
        int weight;
        String portId;
        boolean isConnected;

        INPUT_PORT(int id, String baseName, @StringRes int visibleName, @DrawableRes int res, int weight, String realtekID) {
            this(id, baseName, visibleName, res, weight);
            this.portId = realtekID;
        }

        INPUT_PORT(int id, String baseName, @StringRes int visibleName, @DrawableRes int res, int weight) {
            this.id = id;
            this.baseName = baseName;
            this.nameResoucre = visibleName;
            drawable = res;
            this.weight = weight;
        }

        public void setConnected(boolean connected) {
            isConnected = connected;
        }


        public static INPUT_PORT getPortByID(int id) {
            for (INPUT_PORT port : values()) {
                if (port.id == id) {
                    return port;
                }
            }
            return INPUT_SOURCE_NONE;
        }

        public static int getPicById(int id) {
            for (INPUT_PORT port : values()) {
                if (port.id == id) {
                    return port.drawable;
                }
            }
            return INPUT_SOURCE_NONE.drawable;
        }

        public int getId() {
            return id;
        }

        public int getNameResource() {
            return nameResoucre;
        }

        public int getDrawable() {
            return drawable;
        }
    }


    public static List<Port> getPortsList(int[] portsNumbs, int currentActive) {
        ArrayList<Port> result = new ArrayList<>();
        if (portsNumbs == null) return result;
        for (int i = 0; i < portsNumbs.length; i++) {
            INPUT_PORT port = INPUT_PORT.getPortByID(portsNumbs[i]);
            if (port != INPUT_SOURCE_NONE) {
                result.add(new Port(port.baseName, port.getDrawable(), port.id, currentActive == port.id));
            }
        }

        return result;
    }
}