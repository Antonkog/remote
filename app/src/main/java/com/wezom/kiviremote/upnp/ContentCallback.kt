package com.wezom.kiviremote.upnp

import com.wezom.kiviremote.upnp.org.droidupnp.view.DIDLObjectDisplay
import java.util.*
import java.util.concurrent.Callable


interface ContentCallback : Callable<Void> {
    fun setContent(content: ArrayList<DIDLObjectDisplay>)
}