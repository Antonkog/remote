package com.wezom.kiviremote.presentation.home.recentdevices.item

import android.net.nsd.NsdServiceInfo
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.wezom.kiviremote.R
import com.wezom.kiviremote.presentation.home.recentdevices.TvDeviceInfo

class DevicesAdapter(private val currentConnection: String, val navigateCommand: (TvDeviceInfo) -> Unit = {}, val connectCommand: (NsdServiceInfo) -> Unit = {}, private val isShowInfoIcon: Boolean = true) : RecyclerView.Adapter<DeviceViewHolder>() {

    private val devices = mutableListOf<TvDeviceInfo>()

    fun swapData(newData: List<TvDeviceInfo>) {
        devices.clear()
        devices.addAll(newData)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        return DeviceViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.device_item, parent, false), navigateCommand, connectCommand)
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        devices[position].indexInRecentList = position
        holder.setRecentDevice(devices[position], currentConnection, isShowInfoIcon)
    }

    override fun getItemCount(): Int = devices.size

}