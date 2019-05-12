package com.wezom.kiviremote.upnp.org.droidupnp.view

data class Port(var portName: String,
                val portImageId: Int,
                val portNum: Int,
                val active: Boolean) : Comparable<Port> {
    override fun compareTo(other: Port): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}