package com.kivi.remote.common.extensions

import com.kivi.remote.common.Constants
import com.kivi.remote.net.model.AspectAvailable
import com.kivi.remote.net.model.AspectMessage
import com.kivi.remote.net.model.Input
import com.kivi.remote.net.model.InputSourceHelper
import com.kivi.remote.presentation.home.tvsettings.driver_set.DriverValue
import java.util.*


class PortsUtils {
    companion object {
        fun getNewInputsList(list: List<DriverValue>?, available: AspectAvailable?, msg: AspectMessage?): List<Input> {
            val inputs = LinkedList<Input>()
            var containsActive  = false
            if (list != null && list.isNotEmpty()) {//server 19+
                list.filter {
                    it.enumValueName == Constants.INPUT_PORT
                }.forEach {


                    inputs.add(Input()
                            .addPortName(it.currentName)
                            .addPortNum(it.intCondition)
                            .addActive(msg?.currentPort == it.intCondition))

                    if(msg?.currentPort == it.intCondition) containsActive = true
                }

                inputs.add(Input()
                        .addPortName("HOME")
                        .addPortNum(Constants.INPUT_HOME_ID)
                        .addActive( !containsActive))

            } else {
                if(msg?.serverVersionCode ?: 20 < Constants.VER_ASPECT_XIX)
                    available?.portsSettings.let {
                        inputs.addAll(InputSourceHelper.getInputsList(it, msg?.currentPort
                                ?: Constants.NO_VALUE))
                    }
            }
            return inputs
        }

    }
}