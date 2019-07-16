package com.wezom.kiviremote.bus

import com.wezom.kiviremote.net.model.Channel
import com.wezom.kiviremote.net.model.Input


class GotInputsEvent(var inputs: List<Input>?)