package com.kivi.remote.upnp

import com.kivi.remote.upnp.org.droidupnp.view.DIDLObjectDisplay
import java.util.*
import java.util.concurrent.Callable


interface ContentCallback : Callable<Void> {
    fun setContent(content: ArrayList<DIDLObjectDisplay>)
}