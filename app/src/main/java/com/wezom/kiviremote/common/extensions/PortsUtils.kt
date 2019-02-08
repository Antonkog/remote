package com.wezom.kiviremote.common.extensions

import com.wezom.kiviremote.R
import com.wezom.kiviremote.common.Constants
import com.wezom.kiviremote.net.model.AspectAvailable
import com.wezom.kiviremote.net.model.AspectMessage
import com.wezom.kiviremote.presentation.home.ports.InputSourceHelper
import com.wezom.kiviremote.presentation.home.tvsettings.driver_set.DriverValue
import com.wezom.kiviremote.upnp.org.droidupnp.view.Port
import java.util.*

class PortsUtils {
    companion object {
        fun getPortsList(list: List<DriverValue>?, available: AspectAvailable?, msg: AspectMessage?): List<Port> {
            val ports = LinkedList<Port>()
            var constants  = false
            if (list != null) {//server 19+
                list.filter {
                    it.enumValueName == Constants.INPUT_PORT
                }?.forEach {
                    ports.add(Port(it.currentName, InputSourceHelper.INPUT_PORT.getPicById(it.intCondition)
                            , it.intCondition, msg?.currentPort == it.intCondition))
                    if(msg?.currentPort == it.intCondition) constants = true
                }
                ports.add(Port("HOME", R.drawable.ic_tv
                        , Constants.INPUT_HOME_ID, !constants))
            } else {
                if(msg?.serverVersionCode ?: 20 < Constants.VER_ASPECT_XIX)
                available?.portsSettings.let {
                    ports.addAll(InputSourceHelper.getPortsList(it, msg?.currentPort
                            ?: Constants.NO_VALUE))
                }
            }
            return ports
        }
    }
}