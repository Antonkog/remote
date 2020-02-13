package com.kivi.remote.nsd

import java.net.InetAddress

data class NsdServiceModel(val host: InetAddress, val port: Int, val name: String)