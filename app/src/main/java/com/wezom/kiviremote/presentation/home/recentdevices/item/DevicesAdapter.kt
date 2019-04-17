package com.wezom.kiviremote.presentation.home.recentdevices.item

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.wezom.kiviremote.R
import com.wezom.kiviremote.persistence.model.RecentDevice

class DevicesAdapter(private val currentConnection: String, val command: (RecentDevice) -> Unit = {}) : RecyclerView.Adapter<DeviceViewHolder>() {

    private var devices = mutableListOf<RecentDevice>()

    fun swapData(newData: List<RecentDevice>) {
        devices.clear()
        devices.addAll(newData)
        notifyDataSetChanged()
    }

    fun addElement(data: RecentDevice) {
        devices.add(data)
        notifyItemInserted(devices.size - 1)
    }

    fun clearData() {
        devices.clear()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        return DeviceViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.device_item, parent, false), command)
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        holder.setRecentDevice(devices[position], currentConnection, true)
    }

    override fun getItemCount(): Int = devices.size

}